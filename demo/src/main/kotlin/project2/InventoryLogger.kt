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

}

data class InventoryObject(val name: String, val quantity: Int, val min: Int, val changes: HashMap<Date, Int>);


