package project2

import java.util.Date
import java.util.UUID

class InventoryRequest(
    var id: String = "",
    var pharmacistUsername: String = "",
    var itemName: String = "",
    var requestedQuantity: Int = 0,
    var reason: String = "",
    var requestDate: Date = Date(),
    var status: RequestStatus = RequestStatus.PENDING,
    var reviewedBy: String? = null,
    var reviewDate: Date? = null,
    var adminNotes: String? = null,
) {
    enum class RequestStatus {
        PENDING,
        APPROVED,
        DENIED,
        FULFILLED
    }

    fun approve(adminUsername: String, notes: String = "") {
        status = RequestStatus.APPROVED
        reviewedBy = adminUsername
        reviewDate = Date()
        adminNotes = notes
    }

    fun deny(adminUsername: String, notes: String) {
        status = RequestStatus.DENIED
        reviewedBy = adminUsername
        reviewDate = Date()
        adminNotes = notes
    }

    fun fulfill() {
        status = RequestStatus.FULFILLED
    }
}
