package project2

import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

object LoginManager {
    private const val MAX_ATTEMPTS = 5

    private val loginHolder = File("demo/src/data/user_pass.csv")
    private val failuresFile = File("demo/src/data/login_failures.csv")
    private val adminNotifyLog = File("demo/src/data/admin_notifications.log")

    @JvmStatic
    val loginFile: File
        get() = loginHolder

    // username: stored hash (iterations:salt:hash)
    private var logins: MutableMap<String, String> = parseFile()

    // username: locked?
    private var locked: MutableMap<String, Boolean> = parseLocked()

    // username: failure info
    private var failures: MutableMap<String, FailureInfo> = parseFailures()

    data class FailureInfo(var attempts: Int = 0, var lastFailedAt: String? = null)

    @JvmStatic
    fun register(username: String, password: String): Boolean {
        if (logins.containsKey(username)) {
            println("Username '$username' already exists!")
            return false
        }
        val hash = PasswordHash.hashPWD(password.toCharArray())
        // keep your CSV structure: username,hash,Role
        FileUtils.appendToFile(loginHolder, mutableListOf("$username,$hash,Patient"))
        // init in-memory state
        logins = parseFile()
        locked[username] = false
        failures[username] = FailureInfo(0, null)
        persistFailures()
        println("Successfully registered user `$username`")
        return true
    }

    /**
     * Returns:
     *  - "true"   -> login succeeded
     *  - "false"  -> bad credentials (not yet locked)
     *  - "locked" -> account is locked
     */
    @JvmStatic
    fun verifyLogin(username: String, password: String): String {
        // Hard lock check (persisted in CSV)
        if (locked[username] == true) return "locked"

        val stored = logins[username] ?: run {
            // For non-existent usernames, don't reveal—treat as a failure but don't create records
            return "false"
        }

        val ok = PasswordHash.verifyPWD(password.toCharArray(), stored)
        if (ok) {
            resetFailures(username)
            return "true"
        } else {
            val info = failures.getOrPut(username) { FailureInfo() }
            info.attempts += 1
            info.lastFailedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            persistFailures()

            if (info.attempts >= MAX_ATTEMPTS) {
                lockAccount(username)
                return "locked"
            }
            return "false"
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

    /** Admin action: unlock a user from admin GUI */
    fun unlockUser(username: String): Boolean {
        val updated = updateUserCsvLine(username) { parts ->
            // parts: [username, hash, role, (maybe LOCKED ACCOUNT)]
            parts.filterNot { it.equals("LOCKED ACCOUNT", ignoreCase = true) }
        }
        if (!updated) return false
        locked[username] = false
        resetFailures(username)
        persistFailures()
        return true
    }

    /** Admin UI to see current attempts */
    fun getAttempts(username: String): Int = failures[username]?.attempts ?: 0

    private fun resetFailures(username: String) {
        val info = failures.getOrPut(username) { FailureInfo() }
        info.attempts = 0
        info.lastFailedAt = null
        persistFailures()
    }

    private fun lockAccount(username: String) {
        // Persist lock by appending ",LOCKED ACCOUNT" to the user row in user_pass.csv
        val updated = updateUserCsvLine(username) { parts ->
            if (parts.any { it.equals("LOCKED ACCOUNT", ignoreCase = true) }) parts
            else parts + "LOCKED ACCOUNT"
        }
        if (updated) {
            locked[username] = true
            // Log/notify admin (replace with email/queue later)
            val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            adminNotifyLog.parentFile?.mkdirs()
            adminNotifyLog.appendText("$now | LOCKOUT | user=$username\n")
        }
    }

    @JvmStatic
    fun refreshLogins() {
        logins = parseFile()
        // (locked/attempts don’t need refresh for a password change)
    }

    private fun parseFile(): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        if (!loginHolder.exists()) return map

        loginHolder.readLines().forEach { line ->
            if (line.isBlank()) return@forEach
            if (line.startsWith("username,hash")) return@forEach

            val parts = line.split(",")
            if (parts.size < 2) return@forEach
            val user = parts[0].trim()
            val hash = parts[1].trim()
            map[user] = hash
        }
        return map
    }

    private fun parseLocked(): MutableMap<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()
        if (!loginHolder.exists()) return map

        loginHolder.readLines().forEach { line ->
            if (line.isBlank()) return@forEach
            if (line.startsWith("username,hash")) return@forEach
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
                add("${u},${info.attempts},${info.lastFailedAt ?: ""}")
            }
        }
        failuresFile.writeText(lines.joinToString(System.lineSeparator()))
    }

    /** Replace the CSV row for a username using transformer */
    private fun updateUserCsvLine(username: String, transform: (List<String>) -> List<String>): Boolean {
        if (!loginHolder.exists()) return false
        val lines = loginHolder.readLines().toMutableList()
        var changed = false

        for (i in lines.indices) {
            val line = lines[i]
            if (line.isBlank() || line.startsWith("username,hash")) continue
            val parts = line.split(",")
            if (parts.isEmpty()) continue
            val user = parts[0].trim()
            if (user.equals(username, ignoreCase = false)) {
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
            // refresh caches
            logins = parseFile()
            locked = parseLocked()
        }
        return changed
    }
}