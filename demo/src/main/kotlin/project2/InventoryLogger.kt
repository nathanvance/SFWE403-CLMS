package project2

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.Date

object InventoryLogger {
    val file = File("src/drug_changes.json");
    val items: MutableSet<InventoryObject> = mutableSetOf();

    init {
        loadDataFromJson();
    }

    fun updateItem(name: String, change: InventoryChange): Boolean {
        val item = findObjectElseNull(name) ?: return false
        item.applyChange(change)
        saveToJson()
        return true
    }

    fun findObjectElseNull(name: String): InventoryObject? {
        return items.find { it.name.contains(name) }
    }

    private fun loadDataFromJson() {
        if (!file.exists()) {
            return println("JSON file not found at ${file.absolutePath}")
        }

        val gson = Gson();
        val jsonText = file.readText();
        val mapType = object : TypeToken<Map<String, Any>>() {}.type;
        val rootMap: Map<String, Any> = gson.fromJson(jsonText, mapType);

        val inventoryItems = rootMap["inventory items"] as? Map<*, *> ?: return
        val drugMap = inventoryItems["drugs"] as? Map<*, *> ?: return

        val name = drugMap["name"] as? String ?: return;
        val quantity = (drugMap["quantity"] as? Number ?: return).toInt();
        val minAmount = (drugMap["minamount"] as? Number ?: return).toInt()
        val changesList = drugMap["changes"] as? List<List<*>> ?: return;

        val changesMap = HashMap<Date, Int>()
        for (entry in changesList) {
            val dateString = entry[0] as? String ?: return;
            val amount = (entry[1] as? Number ?: return).toInt()
            val date = Date.from(java.time.Instant.parse(dateString))
            changesMap[date] = amount
        }

        val inventoryObject = InventoryObject(name, quantity, minAmount, changesMap);
        items.add(inventoryObject);
    }

    private fun saveToJson() {
        val gson = Gson();
        val inventoryMap = mutableMapOf<String, Any>()
        for (item in items) {
            val itemJson = mapOf(
                "name" to item.name,
                "quantity" to item.quantity,
                "minamount" to item.min,
                "changes" to item.changes.map { (date, qty) ->
                    listOf(date.toInstant().toString(), qty)
                }
            )

            inventoryMap["drugs"] = itemJson
        }
        val root = mapOf(
            "inventory items" to inventoryMap
        )
        val jsonText = gson.toJson(root)
        file.writeText((jsonText))

        println("Saved current inventory to JSON file.")
    }

}

data class InventoryObject(
    var name: String,
    var quantity: Int,
    var min: Int,
    var changes: HashMap<Date, Int>
) {
    fun applyChange(change: InventoryChange) {
        when (change) {
            is QuantityChange.SetQuantity -> quantity = change.newVal
            is QuantityChange.AddQuantity -> quantity += change.amount
            is QuantityChange.DecreaseQuantity -> quantity -= change.amount
            is InventoryChange.Rename -> name = change.newName
            is InventoryChange.SetMin -> min = change.min
        }

        if (change is QuantityChange) return
        changes[Date()] = quantity
    }
}


