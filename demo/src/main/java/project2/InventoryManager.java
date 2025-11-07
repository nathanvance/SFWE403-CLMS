package project2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized manager for reading, writing, and updating inventory records.
 * Each record represents a drug/test material identified by name and tracked in grams.
 */
public final class InventoryManager {
    private static final Path INVENTORY_FILE = Paths.get("demo/src/data/inventory.csv");
    private static final Path CHANGE_LOG_FILE = Paths.get("demo/src/data/inventory_changes.log");
    private static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final Map<String, InventoryItem> INVENTORY = new ConcurrentHashMap<>();

    static {
        try {
            initializeStorage();
            reloadFromFile();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Could not initialize inventory storage: " + e.getMessage());
        }
    }

    private InventoryManager() {}

    /**
     * Forces the manager to reread the on-disk inventory file.
     */
    public static synchronized void reloadFromFile() throws IOException {
        INVENTORY.clear();
        if (Files.notExists(INVENTORY_FILE)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(INVENTORY_FILE, StandardCharsets.UTF_8)) {
            String header = reader.readLine(); // Skip header
            if (header == null) {
                return;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 3) {
                    continue;
                }
                String displayName = parts[0].trim();
                double grams = parseDouble(parts[1]);
                Instant lastUpdated = parseInstant(parts[2]);
                INVENTORY.put(normalizeKey(displayName), new InventoryItem(displayName, grams, lastUpdated));
            }
        }
    }

    /**
     * Returns a snapshot of the inventory at the moment the method is called.
     */
    public static synchronized Map<String, Double> snapshot() {
        Map<String, Double> copy = new LinkedHashMap<>();
        INVENTORY.values().stream()
                .sorted(Comparator.comparing(InventoryItem::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .forEach(item -> copy.put(item.getDisplayName(), item.getGrams()));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Returns the current grams tracked for a specific item.
     */
    public static synchronized double getAmountInGrams(String itemName) {
        Objects.requireNonNull(itemName, "itemName cannot be null");
        InventoryItem item = INVENTORY.get(normalizeKey(itemName));
        return item == null ? 0.0 : item.getGrams();
    }

    /**
     * Applies a delta (positive or negative grams) to an item's inventory and persists the change.
     *
     * @param itemName   Drug/sample/test name being updated.
     * @param deltaGrams Positive values add stock, negative values consume stock.
     * @param changeType The type of change that was made.
     * @param changedBy  Optional actor responsible for the change.
     * @param note       Optional human-readable context for the change.
     */
    public static void recordChange(String itemName,
                                    double deltaGrams,
                                    InventoryChange changeType,
                                    String changedBy,
                                    String note) {
        Objects.requireNonNull(itemName, "itemName cannot be null");
        Objects.requireNonNull(changeType, "changeType cannot be null");
        if (deltaGrams == 0) {
            return;
        }
        synchronized (InventoryManager.class) {
            String key = normalizeKey(itemName);
            InventoryItem item = INVENTORY.computeIfAbsent(
                    key,
                    ignored -> new InventoryItem(itemName.trim(), 0.0, Instant.now())
            );

            double newAmount = roundToMilligrams(item.getGrams() + deltaGrams);
            if (newAmount < 0) {
                throw new IllegalArgumentException("Inventory cannot be negative for " + item.getDisplayName());
            }

            item.setGrams(newAmount);
            item.setLastUpdated(Instant.now());
            INVENTORY.put(key, item);

            persistInventory();
            appendChangeLog(item.getDisplayName(), deltaGrams, newAmount, changeType, changedBy, note);
        }
    }

    private static void initializeStorage() throws IOException {
        Files.createDirectories(INVENTORY_FILE.getParent());
        if (Files.notExists(INVENTORY_FILE)) {
            List<String> header = List.of("name,grams,lastUpdated");
            Files.write(INVENTORY_FILE, header, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
        if (Files.notExists(CHANGE_LOG_FILE)) {
            List<String> header = List.of("timestamp,name,delta,changeType,updatedAmount,changedBy,note");
            Files.write(CHANGE_LOG_FILE, header, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
    }

    private static void persistInventory() {
        List<String> lines = new ArrayList<>();
        lines.add("name,grams,lastUpdated");
        INVENTORY.values().stream()
                .sorted(Comparator.comparing(InventoryItem::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .forEach(item -> lines.add(String.join(",",
                        sanitizeField(item.getDisplayName()),
                        formatAmount(item.getGrams()),
                        INSTANT_FORMATTER.format(item.getLastUpdated())
                )));

        try {
            Files.write(
                    INVENTORY_FILE,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist inventory file", e);
        }
    }

    private static void appendChangeLog(String itemName,
                                        double delta,
                                        double updatedAmount,
                                        InventoryChange changeType,
                                        String changedBy,
                                        String note) {
        String line = String.join(",",
                INSTANT_FORMATTER.format(Instant.now()),
                sanitizeField(itemName),
                formatAmount(delta),
                changeType.name(),
                formatAmount(updatedAmount),
                sanitizeField(changedBy),
                sanitizeField(note)
        );
        try {
            Files.writeString(
                    CHANGE_LOG_FILE,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append inventory change log", e);
        }
    }

    private static double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static Instant parseInstant(String raw) {
        try {
            return Instant.parse(raw.trim());
        } catch (Exception e) {
            return Instant.EPOCH;
        }
    }

    private static String normalizeKey(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String sanitizeField(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace(",", " ").replaceAll("\\s+", " ").trim();
    }

    private static String formatAmount(double value) {
        return String.format(Locale.US, "%.3f", value);
    }

    private static double roundToMilligrams(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static final class InventoryItem {
        private final String displayName;
        private double grams;
        private Instant lastUpdated;

        InventoryItem(String displayName, double grams, Instant lastUpdated) {
            this.displayName = displayName;
            this.grams = grams;
            this.lastUpdated = lastUpdated;
        }

        String getDisplayName() {
            return displayName;
        }

        double getGrams() {
            return grams;
        }

        void setGrams(double grams) {
            this.grams = grams;
        }

        Instant getLastUpdated() {
            return lastUpdated;
        }

        void setLastUpdated(Instant lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }
}
