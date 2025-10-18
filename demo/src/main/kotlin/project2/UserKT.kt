package project2

import java.io.File

open class UserKT(user: String, passHash: String) {
    val username = user
    val hash = passHash

    fun changePassword(new: String, old: String = hash): Boolean {
        val iterations = old.split(":")[0].toInt()
        val newHash = PasswordHash.hashPWD(new.toCharArray(), iterations)
        if (newHash == old) return false

        val changed = FileUtils.replaceInFile(
            file = LoginManager.loginFile,
            toReplace = "$username,$old",
            replaceString = "$username,$newHash"
        )

        if (changed) {
            // make sure LoginManager sees the new hash without restarting
            LoginManager.refreshLogins()
        }
        return changed
    }

    fun deleteAccount(): Boolean {
        val removed = UserManager.removeUser(username)

        val file = File("src/data/user_pass.csv")
        val deletedFromFile = FileUtils.deleteLine(file, "$username,")

        return removed && deletedFromFile
    }
}
