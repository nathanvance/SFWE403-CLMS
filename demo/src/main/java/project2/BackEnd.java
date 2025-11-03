package project2;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class BackEnd {
    private static final String FILE_PATH = "demo/src/data/output.csv";
    private static final File LOGIN_FILE = new File(FILE_PATH);

    //function for authenticating login
    private BackEnd() {}
    public static boolean checkLogin(String username, String password) {
        return LoginManager.usernameExists(username);
    }
    public static boolean createNewAccount(String username, String password, String name, String age) {
        return LoginManager.register(username, password);
    }
    //function for returning the list of all users in system to and display them
    public static ObservableList<User> loadUserData() {

        ObservableList<User> someUsers = FXCollections.observableArrayList();
        System.out.print("waiting for file to be read");
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            System.out.print("file read");
            String line;
            // Skip header
            br.readLine();

            // Read each line of the CSV
            while ((line = br.readLine()) != null) {

                if (line.startsWith("username,password,name,age")) continue;

                String[] values = line.split(",");
                if (values.length == 4) {
                    String username = values[0];
                    String password = values[1];
                    String name = values[2];
                    String age = values[3];

                    // Create a new User object and add it to the list
                    someUsers.add(new User(username, password, name, age));
                    //User(String username, String password, String name, String age, String medications) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return someUsers;
    }


    //this is for a search button to display the users prescriptions 
    public static ObservableList<Prescription> loadUserPrescriptions(String username) {
        ObservableList<Prescription> userPrescrips = FXCollections.observableArrayList();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean prescriptionsFound = false;


            while ((line = br.readLine()) != null) {
                // Skip the header
                if (line.startsWith("username,pharmacistName,prescriptionName,drugName,drugQuantity")) continue;

                String[] prescription = line.split(",");
                String csvUsername = prescription[0];

                if (csvUsername.equals(username)) {
                    prescriptionsFound = true;

                    String pharmacistName = prescription[1];
                    String prescriptionName = prescription[2];
                    String drugName = prescription[3];
                    String drugQuantity = prescription[4];
//
//                    Prescription somePrescription = new Prescription(pharmacistName, prescriptionName, drugName, drugQuantity);
//
//                    userPrescrips.add(somePrescription);

                }

            }

            // If no prescriptions are found, display a message to terminal
            if (!prescriptionsFound) {
                System.out.print("Prescriptions not found");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return userPrescrips;
    }

    //function for adding new prescriptions
    public static void addPrescriptionToCSV(String username, String pharmacistName, String prescriptionName, String drugName, String drugQuantity) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            System.out.print("file to write is found");
            String newPrescriptionLine = username + "," + pharmacistName + "," + prescriptionName + "," + drugName + "," + drugQuantity;
            bw.write(newPrescriptionLine);
            bw.newLine();

            // Log the inventory change
            try {
                double quantity = Double.parseDouble(drugQuantity);
                InventoryLogger.logInventoryChange(drugName, quantity, pharmacistName);
            } catch (NumberFormatException e) {
                System.err.println("Could not parse drug quantity for inventory logging: " + drugQuantity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}








