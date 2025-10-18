package project2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lightweight bridge between JavaFX dashboards and the Kotlin data layer (Patient.kt).
 * Supports both patient and pharmacist dashboards, with simple caching.
 */
public final class PatientDataService {

    private static List<Patient> cachedPatients = null;

    private PatientDataService() {}

    // ==========================
    //  CACHE MANAGEMENT
    // ==========================

    /** Clears the in-memory cache of loaded patients. */
    public static synchronized void invalidateCache() {
        cachedPatients = null;
    }

    /** Loads all patients (with caching). */
    private static synchronized List<Patient> loadAllPatientsCached() {
        if (cachedPatients == null) {
            cachedPatients = Patient.Companion.loadAllPatients();
        }
        return cachedPatients;
    }

    // ==========================
    //  PATIENT UTILITIES
    // ==========================

    public static Optional<Patient> findPatient(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return loadAllPatientsCached().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public static List<String> getInsuranceOptions() {
        return Patient.Companion.loadInsuranceOptions();
    }

    public static boolean updateInsurance(String username, String newInsurance) throws IOException {
        Patient patient = Patient.Companion.loadByUsername(username);
        if (patient == null) return false;
        patient.setInsurance(newInsurance);
        patient.updatePatientInJson();
        // ✅ refresh cache after update
        invalidateCache();
        return true;
    }

    // ==========================
    //  PHARMACIST UTILITIES
    // ==========================

    /** Loads all patients assigned to a given pharmacist. */
    public static List<Patient> getPatientsForPharmacist(String pharmacistId) {
        List<Patient> allPatients = loadAllPatientsCached();
        List<Patient> assigned = new ArrayList<>();

        if (pharmacistId == null || pharmacistId.isBlank()) {
            return assigned;
        }

        String normalizedId = sanitize(pharmacistId);

        for (Patient p : allPatients) {
            String pharmField = p.getPharmacistName();
            if (pharmField == null) continue;
            if (sanitize(pharmField).contains(normalizedId) ||
                    sanitize(p.getUsername()).equals(normalizedId)) {
                assigned.add(p);
            }
        }

        return assigned;
    }

    /** Builds pharmacist assignments for dashboard display. */
    public static List<PharmacistAssignment> getAssignmentsForPharmacist(String pharmacistId) {
        List<PharmacistAssignment> assignments = new ArrayList<>();
        List<Patient> patients = getPatientsForPharmacist(pharmacistId);

        for (Patient patient : patients) {
            if (patient.getMedications() == null || patient.getMedications().isEmpty()) {
                assignments.add(new PharmacistAssignment(
                        patient.getUsername(),
                        "No prescriptions assigned",
                        0.0
                ));
                continue;
            }

            patient.getMedications().forEach(pres -> {
                assignments.add(new PharmacistAssignment(
                        patient.getUsername(),
                        pres.getPrescriptionName(),
                        pres.getDrugQuantity()
                ));
            });
        }

        return assignments;
    }

    /** Gets display name of pharmacist (based on assigned patients). */
    public static Optional<String> getPharmacistDisplayName(String pharmacistId) {
        List<Patient> allPatients = loadAllPatientsCached();
        String normalizedId = sanitize(pharmacistId);

        return allPatients.stream()
                .map(Patient::getPharmacistName)
                .filter(name -> name != null && sanitize(name).contains(normalizedId))
                .findFirst();
    }

    // ==========================
    //  HELPERS
    // ==========================

    private static String sanitize(String value) {
        if (value == null) return "";
        return value.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }
}
