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
        LOCKED,
        RESET_REQUIRED
    }

    enum class ChangePwdResult { OK, BAD_CURRENT, MISMATCH, WEAK, SAME_AS_OLD, ERROR }

    data class LoginResult(val status: LoginStatus, val role: String? = null)

    @JvmStatic
    fun getRole(username: String): String? = logins[username]?.role

    @JvmStatic
    fun getAllUsers(): Map<String, LoginEntry> = logins.toMap()

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
        UserManager.refreshUsers()
        return true
    }

    @JvmStatic
    fun verifyLogin(username: String, password: String): LoginResult {
        if (locked[username] == true) {
            return LoginResult(LoginStatus.LOCKED)
        }

        val csvLine = FileUtils.findLine(loginHolder, "$username,") ?: return LoginResult(LoginStatus.INVALID)

        if (csvLine.split(",").any { it.equals("DELETED ACCOUNT", ignoreCase = true) }) {
            return LoginResult(LoginStatus.INVALID)
        }

        val entry = logins[username] ?: return LoginResult(LoginStatus.INVALID)
        val verified = PasswordHash.verifyPWD(password.toCharArray(), entry.hash)

        return if (verified) {
            // If admin has marked this account for reset, force the reset flow
            val needsReset = csvLine.split(",").any { it.equals("RESET REQUIRED", ignoreCase = true) }
            if (needsReset) LoginResult(LoginStatus.RESET_REQUIRED, entry.role)
            else LoginResult(LoginStatus.SUCCESS, entry.role)
        } else {
            // Unchanged failure tracking
            val info = failures.getOrPut(username) { FailureInfo() }
            info.attempts++
            info.lastFailedAt = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
            if (info.attempts >= MAX_ATTEMPTS) {
                lockAccount(username)
            }
            persistFailures()
            LoginResult(LoginStatus.INVALID)
        }
    }

    @JvmStatic
    fun verifyPassword(username: String, plain: String): Boolean {
        val entry = logins[username] ?: return false
        return PasswordHash.verifyPWD(plain.toCharArray(), entry.hash)
    }

    @JvmStatic
    fun changePasswordSelf(username: String, current: String, newPwd: String, confirm: String): ChangePwdResult {
        val entry = logins[username] ?: return ChangePwdResult.ERROR

        // Verify current password
        if (!PasswordHash.verifyPWD(current.toCharArray(), entry.hash)) {
            return ChangePwdResult.BAD_CURRENT
        }

        // New password must be different
        if (PasswordHash.verifyPWD(newPwd.toCharArray(), entry.hash)) {
            return ChangePwdResult.SAME_AS_OLD
        }

        // Confirm
        if (newPwd != confirm) return ChangePwdResult.MISMATCH
        val strong = newPwd.any(Char::isUpperCase) &&
                newPwd.any(Char::isLowerCase) &&
                newPwd.any(Char::isDigit)     &&
                newPwd.any { !it.isLetterOrDigit() }
        if (!strong) return ChangePwdResult.WEAK

        // 4) write hash to CSV
        val newHash = PasswordHash.hashPWD(newPwd.toCharArray())
        if (!updateUserHashInCsv(username, newHash)) return ChangePwdResult.ERROR

        // 5) refresh memory + clear failures
        refreshLogins()
        resetFailures(username)
        return ChangePwdResult.OK
    }

    @JvmStatic
    fun deleteSelf(username: String, passwordPlain: String, reason: String = "Self-request"): Boolean {
        // must exist and password must be correct
        val entry = logins[username] ?: return false
        if (!PasswordHash.verifyPWD(passwordPlain.toCharArray(), entry.hash)) return false
        if (!loginHolder.exists()) return false

        val targetLine = FileUtils.findLine(loginHolder, "$username,") ?: return false
        val parts = targetLine.split(",").toMutableList()

        // already deleted?
        if (parts.any { it.equals("DELETED ACCOUNT", ignoreCase = true) }) return true

        // archive
        val deletedCsv = java.io.File("src/data/deleted_users.csv")
        deletedCsv.parentFile?.mkdirs()
        if (!deletedCsv.exists()) {
            deletedCsv.writeText("username,hash,role,deletedAt,deletedBy,reason\n")
        }
        val now = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        val role = parts.getOrNull(2)?.trim().takeUnless { it.isNullOrBlank() } ?: "Patient"
        val archiveLine = listOf(username, parts.getOrNull(1)?.trim().orEmpty(), role, now, username, reason.replace(",", " ")).joinToString(",")
        FileUtils.appendToFile(deletedCsv, listOf(archiveLine))

        // mark as deleted
        val updated = (parts + "DELETED ACCOUNT").joinToString(",")
        val ok = FileUtils.replaceInFile(loginHolder, targetLine, updated)
        if (!ok) return false

        locked[username] = true
        resetFailures(username)
        refreshLogins()
        locked = parseLocked()

        adminNotifyLog.parentFile?.mkdirs()
        adminNotifyLog.appendText("$now | DELETE_SELF | user=$username reason=$reason\n")
        return true
    }

    @JvmStatic
    fun isDeleted(username: String): Boolean {
        val line = FileUtils.findLine(loginHolder, "$username,") ?: return false
        return line.split(",").any { it.equals("DELETED ACCOUNT", ignoreCase = true) }
    }

    @JvmStatic
    fun restoreUser(performedBy: String, targetUsername: String): Boolean {
        // Admin
        val actorRole = logins[performedBy]?.role ?: return false
        val isAdmin = actorRole.equals("Admin", true) || actorRole.equals("Manager", true)
        if (!isAdmin) return false

        val line = FileUtils.findLine(loginHolder, "$targetUsername,") ?: return false
        val parts = line.split(",")
        if (parts.none { it.equals("DELETED ACCOUNT", true) }) return true // active

        // remove DELETED ACCOUNT, ensure RESET REQUIRED so user must change pwd on next login
        val restored = parts.filterNot { it.equals("DELETED ACCOUNT", true) }.toMutableList()
        if (restored.none { it.equals("RESET REQUIRED", true) }) restored += "RESET REQUIRED"

        val ok = FileUtils.replaceInFile(loginHolder, line, restored.joinToString(","))
        if (!ok) return false

        // unlock + refresh in-memory
        locked[targetUsername] = false
        resetFailures(targetUsername)
        refreshLogins()
        locked = parseLocked()

        val now = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        adminNotifyLog.parentFile?.mkdirs()
        adminNotifyLog.appendText("$now | RESTORE | user=$targetUsername by=$performedBy\n")
        return true
    }

    @JvmStatic
    fun deleteSelf(username: String, passwordPlain: String): Boolean =
        deleteSelf(username, passwordPlain, "Self-request")

    private fun updateUserHashInCsv(username: String, newHash: String): Boolean {
        if (!loginHolder.exists()) return false
        val lines = loginHolder.readLines().toMutableList()
        var changed = false

        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("$username,")) {
                // CSV columns: username,hash(iter:salt:hash),role[,LOCKED ACCOUNT]
                val parts = line.split(",").toMutableList()
                if (parts.size >= 2) {
                    parts[1] = newHash
                    lines[i] = parts.joinToString(",")
                    changed = true
                }
                break
            }
        }
        if (!changed) return false
        loginHolder.writeText(lines.joinToString(System.lineSeparator()))
        return true
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
    fun requirePasswordReset(performedBy: String, targetUsername: String): Boolean {
        val actorRole = logins[performedBy]?.role ?: return false
        val isAdmin = actorRole.equals("Admin", true) || actorRole.equals("Manager", true)
        if (!isAdmin) return false
        if (!loginHolder.exists()) return false

        val line = FileUtils.findLine(loginHolder, "$targetUsername,") ?: return false
        val parts = line.split(",")

        // If already deleted, do nothing
        if (parts.any { it.equals("DELETED ACCOUNT", true) }) return false

        // If already flagged, that's success for our purposes
        if (parts.any { it.equals("RESET REQUIRED", true) }) return true

        val updated = (parts + "RESET REQUIRED").joinToString(",")
        if (!FileUtils.replaceInFile(loginHolder, line, updated)) return false

        // Refresh caches and log
        refreshLogins()
        val now = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        adminNotifyLog.parentFile?.mkdirs()
        adminNotifyLog.appendText("$now | PASSWORD_RESET_INIT | user=$targetUsername by=$performedBy\n")
        return true
    }

    @JvmStatic
    fun isResetRequired(username: String): Boolean {
        val line = FileUtils.findLine(loginHolder, "$username,") ?: return false
        return line.split(",").any { it.equals("RESET REQUIRED", true) }
    }

    @JvmStatic
    fun isLocked(username: String): Boolean = locked[username] == true

    @JvmStatic
    fun changePasswordAfterAdminReset(username: String, newPwd: String, confirm: String): List<String> {
        // Must be a non-deleted user
        val entry = logins[username] ?: return listOf("User does not exist.")

        // Account must have been marked by admin
        val line = FileUtils.findLine(loginHolder, "$username,") ?: return listOf("Account not found.")
        val hasResetFlag = line.split(",").any { it.equals("RESET REQUIRED", ignoreCase = true) }
        if (!hasResetFlag) return listOf("No password reset is currently required.")

        // Do not allow reuse of the old password
        if (PasswordHash.verifyPWD(newPwd.toCharArray(), entry.hash)) {
            return listOf("New password cannot match the old password.")
        }

        val errs = buildList {
            if (!newPwd.contains(Regex("[0-9]"))) add("Password must contain a number! (0-9)")
            if (!newPwd.contains(Regex("[A-Z]"))) add("Password must contain an uppercase letter!")
            if (!newPwd.contains(Regex("[a-z]"))) add("Password must contain a lowercase letter!")
            if (!newPwd.contains(Regex("[^A-Za-z0-9]"))) add("Password must contain a special character!")
            if (newPwd != confirm) add("Password and confirmation do not match!")
        }
        if (errs.isNotEmpty()) return errs

        // Update the stored hash, keeping role and any flags
        val newHash = PasswordHash.hashPWD(newPwd.toCharArray())
        val ok = updateUserCsvLine(username) { parts ->
            val role = parts.getOrNull(2)?.trim().takeUnless { it.isNullOrBlank() } ?: "Patient"
            val flags = if (parts.size > 3) parts.drop(3) else emptyList()
            listOf(username, newHash, role) + flags
        }
        if (!ok) return listOf("Failed to update password on disk.")

        // Clear the RESET REQUIRED flag once the user has set a new password
        val updatedLine = FileUtils.findLine(loginHolder, "$username,") // get the new, post-update line
        if (updatedLine != null && updatedLine.split(",").any { it.equals("RESET REQUIRED", ignoreCase = true) }) {
            val cleared = updatedLine
                .split(",")
                .filterNot { it.equals("RESET REQUIRED", ignoreCase = true) }
                .joinToString(",")
            FileUtils.replaceInFile(loginHolder, updatedLine, cleared)
            refreshLogins()
        }

        val now = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        adminNotifyLog.appendText("$now | PASSWORD_RESET_COMPLETE | user=$username\n")
        return emptyList()
    }

    @JvmStatic
    fun deleteUser(performedBy: String, targetUsername: String, reason: String = "N/A"): Boolean {
        // Only Admin can delete, and don't allow deleting yourself
        val actorRole = logins[performedBy]?.role ?: return false
        val isAdmin = actorRole.equals("Admin", true) || actorRole.equals("Manager", true)
        if (!isAdmin || performedBy.equals(targetUsername, ignoreCase = true)) return false
        if (!loginHolder.exists()) return false

        val targetLine = FileUtils.findLine(loginHolder, "$targetUsername,") ?: return false
        val parts = targetLine.split(",").toMutableList()

        // Already deleted
        if (parts.any { it.equals("DELETED ACCOUNT", ignoreCase = true) }) return true

        // Archive current record
        val deletedCsv = java.io.File("src/data/deleted_users.csv")
        deletedCsv.parentFile?.mkdirs()
        if (!deletedCsv.exists()) {
            deletedCsv.writeText("username,hash,role,deletedAt,deletedBy,reason\n")
        }

        val now = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        val role = parts.getOrNull(2)?.trim().takeUnless { it.isNullOrBlank() } ?: "Patient"
        val archiveLine = listOf(targetUsername, parts.getOrNull(1)?.trim().orEmpty(), role, now, performedBy, reason.replace(",", " ")
        ).joinToString(",")
        FileUtils.appendToFile(deletedCsv, listOf(archiveLine))

        // Mark as deleted
        val updated = (parts + "DELETED ACCOUNT").joinToString(",")
        val ok = FileUtils.replaceInFile(loginHolder, targetLine, updated)
        if (!ok) return false

        locked[targetUsername] = true
        resetFailures(targetUsername)
        refreshLogins()
        locked = parseLocked()

        adminNotifyLog.parentFile?.mkdirs()
        adminNotifyLog.appendText("$now | DELETE | user=$targetUsername by=$performedBy reason=$reason\n")
        return true
    }

    @JvmStatic
    fun changePassword(username: String, currentPwd: String, newPwd: String, confirm: String): List<String> {
        val entry = logins[username] ?: return listOf("User does not exist.")

        // Must match current password
        if (!PasswordHash.verifyPWD(currentPwd.toCharArray(), entry.hash)) {
            return listOf("Current password is incorrect.")
        }
        // Do not allow the old password to be reused
        if (PasswordHash.verifyPWD(newPwd.toCharArray(), entry.hash)) {
            return listOf("New password cannot match the current password.")
        }

        val errs = buildList {
            if (!newPwd.contains(Regex("[0-9]"))) add("Password must contain a number! (0-9)")
            if (!newPwd.contains(Regex("[A-Z]"))) add("Password must contain an uppercase letter!")
            if (!newPwd.contains(Regex("[a-z]"))) add("Password must contain a lowercase letter!")
            if (!newPwd.contains(Regex("[^A-Za-z0-9]"))) add("Password must contain a special character!")
            if (newPwd != confirm) add("Password and confirmation do not match!")
        }
        if (errs.isNotEmpty()) return errs

        val newHash = PasswordHash.hashPWD(newPwd.toCharArray())

        // Write the new hash, keep role and any flags
        val ok = updateUserCsvLine(username) { parts ->
            val role = parts.getOrNull(2)?.trim().takeUnless { it.isNullOrBlank() } ?: "Patient"
            val flags = if (parts.size > 3) parts.drop(3) else emptyList()
            listOf(username, newHash, role) + flags
        }
        if (!ok) return listOf("Failed to update password on disk.")

        // If Admin had set RESET REQUIRED for this user, clear it
        val line = FileUtils.findLine(loginHolder, "$username,")
        if (line != null && line.split(",").any { it.equals("RESET REQUIRED", true) }) {
            val cleared = line.split(",").filterNot { it.equals("RESET REQUIRED", true) }.joinToString(",")
            FileUtils.replaceInFile(loginHolder, line, cleared)
            refreshLogins()
        }

        val now = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        adminNotifyLog.appendText("$now | PASSWORD_CHANGE | user=$username\n")
        return emptyList()
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

    @JvmStatic
    fun usernameExists(username: String): Boolean = logins.containsKey(username)
}
