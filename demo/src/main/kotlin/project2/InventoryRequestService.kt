package project2

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

object InventoryRequestService {
    private val file = File("src/data/inventory_requests.json")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    private var requests: MutableList<InventoryRequest> = mutableListOf()

    init {
        loadFromJson()
    }

    fun createRequest(pharmacistUsername: String, itemName: String, quantity: Int, reason: String): InventoryRequest {
        val request = InventoryRequest(
            UUID.randomUUID().toString(),
            pharmacistUsername,
            itemName,
            quantity,
            reason,
            Date(),
            InventoryRequest.RequestStatus.PENDING,
            null,
            null,
            null
        )

        requests.add(request)
        saveToJson()
        return request
    }

    fun getRequestById(id: String): InventoryRequest? {
        return requests.find { it.id == id }
    }

    fun getRequestsByPharmacist(pharmacistUsername: String): List<InventoryRequest> {
        return requests.filter { it.pharmacistUsername == pharmacistUsername }
            .sortedByDescending { it.requestDate }
    }

    fun getPendingRequests(): List<InventoryRequest> {
        return requests.filter { it.status == InventoryRequest.RequestStatus.PENDING }
            .sortedBy { it.requestDate }
    }

    fun getAllRequests(): List<InventoryRequest> {
        return requests.sortedByDescending { it.requestDate }
    }

    fun approveRequest(requestId: String, adminUsername: String, notes: String = ""): Boolean {
        val request = getRequestById(requestId) ?: return false
        request.approve(adminUsername, notes)
        saveToJson()
        return true
    }

    fun denyRequest(requestId: String, adminUsername: String, notes: String): Boolean {
        val request = getRequestById(requestId) ?: return false
        request.deny(adminUsername, notes)
        saveToJson()
        return true
    }

    fun fulfillRequest(requestId: String): Boolean {
        val request = getRequestById(requestId) ?: return false
        if (request.status != InventoryRequest.RequestStatus.APPROVED) return false
        request.fulfill()
        saveToJson()
        return true
    }

    private fun loadFromJson() {
        if (!file.exists()) {
            println("Inventory requests file not found, creating new one at ${file.absolutePath}")
            file.parentFile?.mkdirs()
            requests = mutableListOf()
            saveToJson()
            return
        }

        try {
            val jsonText = file.readText()
            if (jsonText.isBlank() || jsonText.trim() == "[]") {
                requests = mutableListOf()
                println("Loaded 0 inventory requests (empty file)")
                return
            }

            val jsonArray = JSONArray(jsonText)
            requests = mutableListOf()

            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                val request = InventoryRequest()

                request.id = jsonObj.getString("id")
                request.pharmacistUsername = jsonObj.getString("pharmacistUsername")
                request.itemName = jsonObj.getString("itemName")
                request.requestedQuantity = jsonObj.getInt("requestedQuantity")
                request.reason = jsonObj.getString("reason")
                request.requestDate = dateFormat.parse(jsonObj.getString("requestDate"))
                request.status = InventoryRequest.RequestStatus.valueOf(jsonObj.getString("status"))
                request.reviewedBy = if (jsonObj.isNull("reviewedBy")) null else jsonObj.getString("reviewedBy")
                request.reviewDate =
                    if (jsonObj.isNull("reviewDate")) null else dateFormat.parse(jsonObj.getString("reviewDate"))
                request.adminNotes = if (jsonObj.isNull("adminNotes")) null else jsonObj.getString("adminNotes")

                requests.add(request)
            }

            println("Loaded ${requests.size} inventory requests")
        } catch (e: Exception) {
            println("Error loading inventory requests: ${e.message}")
            e.printStackTrace()
            requests = mutableListOf()
        }
    }

    private fun saveToJson() {
        try {
            file.parentFile?.mkdirs()

            val jsonArray = JSONArray()
            for (request in requests) {
                val jsonObj = JSONObject()
                jsonObj.put("id", request.id)
                jsonObj.put("pharmacistUsername", request.pharmacistUsername)
                jsonObj.put("itemName", request.itemName)
                jsonObj.put("requestedQuantity", request.requestedQuantity)
                jsonObj.put("reason", request.reason)
                jsonObj.put("requestDate", dateFormat.format(request.requestDate))
                jsonObj.put("status", request.status.name)
                jsonObj.put("reviewedBy", request.reviewedBy)
                jsonObj.put(
                    "reviewDate",
                    if (request.reviewDate != null) dateFormat.format(request.reviewDate) else null
                )
                jsonObj.put("adminNotes", request.adminNotes)

                jsonArray.put(jsonObj)
            }

            file.writeText(jsonArray.toString(2)) // Pretty print with 2-space indent
            println("Saved ${requests.size} inventory requests to JSON")
        } catch (e: Exception) {
            println("Error saving inventory requests: ${e.message}")
            e.printStackTrace()
        }
    }

    fun refresh() {
        loadFromJson()
    }
}
