package project2;

/**
 * Simple projection of a stored lab test entry for table display.
 */
public class TestHistoryEntry {
    private final String reportId;
    private final String reportDateTime;
    private final String patientId;
    private final String patientName;
    private final String testName;
    private final String resultValue;
    private final String units;
    private final String resultStatus;
    private final String resultsReceivedDateTime;

    public TestHistoryEntry(String reportId,
                            String reportDateTime,
                            String patientId,
                            String patientName,
                            String testName,
                            String resultValue,
                            String units,
                            String resultStatus,
                            String resultsReceivedDateTime) {
        this.reportId = reportId;
        this.reportDateTime = reportDateTime;
        this.patientId = patientId;
        this.patientName = patientName;
        this.testName = testName;
        this.resultValue = resultValue;
        this.units = units;
        this.resultStatus = resultStatus;
        this.resultsReceivedDateTime = resultsReceivedDateTime;
    }

    public String getReportId() {
        return reportId;
    }

    public String getReportDateTime() {
        return reportDateTime;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getTestName() {
        return testName;
    }

    public String getResultValue() {
        return resultValue;
    }

    public String getUnits() {
        return units;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public String getResultsReceivedDateTime() {
        return resultsReceivedDateTime;
    }
}
