package project2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;



public class BackEnd {


    //function for authenticating login 
    public static boolean checkLogin(String username, String password) {
        String filePath = "C:\\Users\\henry\\Downloads\\Project2Java\\demo\\src\\main\\java\\project2\\output.csv";
        
        System.out.print("waiting for file to be read");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            System.out.print(" file has been read");
            String line;
            while ((line = br.readLine()) != null) {
                // Skip the header
                if (line.startsWith("username,password,name,age")) continue;

                String[] credentials = line.split(",");
                if (credentials.length < 2) { // Validate array length
                    System.err.println("Malformed line: " + line);
                    continue; // Skip malformed lines
                }
                String csvUsername = credentials[0];
                String csvPassword = credentials[1];
                
                

                // If the username and password match, return true
                if (csvUsername.equals(username) && csvPassword.equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    
    //function for returning the list of all users in system to and display them
    public static ObservableList<User> loadUserData(String filePath) {

        ObservableList<User> someUsers = FXCollections.observableArrayList();
        System.out.print("waiting for file to be read");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
    String filePath = "C:\\Users\\henry\\Downloads\\Project2Java\\demo\\src\\main\\java\\project2\\prescriptions.csv";

         try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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

                    Prescription somePrescription = new Prescription(pharmacistName, prescriptionName, drugName, drugQuantity);

                    userPrescrips.add(somePrescription);
                
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
        String filePath = "C:\\Users\\henry\\Downloads\\Project2Java\\demo\\src\\main\\java\\project2\\prescriptions.csv";
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            System.out.print("file to write is found");
            String newPrescriptionLine = username + "," + pharmacistName + "," + prescriptionName + "," + drugName + "," + drugQuantity;
            bw.write(newPrescriptionLine);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //function for creating new account 
    public static void createNewAccount(String usernameIn, String passwordIn, String nameIn, String AgeIn) {
      //String filePath = "C:\\Users\\henry\\Downloads\\Project2Java\\demo\\src\\main\\java\\project2\\output.csv";
      System.out.print("createNewAccount Invoked");

        String filePath = "C:\\Users\\henry\\Downloads\\Project2Java\\demo\\src\\main\\java\\project2\\output.csv";
        
            System.out.print("haven't read file yet");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
                System.out.print("file to write is found");
                String newPrescriptionLine = usernameIn + "," + passwordIn + "," + nameIn + "," + AgeIn;
                bw.write(newPrescriptionLine);
                bw.newLine();
            
            } catch (IOException e) {
                e.printStackTrace();
            }
        
        
        
    }
    
        //return whether username exists or not if exists return false else true if we write
    
}








