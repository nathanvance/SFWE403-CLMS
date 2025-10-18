package project2;
import java.io.FileWriter;
import java.io.IOException;

public class TestEntry {
    // Report Information
    private String reportID;
    private String reportDateTime;

    // Patient Information
    private String patientID;
    private String patientName;
    private String dateOfBirth;
    private int age;
    private String gender;

    // Ordering Physician Information
    private String physicianID;
    private String physicianName;

    // Test Execution Details
    private String sampleType;
    private String sampleID;
    private String collectionDateTime;
    private String collectedBy;
    private String testPerformedBy;
    private String machineUsed;

    // Test Results
    private String testName;
    private String resultValues;
    private String units;
    private String normalRange;
    private String resultStatus;
    private String resultsReceivedDateTime;

    // Health Insurance / Billing
    private String insuranceProvider;
    private String insurancePolicyNumber;

    // Constructor
    public TestEntry(String reportID, String reportDateTime, String patientID, String patientName,
                     String dateOfBirth, int age, String gender, String physicianID, String physicianName,
                     String sampleType, String sampleID, String collectionDateTime, String collectedBy,
                     String testPerformedBy, String machineUsed, String testName, String resultValues,
                     String units, String normalRange, String resultStatus, String resultsReceivedDateTime,
                     String insuranceProvider, String insurancePolicyNumber) {
        this.reportID = reportID;
        this.reportDateTime = reportDateTime;
        this.patientID = patientID;
        this.patientName = patientName;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.gender = gender;
        this.physicianID = physicianID;
        this.physicianName = physicianName;
        this.sampleType = sampleType;
        this.sampleID = sampleID;
        this.collectionDateTime = collectionDateTime;
        this.collectedBy = collectedBy;
        this.testPerformedBy = testPerformedBy;
        this.machineUsed = machineUsed;
        this.testName = testName;
        this.resultValues = resultValues;
        this.units = units;
        this.normalRange = normalRange;
        this.resultStatus = resultStatus;
        this.resultsReceivedDateTime = resultsReceivedDateTime;
        this.insuranceProvider = insuranceProvider;
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    // Function to append a TestEntry to the CSV file
    public void writeToCSV(String filePath) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.append(String.join(",",
                    reportID, reportDateTime, patientID, patientName, dateOfBirth,
                    String.valueOf(age), gender, physicianID, physicianName, sampleType, sampleID,
                    collectionDateTime, collectedBy, testPerformedBy, machineUsed, testName,
                    resultValues, units, normalRange, resultStatus, resultsReceivedDateTime,
                    insuranceProvider, insurancePolicyNumber
            ));
            writer.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}