package project2

open class UserKT(user: String, passHash: String) {
    protected val username = user
    protected val hash = passHash

    fun changePassword(new: String, old: String = hash): Boolean {
        // keep same iteration count as the old hash
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
}