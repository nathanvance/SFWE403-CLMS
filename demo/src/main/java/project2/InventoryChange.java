package project2;

/**
 * Enumerates the types of updates that can be applied to drug/test inventories.
 */
public enum InventoryChange {
    RECEIVED("Inventory increased after receiving new material."),
    DISPENSED("Inventory decreased because a prescription was fulfilled."),
    SAMPLE_USED("Inventory decreased for lab sample or test material consumption."),
    ADJUSTMENT("Inventory adjusted after an audit or correction.");

    private final String description;

    InventoryChange(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
