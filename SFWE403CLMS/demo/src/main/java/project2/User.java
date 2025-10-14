package project2;

public class User {
    private String username;
    private String password;
    private String name;
    private String age;

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

