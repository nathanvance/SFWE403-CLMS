package project2

class Patient(user: String, passHash: String) : UserKT(user, passHash) {
    val medications = mutableSetOf<Prescription>();
    var insurance: InsuranceInformation? = null;

    fun addPrescription(pres: Prescription): Boolean {
        return medications.add(pres);
    }

    fun removePrescription(presName: String): Boolean {
        return medications.find { it.prescriptionName.contains(presName) }.let {
            medications.remove(it);
        } ?: false;
    }

}