package project2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;



public class BackEnd {

    /* Account Recovery */
    // Lockout configuration
    private static final int MAX_FAILED_ATTEMPTS = 5; // Attempts before lockout
    private static final long LOCKOUT_MINUTES = 5; // Lockout duration in minutes

    // Track failed login attempts and end time per username
    private static final Map<String, Integer> FAILED_ATTEMPTS = new ConcurrentHashMap<>();
    private static final Map<String, Long> LOCKED_UNTIL = new ConcurrentHashMap<>();

    // Reuse existing CSV path (change if needed)
    private static final String CSV_FILE_PATH = "C:\\Users\\henry\\Downloads\\Project2Java\\demo\\src\\main\\java\\project2\\output.csv";

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
        
    public static boolean isLocked(String username) {
        String key = username.toLowerCase();
        long until = LOCKED_UNTIL.getOrDefault(key, 0L);
        return until > System.currentTimeMillis();
    }

    // Find user by username
    public static Optional<User> findUserByUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            // Skip header
            br.readLine();

            while ((line = br .readLine()) != null) {
                if (line.startsWith("username,password,name,age")) continue;
                String[] cols = line.split(",");
                if (cols.length < 4 && cols[0].equalsIgnoreCase(username)) {
                    return Optional.of(new User(cols[0], cols[1], cols[2], cols[3]));
                }
                }
    
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    // Check if username exists (for account lockout)
    public static boolean checkLoginWithLockout(String username, String password) {
        String key = username.toLowerCase();

        // 1) deny if currently locked
        if (isLocked(username)) {
            System.out.println("[LOGIN] user locked: " + username);
            return false;
        }

        // 2) delegate to your existing checkLogin(...)
        boolean ok = checkLogin(username, password);
        if (ok) {
            // success → clear any failures/lock
            FAILED_ATTEMPTS.remove(key);
            LOCKED_UNTIL.remove(key);
            return true;
        }

        // 3) wrong password → bump failures and maybe lock
        int attempts = FAILED_ATTEMPTS.getOrDefault(key, 0) + 1;
        FAILED_ATTEMPTS.put(key, attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            long until = System.currentTimeMillis() + LOCKOUT_MINUTES * 20_000L;
            LOCKED_UNTIL.put(key, until);
            System.out.println("[LOGIN] lockout triggered for " + username);
        }
        return false;
    }

    public static boolean managerUnlockUser(String managerUsername, String targetUsername) {
        String key = targetUsername.toLowerCase();
        FAILED_ATTEMPTS.remove(key);
        LOCKED_UNTIL.remove(key);
        System.out.println("[UNLOCK] manager=" + managerUsername + " unlocked user=" + targetUsername);
        return true;
    }

    public static boolean managerResetPassword(String managerUsername, String targetUsername, String tempPassword) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("username,password,name,age")) {
                    lines.add(line); // keep header
                    continue;
                }
                String[] cols = line.split(",");
                if (cols.length >= 4 && cols[0].equalsIgnoreCase(targetUsername)) {
                    cols[1] = tempPassword;              // set new password
                    line = String.join(",", cols);
                    updated = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!updated) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH, false))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // clear lock state as part of reset
        String key = targetUsername.toLowerCase();
        FAILED_ATTEMPTS.remove(key);
        LOCKED_UNTIL.remove(key);

        System.out.println("[RESET] manager=" + managerUsername + " set temp password for " + targetUsername);
        return true;
    }

    public static int remainingAttempts(String username) {
        String key = username.toLowerCase();
        int attempts = FAILED_ATTEMPTS.getOrDefault(key, 0);
        int left = MAX_FAILED_ATTEMPTS - attempts;
        return Math.max(0, left);
    }

    public static long lockoutSecondsRemaining(String username) {
        String key = username.toLowerCase();
        long until = LOCKED_UNTIL.getOrDefault(key, 0L);
        long diff = until - System.currentTimeMillis();
        return diff > 0 ? diff / 1000 : 0;
    }


    //return whether username exists or not if exists return false else true if we write
    
}








