package project2

sealed class InventoryChange {
    data class SetMin(val min: Int) : InventoryChange()
    data class Rename(val newName: String) : InventoryChange()
}

sealed class QuantityChange : InventoryChange() {
    data class SetQuantity(val newVal: Int) : QuantityChange()
    data class AddQuantity(val amount: Int) : QuantityChange()
    data class DecreaseQuantity(val amount: Int) : QuantityChange()
}