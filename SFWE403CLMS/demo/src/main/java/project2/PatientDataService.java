package project2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PatientDataService {

    private static final Path DEFAULT_DATA_PATH = Paths.get("src", "data", "patient_records.json");

    private static PatientData cachedData;

    private PatientDataService() {
        // Utility class
    }

    public static synchronized PatientData loadData() {
        if (cachedData == null) {
            cachedData = readFromFile();
            hydrateUsernames(cachedData);
        }
        return cachedData;
    }

    public static synchronized void invalidateCache() {
        cachedData = null;
    }

    public static Optional<PatientProfile> findPatient(String username) {
        return findPatientInternal(username, true);
    }

    private static Optional<PatientProfile> findPatientInternal(String username, boolean allowReload) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        PatientData data = loadData();

        PatientProfile exactMatch = data.getPatients().get(username);
        if (exactMatch != null) {
            exactMatch.setUsername(username);
            return Optional.of(exactMatch);
        }

        Optional<PatientProfile> result = data.getPatients()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && entry.getKey().equalsIgnoreCase(username))
                .map(entry -> {
                    entry.getValue().setUsername(entry.getKey());
                    return entry.getValue();
                })
                .findFirst();

        if (result.isEmpty() && allowReload) {
            invalidateCache();
            return findPatientInternal(username, false);
        }

        return result;
    }

    public static List<String> getInsuranceOptions() {
        return new ArrayList<>(loadData().getInsuranceOptions());
    }

    public static synchronized boolean updateInsurance(String username, String newInsurance) throws IOException {
        PatientData data = loadData();
        Optional<PatientProfile> maybePatient = findPatient(username);

        if (maybePatient.isEmpty()) {
            return false;
        }

        maybePatient.get().setInsurance(newInsurance);
        writeToFile(data);
        return true;
    }

    private static PatientData readFromFile() {
        Path dataPath = resolveDataPath();

        if (!Files.exists(dataPath)) {
            return new PatientData();
        }

        try {
            String raw = Files.readString(dataPath);
            if (raw == null || raw.isBlank()) {
                return new PatientData();
            }

            JSONObject root = new JSONObject(raw);
            PatientData data = new PatientData();

            JSONArray insuranceArray = root.optJSONArray("insuranceOptions");
            if (insuranceArray != null) {
                List<String> options = new ArrayList<>();
                for (int i = 0; i < insuranceArray.length(); i++) {
                    options.add(insuranceArray.optString(i, ""));
                }
                data.setInsuranceOptions(options);
            }

            JSONObject patientsObject = root.optJSONObject("patients");
            if (patientsObject != null) {
                Map<String, PatientProfile> patients = new HashMap<>();
                for (String key : patientsObject.keySet()) {
                    JSONObject patientObj = patientsObject.optJSONObject(key);
                    if (patientObj == null) {
                        continue;
                    }
                    PatientProfile profile = new PatientProfile();
                    profile.setUsername(key);
                    profile.setPharmacist(patientObj.optString("pharmacist", null));
                    profile.setInsurance(patientObj.optString("insurance", null));

                    JSONArray healthArray = patientObj.optJSONArray("healthRecords");
                    if (healthArray != null) {
                        List<HealthRecord> records = new ArrayList<>();
                        for (int i = 0; i < healthArray.length(); i++) {
                            JSONObject recordObj = healthArray.optJSONObject(i);
                            if (recordObj == null) {
                                continue;
                            }
                            HealthRecord record = new HealthRecord();
                            record.setDate(recordObj.optString("date", null));
                            record.setSummary(recordObj.optString("summary", null));
                            record.setNotes(recordObj.optString("notes", null));
                            records.add(record);
                        }
                        profile.setHealthRecords(records);
                    } else {
                        profile.setHealthRecords(null);
                    }

                    JSONArray prescriptionsArray = patientObj.optJSONArray("prescriptions");
                    if (prescriptionsArray != null) {
                        List<PrescriptionDetails> prescriptions = new ArrayList<>();
                        for (int i = 0; i < prescriptionsArray.length(); i++) {
                            JSONObject prescriptionObj = prescriptionsArray.optJSONObject(i);
                            if (prescriptionObj == null) {
                                continue;
                            }
                            PrescriptionDetails details = new PrescriptionDetails();
                            details.setName(prescriptionObj.optString("name", null));
                            if (prescriptionObj.has("quantity")) {
                                details.setQuantity(prescriptionObj.optDouble("quantity", 0.0));
                            }
                            prescriptions.add(details);
                        }
                        profile.setPrescriptions(prescriptions);
                    } else {
                        profile.setPrescriptions(null);
                    }

                    patients.put(key, profile);
                }
                data.setPatients(patients);
            }

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new PatientData();
        }
    }

    private static void writeToFile(PatientData data) throws IOException {
        if (data == null) {
            return;
        }

        Path dataPath = resolveDataPath();

        JSONObject root = new JSONObject();
        root.put("insuranceOptions", new JSONArray(data.getInsuranceOptions()));

        JSONObject patientsObject = new JSONObject();
        for (Map.Entry<String, PatientProfile> entry : data.getPatients().entrySet()) {
            String username = entry.getKey();
            PatientProfile profile = entry.getValue();
            if (profile == null) {
                continue;
            }

            JSONObject patientObj = new JSONObject();
            if (profile.getPharmacist() != null) {
                patientObj.put("pharmacist", profile.getPharmacist());
            }
            if (profile.getInsurance() != null) {
                patientObj.put("insurance", profile.getInsurance());
            }

            if (profile.getHealthRecords() != null && !profile.getHealthRecords().isEmpty()) {
                JSONArray healthArray = new JSONArray();
                for (HealthRecord record : profile.getHealthRecords()) {
                    if (record == null) {
                        continue;
                    }
                    JSONObject recordObj = new JSONObject();
                    if (record.getDate() != null) {
                        recordObj.put("date", record.getDate());
                    }
                    if (record.getSummary() != null) {
                        recordObj.put("summary", record.getSummary());
                    }
                    if (record.getNotes() != null) {
                        recordObj.put("notes", record.getNotes());
                    }
                    healthArray.put(recordObj);
                }
                patientObj.put("healthRecords", healthArray);
            }

            if (profile.getPrescriptions() != null && !profile.getPrescriptions().isEmpty()) {
                JSONArray prescriptionsArray = new JSONArray();
                for (PrescriptionDetails prescription : profile.getPrescriptions()) {
                    if (prescription == null) {
                        continue;
                    }
                    JSONObject prescriptionObj = new JSONObject();
                    if (prescription.getName() != null) {
                        prescriptionObj.put("name", prescription.getName());
                    }
                    prescriptionObj.put("quantity", prescription.getQuantity());
                    prescriptionsArray.put(prescriptionObj);
                }
                patientObj.put("prescriptions", prescriptionsArray);
            }

            patientsObject.put(username, patientObj);
        }

        root.put("patients", patientsObject);

        Files.createDirectories(dataPath.getParent());
        Files.writeString(dataPath, root.toString(2));
        cachedData = data;
        hydrateUsernames(data);
    }

    private static Path resolveDataPath() {
        List<Path> candidates = List.of(
                DEFAULT_DATA_PATH,
                Paths.get("demo", "src", "data", "patient_records.json"),
                Paths.get("SFWE403CLMS", "demo", "src", "data", "patient_records.json")
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        return DEFAULT_DATA_PATH;
    }

    private static void hydrateUsernames(PatientData data) {
        if (data == null) {
            return;
        }

        for (Map.Entry<String, PatientProfile> entry : data.getPatients().entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().setUsername(entry.getKey());
            }
        }
    }
}
