package project2;

public class PatientProfile {
    private String username;
    private String pharmacist;
    private String insurance;
    private java.util.List<HealthRecord> healthRecords = new java.util.ArrayList<>();
    private java.util.List<PrescriptionDetails> prescriptions = new java.util.ArrayList<>();

    public PatientProfile() {
        // Required by Jackson
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPharmacist() {
        return pharmacist;
    }

    public void setPharmacist(String pharmacist) {
        this.pharmacist = pharmacist;
    }

    public String getInsurance() {
        return insurance;
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
    }

    public java.util.List<HealthRecord> getHealthRecords() {
        return healthRecords;
    }

    public void setHealthRecords(java.util.List<HealthRecord> healthRecords) {
        this.healthRecords = healthRecords != null ? healthRecords : new java.util.ArrayList<>();
    }

    public java.util.List<PrescriptionDetails> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(java.util.List<PrescriptionDetails> prescriptions) {
        this.prescriptions = prescriptions != null ? prescriptions : new java.util.ArrayList<>();
    }
}
