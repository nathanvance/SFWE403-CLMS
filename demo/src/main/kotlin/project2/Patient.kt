package project2

import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Patient(user: String, passHash: String) : UserKT(user, passHash) {
    val medications = mutableSetOf<Prescription>()
    val healthRecords = mutableListOf<HealthRecord>()
    var pharmacistName: String? = null
    var accountStatus: String? = null
    var insurance: String? = null
    var pharmacistId: String? = null

    init {
        loadDataFromJson()
    }

    private fun loadDataFromJson() {
        val file = File("demo/src/data/patient_data.json")
        if (!file.exists()) {
            println("⚠️ JSON file not found at ${file.absolutePath}")
            return
        }

        val gson = Gson()
        val jsonText = file.readText()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val rootMap: Map<String, Any> = gson.fromJson(jsonText, mapType)

        val patientsList = (rootMap["patients"] as? List<Map<String, Any>>)?.mapNotNull { it } ?: return
        val entry = patientsList.find { it["username"] == this.username } ?: return

        this.pharmacistName = entry["pharmacist"]?.toString()
        this.pharmacistId = entry["pharmacistId"]?.toString()  // ✅ new line
        this.accountStatus = entry["accountStatus"]?.toString()
        this.insurance = entry["insurance"]?.toString()

        val prescriptions = (entry["prescriptions"] as? List<Map<String, Any>>)?.mapNotNull { pres ->
            try {
                val quantity = when (val q = pres["quantity"]) {
                    is Number -> q.toDouble()
                    is String -> q.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                val prescription = Prescription(
                    this.pharmacistName ?: "",
                    pres["name"]?.toString() ?: "Unknown Prescription",
                    pres["name"]?.toString() ?: "Unknown Drug",
                    quantity
                )
                prescription.lastPickupDate = pres["lastPickupDate"]?.toString()
                prescription
            } catch (e: Exception) {
                println("⚠️ Skipped bad prescription for ${this.username}: ${e.message}")
                null
            }
        } ?: emptyList()
        this.medications.addAll(prescriptions)

        val history = (entry["healthHistory"] as? List<Map<String, Any>>)?.mapNotNull { rec ->
            try {
                HealthRecord(
                    rec["date"]?.toString() ?: "Unknown Date",
                    rec["summary"]?.toString() ?: "No Summary",
                    rec["notes"]?.toString() ?: "No Notes"
                )
            } catch (e: Exception) {
                println("⚠️ Skipped bad health record for ${this.username}: ${e.message}")
                null
            }
        } ?: emptyList()
        this.healthRecords.addAll(history)
    }

    fun addPrescription(pres: Prescription): Boolean {
        val added = medications.add(pres)
        if (added) updatePatientInJson()
        return added
    }

    fun removePrescription(presName: String): Boolean {
        val presToRemove = medications.find { it.prescriptionName.equals(presName, ignoreCase = true) }
        val removed = presToRemove != null && medications.remove(presToRemove)
        if (removed) updatePatientInJson()
        return removed
    }

    fun addHealthRecord(record: HealthRecord): Boolean {
        val added = healthRecords.add(record)
        if (added) updatePatientInJson()
        return added
    }

    fun updatePatientInJson() {
        val file = File("src/data/patient_data.json")
        if (!file.exists()) return

        val gson = Gson()
        val jsonText = file.readText()

        val mapType = object : TypeToken<MutableMap<String, Any>>() {}.type
        val jsonMap = gson.fromJson<MutableMap<String, Any>>(jsonText, mapType)

        val patientsList = jsonMap["patients"] as? List<Map<String, Any>> ?: return
        val updatedPatients = patientsList.map { patient ->
            if (patient["username"] == this.username) {
                val updatedPrescriptions = this.medications.map { pres ->
                    mapOf(
                        "name" to pres.prescriptionName,
                        "quantity" to pres.drugQuantity,
                        "lastPickupDate" to pres.lastPickupDate
                    )
                }

                val updatedHealthRecords = this.healthRecords.map { rec ->
                    mapOf(
                        "date" to rec.date,
                        "summary" to rec.summary,
                        "notes" to rec.notes
                    )
                }

                patient.toMutableMap().apply {
                    this["prescriptions"] = updatedPrescriptions
                    this["healthHistory"] = updatedHealthRecords
                    this["insurance"] = this@Patient.insurance ?: ""
                    this["pharmacistId"] = this@Patient.pharmacistId ?: ""
                }
            } else patient
        }

        val insuranceOptions = jsonMap["insuranceOptions"] ?: emptyList<String>()
        val updatedJson = gson.toJson(mapOf("patients" to updatedPatients, "insuranceOptions" to insuranceOptions))
        file.writeText(updatedJson)
    }

    companion object {
        private val gson = Gson()

        fun loadAllPatients(): List<Patient> {
            val file = File("src/data/patient_data.json")
            if (!file.exists()) return emptyList()

            val jsonText = file.readText()
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val rootMap: Map<String, Any> = gson.fromJson(jsonText, mapType)
            val patientsList = (rootMap["patients"] as? List<Map<String, Any>>)?.mapNotNull { it } ?: return emptyList()

            return patientsList.mapNotNull { p ->
                try {
                    val username = p["username"]?.toString() ?: return@mapNotNull null
                    val hash = p["hash"]?.toString() ?: ""
                    val patient = Patient(username, hash)
                    patient.pharmacistName = p["pharmacist"]?.toString()
                    patient.accountStatus = p["accountStatus"]?.toString()
                    patient.insurance = p["insurance"]?.toString()

                    // ✅ Load health records if available
                    val history = (p["healthHistory"] as? List<Map<String, Any>>)?.mapNotNull { rec ->
                        try {
                            HealthRecord(
                                rec["date"]?.toString() ?: "Unknown Date",
                                rec["summary"]?.toString() ?: "No Summary",
                                rec["notes"]?.toString() ?: "No Notes"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    patient.healthRecords.addAll(history)

                    patient
                } catch (e: Exception) {
                    println("⚠️ Skipped bad patient: ${e.message}")
                    null
                }
            }
        }

        fun loadInsuranceOptions(): List<String> {
            val file = File("src/data/patient_data.json")
            if (!file.exists()) return emptyList()
            val jsonText = file.readText()
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val jsonMap: Map<String, Any> = gson.fromJson(jsonText, mapType)
            return (jsonMap["insuranceOptions"] as? List<String>) ?: emptyList()
        }

        fun loadByUsername(username: String): Patient? {
            return loadAllPatients().find { it.username.equals(username, ignoreCase = true) }
        }
    }
}
