package project2

import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

object LoginManager {
    private const val MAX_ATTEMPTS = 5

    val loginHolder = File("src/data/user_pass.csv")
    private val failuresFile = File("src/data/login_failures.csv")
    private val adminNotifyLog = File("src/data/admin_notifications.log")

    data class LoginEntry(val hash: String, val role: String)

    enum class LoginStatus {
        SUCCESS,
        INVALID,
        LOCKED
    }

    data class LoginResult(val status: LoginStatus, val role: String? = null)

    @JvmStatic
    val loginFile: File
        get() = loginHolder

    private var logins: MutableMap<String, LoginEntry> = parseFile()
    private var locked: MutableMap<String, Boolean> = parseLocked()
    private var failures: MutableMap<String, FailureInfo> = parseFailures()

    data class FailureInfo(var attempts: Int = 0, var lastFailedAt: String? = null)

    @JvmStatic
    fun register(username: String, password: String): Boolean {
        if (logins.containsKey(username)) {
            println("Username '$username' already exists!")
            return false
        }

        val hash = PasswordHash.hashPWD(password.toCharArray())
        // keep CSV structure: username,hash,Role
        FileUtils.appendToFile(loginHolder, mutableListOf("$username,$hash,Patient"))
        logins = parseFile()
        locked[username] = false
        failures[username] = FailureInfo(0, null)
        persistFailures()
        println("Successfully registered user `$username`")
        return true
    }

    @JvmStatic
    fun verifyLogin(username: String, password: String): LoginResult {
        if (locked[username] == true) {
            return LoginResult(LoginStatus.LOCKED)
        }

        val entry = logins[username] ?: return LoginResult(LoginStatus.INVALID)
        val verified = PasswordHash.verifyPWD(password.toCharArray(), entry.hash)

        return if (verified) {
            LoginResult(LoginStatus.SUCCESS, entry.role)
        } else {
            // update failure counter
            val info = failures.getOrPut(username) { FailureInfo() }
            info.attempts++
            info.lastFailedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

            if (info.attempts >= MAX_ATTEMPTS) {
                lockAccount(username)
            }

            persistFailures()
            LoginResult(LoginStatus.INVALID)
        }
    }

    @JvmStatic
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

    @JvmStatic
    fun remainingAttempts(username: String): Int {
        val attempts = failures[username]?.attempts ?: 0
        val rem = MAX_ATTEMPTS - attempts
        return if (locked[username] == true) 0 else rem.coerceAtLeast(0)
    }

    fun unlockUser(username: String): Boolean {
        val updated = updateUserCsvLine(username) { parts ->
            parts.filterNot { it.equals("LOCKED ACCOUNT", ignoreCase = true) }
        }
        if (!updated) return false
        locked[username] = false
        resetFailures(username)
        persistFailures()
        return true
    }

    fun getAttempts(username: String): Int = failures[username]?.attempts ?: 0

    fun resetFailures(username: String) {
        val info = failures.getOrPut(username) { FailureInfo() }
        info.attempts = 0
        info.lastFailedAt = null
        persistFailures()
    }

    private fun lockAccount(username: String) {
        val updated = updateUserCsvLine(username) { parts ->
            if (parts.any { it.equals("LOCKED ACCOUNT", ignoreCase = true) }) parts
            else parts + "LOCKED ACCOUNT"
        }
        if (updated) {
            locked[username] = true
            val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            adminNotifyLog.parentFile?.mkdirs()
            adminNotifyLog.appendText("$now | LOCKOUT | user=$username\n")
        }
    }

    @JvmStatic
    fun refreshLogins() {
        logins = parseFile()
    }

    private fun parseFile(): MutableMap<String, LoginEntry> {
        if (!loginHolder.exists()) return mutableMapOf()
        val lines = loginHolder.readLines()
        val map = mutableMapOf<String, LoginEntry>()

        for (line in lines) {
            if (line.startsWith("username,hash") || line.isBlank()) continue

            val parts = line.split(",")
            if (parts.size < 2) continue

            val username = parts[0].trim()
            val hash = parts[1].trim()
            val role = parts.getOrNull(2)?.trim()?.takeIf { it.isNotEmpty() && it != "LOCKED ACCOUNT" } ?: "Patient"

            map[username] = LoginEntry(hash, role)
        }
        return map
    }

    private fun parseLocked(): MutableMap<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()
        if (!loginHolder.exists()) return map

        loginHolder.readLines().forEach { line ->
            if (line.isBlank() || line.startsWith("username,hash")) return@forEach
            val parts = line.split(",")
            val user = parts.firstOrNull()?.trim() ?: return@forEach
            map[user] = parts.any { it.equals("LOCKED ACCOUNT", ignoreCase = true) }
        }
        return map
    }

    private fun parseFailures(): MutableMap<String, FailureInfo> {
        val map = mutableMapOf<String, FailureInfo>()
        if (!failuresFile.exists()) return map

        failuresFile.readLines().forEachIndexed { idx, raw ->
            if (raw.isBlank()) return@forEachIndexed
            if (idx == 0 && raw.startsWith("username,attempts")) return@forEachIndexed
            val parts = raw.split(",")
            if (parts.size < 2) return@forEachIndexed
            val user = parts[0].trim()
            val attempts = parts[1].trim().toIntOrNull() ?: 0
            val last = parts.getOrNull(2)?.trim()?.ifBlank { null }
            map[user] = FailureInfo(attempts, last)
        }
        return map
    }

    private fun persistFailures() {
        failuresFile.parentFile?.mkdirs()
        val lines = buildList {
            add("username,attempts,lastFailedAt")
            failures.forEach { (u, info) ->
                add("$u,${info.attempts},${info.lastFailedAt ?: ""}")
            }
        }
        failuresFile.writeText(lines.joinToString(System.lineSeparator()))
    }

    private fun updateUserCsvLine(username: String, transform: (List<String>) -> List<String>): Boolean {
        if (!loginHolder.exists()) return false
        val lines = loginHolder.readLines().toMutableList()
        var changed = false

        for (i in lines.indices) {
            val line = lines[i]
            if (line.isBlank() || line.startsWith("username,hash")) continue
            val parts = line.split(",")
            val user = parts[0].trim()
            if (user == username) {
                val newParts = transform(parts)
                val newLine = newParts.joinToString(",")
                if (newLine != line) {
                    lines[i] = newLine
                    changed = true
                }
                break
            }
        }

        if (changed) {
            loginHolder.writeText(lines.joinToString(System.lineSeparator()))
            logins = parseFile()
            locked = parseLocked()
        }
        return changed
    }

    @JvmStatic
    fun updateUserType(username: String, newType: String): Boolean {
        if (!loginHolder.exists()) return false

        val targetLine = FileUtils.findLine(loginHolder, "$username,") ?: return false
        val parts = targetLine.split(",").toMutableList()
        if (parts.size < 3) return false

        parts[2] = newType
        val newLine = parts.joinToString(",")
        val success = FileUtils.replaceInFile(loginHolder, targetLine, newLine)

        if (success) {
            println("Updated $username role to $newType")
            refreshLogins() // reload in-memory data
        }

        return success
    }
}
