package project2;

public class PrescriptionDetails {
    private String name;
    private double quantity;

    public PrescriptionDetails() {
        // Required by Jackson
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
