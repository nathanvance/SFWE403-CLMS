package project2;

public class PharmacistAssignment {
    private final String patientUsername;
    private final String medication;
    private final double quantity;

    public PharmacistAssignment(String patientUsername, String medication, double quantity) {
        this.patientUsername = patientUsername;
        this.medication = medication;
        this.quantity = quantity;
    }

    public String getPatientUsername() {
        return patientUsername;
    }

    public String getMedication() {
        return medication;
    }

    public double getQuantity() {
        return quantity;
    }
}

