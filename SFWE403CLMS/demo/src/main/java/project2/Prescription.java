package project2;

public class Prescription {
    
    private String pharmacistName;
    private String prescriptionName;
    private String drugName;
    private String drugQuantity;

    public Prescription(String pharmacistName, String prescriptionName, String drugName, String drugQuantity) {
        this.drugName = drugName;
        this.drugQuantity = drugQuantity;
        this.pharmacistName = pharmacistName;
        this.prescriptionName = prescriptionName;
    }

    

    public String getPharmacistName() {
        return pharmacistName;
    }

    public void setPharmacistName(String pharmacistName) {
        this.pharmacistName = pharmacistName;
    }

    public String getPrescriptionName() {
        return prescriptionName;
    }

    public void setPrescriptionName(String prescriptionName) {
        this.prescriptionName = prescriptionName;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDrugQuantity() {
        return drugQuantity;
    }

    public void setDrugQuantity(String drugQuantity) {
        this.drugQuantity = drugQuantity;
    }


    


}
