package project2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientData {
    private Map<String, PatientProfile> patients = new HashMap<>();
    private List<String> insuranceOptions = new ArrayList<>();

    public PatientData() {
        // Required by Jackson
    }

    public Map<String, PatientProfile> getPatients() {
        return patients;
    }

    public void setPatients(Map<String, PatientProfile> patients) {
        this.patients = patients;
    }

    public List<String> getInsuranceOptions() {
        return insuranceOptions;
    }

    public void setInsuranceOptions(List<String> insuranceOptions) {
        this.insuranceOptions = insuranceOptions;
    }
}
