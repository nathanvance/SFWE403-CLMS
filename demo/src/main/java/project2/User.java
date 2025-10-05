package project2;

public class User {
    private String username;
    private String password;
    private String name;
    private String age;

    /* Added for lockout and roles */
    // number of consecutive failed login attempts
    private int failedAttempts = 0;
    // Account lockout time
    private long lockedUntil = 0L;
    // User role ("manager", "staff", "patient")
    private String role = "staff"; // default role is "staff"

    // Getters and setters for new fields
    public int getFailedAttempts() {
        return failedAttempts;
    }
    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    public long getLockedUntil() {
        return lockedUntil;
    }
    public void setLockedUntil(long lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAccountLocked() {
        return lockedUntil > System.currentTimeMillis();
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }
    
    public void resetFailures() {
        this.failedAttempts = 0;
        this.lockedUntil = 0L;
    }

    //TO DO: add a user access field for user permissions. 
    //Before adding a prescription we need to make sure the account is created first and in the list
    //We need to have a medications class with fields for quantity, drug name...
    //^We can create a new prescription easier by clicking on the medication we want and enter the quantity
    //make sure we check that quantity entered does not exceed quantity availble

    // Constructor
    public User(String username, String password, String name, String age) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.age = age;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    

}

