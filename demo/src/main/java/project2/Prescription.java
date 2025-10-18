package project2;

public class Prescription {
    
    public String pharmacistName;
    public String prescriptionName;
    public String drugName;
    public Double drugQuantity;
    public String lastPickupDate;

    public Prescription(String pharmacistName, String prescriptionName, String drugName, Double drugQuantity) {
        this.drugName = drugName;
        this.drugQuantity = drugQuantity;
        this.pharmacistName = pharmacistName;
        this.prescriptionName = prescriptionName;
        this.lastPickupDate = null;
    }

    public String getPharmacistName() {
        return pharmacistName;
    }

    public String getPrescriptionName() {
        return prescriptionName;
    }

    public String getDrugName() {
        return drugName;
    }

    public Double getDrugQuantity() {
        return drugQuantity;
    }

    public String getLastPickupDate() {
        return lastPickupDate;
    }

    public void setLastPickupDate(String lastPickupDate) {
        this.lastPickupDate = lastPickupDate;
    }

}
