package project2

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.Date

object InventoryLogger {
    val file = File("src/data/drug_changes.json");
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
        return items.find { it.name.contains(name, ignoreCase = true) }
    }

    fun getAllItems(): List<InventoryObject> {
        return items.toList()
    }

    fun addNewItem(name: String, quantity: Int, minAmount: Int): InventoryObject {
        val item = InventoryObject(name, quantity, minAmount, HashMap())
        items.add(item)
        saveToJson()
        return item
    }

    fun refresh() {
        items.clear()
        loadDataFromJson()
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
        val drugsData = inventoryItems["drugs"]

        // Handle both single object and array of objects
        val drugsList = when (drugsData) {
            is List<*> -> drugsData
            is Map<*, *> -> listOf(drugsData)
            else -> return
        }

        for (drugData in drugsList) {
            val drugMap = drugData as? Map<*, *> ?: continue

            val name = drugMap["name"] as? String ?: continue
            val quantity = (drugMap["quantity"] as? Number ?: continue).toInt()
            val minAmount = (drugMap["minamount"] as? Number ?: continue).toInt()
            val changesList = drugMap["changes"] as? List<List<*>> ?: continue

            val changesMap = HashMap<Date, Int>()
            for (entry in changesList) {
                val dateString = entry[0] as? String ?: continue
                val amount = (entry[1] as? Number ?: continue).toInt()
                val date = Date.from(java.time.Instant.parse(dateString))
                changesMap[date] = amount
            }

            val inventoryObject = InventoryObject(name, quantity, minAmount, changesMap)
            items.add(inventoryObject)
        }

        println("Loaded ${items.size} inventory items from JSON")
    }

    private fun saveToJson() {
        val gson = Gson();
        val drugsList = mutableListOf<Map<String, Any>>()

        for (item in items) {
            val itemJson = mapOf(
                "name" to item.name,
                "quantity" to item.quantity,
                "minamount" to item.min,
                "changes" to item.changes.map { (date, qty) ->
                    listOf(date.toInstant().toString(), qty)
                }
            )
            drugsList.add(itemJson)
        }

        val inventoryMap = mapOf("drugs" to drugsList)
        val root = mapOf("inventory items" to inventoryMap)
        val jsonText = gson.toJson(root)
        file.writeText(jsonText)

        println("Saved ${items.size} inventory items to JSON file.")
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

        if (change is QuantityChange) {
            changes[Date()] = quantity
        }
    }

    fun getChangesHistory(): Map<Date, Int> {
        return changes.toSortedMap()
    }

    fun isLowStock(): Boolean {
        return quantity <= min
    }
}


