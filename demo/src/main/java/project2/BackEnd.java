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
        String filePath = "demo/output.csv";
        
        System.out.print("waiting for file to be read");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            System.out.print(" file has been read");
            String line;
            while ((line = br.readLine()) != null) {
                // Skip the header
                if (line.startsWith("username,password,name,age,role")) continue;

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

    //function for authenticating login and returning user role
    public static String checkLoginAndGetRole(String username, String password) {
        String filePath = "demo/output.csv";
        
        System.out.print("waiting for file to be read");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            System.out.print(" file has been read");
            String line;
            while ((line = br.readLine()) != null) {
                // Skip the header
                if (line.startsWith("username,password,name,age,role")) continue;

                String[] credentials = line.split(",");
                if (credentials.length < 4) { // Validate array length
                    System.err.println("Malformed line: " + line);
                    continue; // Skip malformed lines
                }
                String csvUsername = credentials[0];
                String csvPassword = credentials[1];
                String role = credentials.length > 4 ? credentials[4] : "user"; // Default to "user" if no role specified
                
                // If the username and password match, return the role
                if (csvUsername.equals(username) && csvPassword.equals(password)) {
                    return role;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Login failed
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

                if (line.startsWith("username,password,name,age,role")) continue;
                if (line.trim().isEmpty()) continue; // Skip empty lines

                String[] values = line.split(",");
                if (values.length >= 4) {
                    String username = values[0];
                    String password = values[1];
                    String name = values[2];
                    String age = values[3];
                    String role = values.length > 4 ? values[4] : "user"; // Default to "user" if no role specified

                    // Create a new User object and add it to the list
                    someUsers.add(new User(username, password, name, age, role));
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
    String filePath = "demo/prescriptions.csv";

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
        String filePath = "demo/prescriptions.csv";
        
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
      System.out.print("createNewAccount Invoked");

        String filePath = "demo/output.csv";
        
            System.out.print("haven't read file yet");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
                System.out.print("file to write is found");
                String newPrescriptionLine = usernameIn + "," + passwordIn + "," + nameIn + "," + AgeIn + ",user";
                bw.write(newPrescriptionLine);
                bw.newLine();
            
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    //function for updating user role
    public static boolean updateUserRole(String username, String newRole) {
        String filePath = "demo/output.csv";
        ObservableList<User> users = loadUserData(filePath);
        boolean updated = false;

        // Update the user's role in the list
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                user.setRole(newRole);
                updated = true;
                break;
            }
        }

        if (updated) {
            // Rewrite the entire CSV file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                // Write header
                bw.write("username,password,name,age,role");
                bw.newLine();

                // Write all user data
                for (User user : users) {
                    String line = user.getUsername() + "," + user.getPassword() + "," + 
                                 user.getName() + "," + user.getAge() + "," + user.getRole();
                    bw.write(line);
                    bw.newLine();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
        //return whether username exists or not if exists return false else true if we write
    
}








