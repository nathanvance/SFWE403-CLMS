package project2;

public class HealthRecord {
    private String date;
    private String summary;
    private String notes;

    public HealthRecord(String date, String summary, String notes) {
        this.date = date;
        this.summary = summary;
        this.notes = notes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

