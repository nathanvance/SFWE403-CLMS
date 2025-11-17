package project2;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class TestEntryFormController {
    private static final String FILE_PATH = "src/data/TestEntries.csv";

    @FXML private TextField reportIDField;
    @FXML private TextField reportDateTimeField;

    @FXML private TextField patientIDField;
    @FXML private TextField patientNameField;
    @FXML private TextField dateOfBirthField;
    @FXML private TextField ageField;
    @FXML private TextField genderField;

    @FXML private TextField physicianIDField;
    @FXML private TextField physicianNameField;

    @FXML private TextField sampleTypeField;
    @FXML private TextField sampleIDField;
    @FXML private TextField collectionDateTimeField;
    @FXML private TextField collectedByField;
    @FXML private TextField testPerformedByField;
    @FXML private TextField machineUsedField;

    @FXML private TextField testNameField;
    @FXML private TextField resultValuesField;
    @FXML private TextField unitsField;
    @FXML private TextField normalRangeField;
    @FXML private TextField resultStatusField;
    @FXML private TextField resultsReceivedDateTimeField;

    @FXML private TextField insuranceProviderField;
    @FXML private TextField insurancePolicyNumberField;

    @FXML private Label statusLabel;

    @FXML
    private void handleEnterButton() {
        try {
            TestEntry entry = new TestEntry(
                    reportIDField.getText(),
                    reportDateTimeField.getText(),
                    patientIDField.getText(),
                    patientNameField.getText(),
                    dateOfBirthField.getText(),
                    Integer.parseInt(ageField.getText()),
                    genderField.getText(),
                    physicianIDField.getText(),
                    physicianNameField.getText(),
                    sampleTypeField.getText(),
                    sampleIDField.getText(),
                    collectionDateTimeField.getText(),
                    collectedByField.getText(),
                    testPerformedByField.getText(),
                    machineUsedField.getText(),
                    testNameField.getText(),
                    resultValuesField.getText(),
                    unitsField.getText(),
                    normalRangeField.getText(),
                    resultStatusField.getText(),
                    resultsReceivedDateTimeField.getText(),
                    insuranceProviderField.getText(),
                    insurancePolicyNumberField.getText()
            );

            entry.writeToCSV(FILE_PATH);
            statusLabel.setText("✅ Test entry successfully saved!");
            clearForm();

        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        reportIDField.clear();
        reportDateTimeField.clear();
        patientIDField.clear();
        patientNameField.clear();
        dateOfBirthField.clear();
        ageField.clear();
        genderField.clear();
        physicianIDField.clear();
        physicianNameField.clear();
        sampleTypeField.clear();
        sampleIDField.clear();
        collectionDateTimeField.clear();
        collectedByField.clear();
        testPerformedByField.clear();
        machineUsedField.clear();
        testNameField.clear();
        resultValuesField.clear();
        unitsField.clear();
        normalRangeField.clear();
        resultStatusField.clear();
        resultsReceivedDateTimeField.clear();
        insuranceProviderField.clear();
        insurancePolicyNumberField.clear();
    }

    @FXML
    private void handleGoBackButton() throws IOException {
        App.setRoot("secondary");
    }

}

