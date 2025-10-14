package project2

import java.io.File;
import java.io.FileWriter;

object LoginManager {
    private val loginHolder = File("demo/src/data/user_pass.csv")
    private var logins = parseFile();

    fun register(username: String, password: String): Boolean {
        if (logins.containsKey(username)) {
            println("Username '$username' already exists!") //Replace with error messages in registration window later
            return false;
        }

        val hash = PasswordHash.hashPWD(password.toCharArray());
        appendToFile(username, hash);
        logins = parseFile();

        println("Successfully register user `$username`");
        return true;
    }

    fun verifyLogin(username: String, password: String): Boolean {
        val stored = logins[username] ?: return false;

        return PasswordHash.verifyPWD(password.toCharArray(), stored);
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

    private fun parseFile(file: File = loginHolder): MutableMap<String, String> {
        val lines = file.readLines();
        val map = mutableMapOf<String, String>();

        for (line in lines) {
            if (line.contains("username,hash")) continue;

            val pair = line.split(",");
            if (pair.size != 2) continue;

            map[pair[0]] = pair[1];
        }

        return map;
    }

    private fun appendToFile(username: String, hash: String) {
        FileWriter(loginHolder, true).use { writer ->
            writer.appendLine("$username,$hash")
        }
    }
}