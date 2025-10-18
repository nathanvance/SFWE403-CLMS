package project2

import java.io.File

object LoginManager {
    val loginHolder = File("SFWE403CLMS/demo/src/data/user_pass.csv")
    private var logins = parseFile()
    private var locked = parseLocked()

    data class LoginEntry(val hash: String, val role: String)

    enum class LoginStatus {
        SUCCESS,
        INVALID,
        LOCKED
    }

    data class LoginResult(val status: LoginStatus, val role: String? = null)

    fun register(username: String, password: String): Boolean {
        if (logins.containsKey(username)) {
            println("Username '$username' already exists!") //Replace with error messages in registration window later
            return false
        }

        val hash = PasswordHash.hashPWD(password.toCharArray())
        FileUtils.appendToFile(loginHolder, mutableListOf("$username,$hash,Patient"))
        logins = parseFile()

        println("Successfully registered user `$username`")
        return true
    }

    fun verifyLogin(username: String, password: String): LoginResult {
        if (locked[username] == true) {
            return LoginResult(LoginStatus.LOCKED)
        }

        val entry = logins[username] ?: return LoginResult(LoginStatus.INVALID)
        val verified = PasswordHash.verifyPWD(password.toCharArray(), entry.hash)

        return if (verified) {
            LoginResult(LoginStatus.SUCCESS, entry.role)
        } else {
            LoginResult(LoginStatus.INVALID)
        }
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

    private fun parseFile(): MutableMap<String, LoginEntry> {
        val lines = loginHolder.readLines()
        val map = mutableMapOf<String, LoginEntry>()

        for (line in lines) {
            if (line.contains("username,hash")) continue

            val parts = line.split(",")
            if (parts.size < 2) continue

            val username = parts[0]
            val hash = parts[1]
            val role = parts.getOrNull(2) ?: "Patient"

            map[username] = LoginEntry(hash, role)
        }

        return map
    }

    private fun parseLocked(): MutableMap<String, Boolean> {
        val lines = loginHolder.readLines()
        val map = mutableMapOf<String, Boolean>()

        for (line in lines) {
            if (line.contains("username,hash")) continue

            val parts = line.split(",")
            if ("LOCKED ACCOUNT" !in parts) {
                map[parts[0]] = false
                continue
            }
            map[parts[0]] = true
        }

        return map
    }

}
