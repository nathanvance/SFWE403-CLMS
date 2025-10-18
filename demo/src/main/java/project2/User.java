package project2;

public class User {
    private String username;
    private String password;
    private String name;
    private String age;
    private String role;

    //TO DO: add a user access field for user permissions. 
    //Before adding a prescription we need to make sure the account is created first and in the list
    //We need to have a medications class with fields for quantity, drug name...
    //^We can create a new prescription easier by clicking on the medication we want and enter the quantity
    //make sure we check that quantity entered does not exceed quantity availble

    // Constructor
    public User(String username, String password, String name, String age, String role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.age = age;
        this.role = role;
    }
    
    // Constructor for backward compatibility (defaults to "user" role)
    public User(String username, String password, String name, String age) {
        this(username, password, name, age, "user");
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}

