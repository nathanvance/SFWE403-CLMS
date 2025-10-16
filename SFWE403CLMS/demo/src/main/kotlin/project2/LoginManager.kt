package project2

import java.io.File;
import java.io.FileWriter;

object LoginManager {
    val loginHolder = File("demo/src/data/user_pass.csv")
    private var logins = parseFile();
    private var locked = parseLocked();

    fun register(username: String, password: String): Boolean {
        if (logins.containsKey(username)) {
            println("Username '$username' already exists!") //Replace with error messages in registration window later
            return false;
        }

        val hash = PasswordHash.hashPWD(password.toCharArray());
        FileUtils.appendToFile(loginHolder, mutableListOf("$username,$hash,Patient"));
        logins = parseFile();

        println("Successfully registered user `$username`");
        return true;
    }

    fun verifyLogin(username: String, password: String): String {
        if (locked[username] == true) return "locked"
        val stored = logins[username] ?: return "false";

        return if (PasswordHash.verifyPWD(password.toCharArray(), stored)) "true" else "false";
    }

    fun passwordChecks(username: String, password: String, confirmation: String): List<String> {
        return buildList {
            if (logins.containsKey(username)) add("Username already exists!")
            if (!password.contains(Regex("[0-9]"))) add("Password must contain a number! (0-9)")
            if (!password.contains(Regex("[A-Z]"))) add("Password must contain an uppercase letter!")
            if (!password.contains(Regex("[a-z]"))) add("Password must contain a lowercase letter!")
            if (!password.contains(Regex("[^A-Za-z0-9]"))) add("Password must contain a special character!")
            if (password != confirmation) add("Password and confirmation do not match!")
        }
    }

    private fun parseFile(): MutableMap<String, String> {
        val lines = loginHolder.readLines();
        val map = mutableMapOf<String, String>();

        for (line in lines) {
            if (line.contains("username,hash")) continue;

            val pair = line.split(",");
            if (pair.size < 2) continue;

            map[pair[0]] = pair[1];
        }

        return map;
    }

    private fun parseLocked(): MutableMap<String, Boolean> {
        val lines = loginHolder.readLines();
        val map = mutableMapOf<String, Boolean>();

        for (line in lines) {
            if (line.contains("username,hash")) continue;

            val parts = line.split(",");
            if ("LOCKED ACCOUNT" !in parts) {
                map[parts[0]] = false
                continue;
            };
            map[parts[0]] = true;
        }

        return map;
    }

}
