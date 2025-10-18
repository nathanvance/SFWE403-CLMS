package project2

class Pharmacist(user: String, passHash: String) : UserKT(user, passHash) {

    val patients: List<Patient> = findPatients()
    val outgoingTests: List<String> = findTests()

    private fun findPatients(): List<Patient> {
        return Patient.loadAllPatients().filter { patient ->
            patient.pharmacistName.equals(this.username, ignoreCase = true)
        }
    }

    private fun findTests(): List<String> {
        return emptyList()
    }
}
