package project2


open class UserKT (user: String, passHash: String){
    protected val username = user;
    protected val hash = passHash;

    fun changePassword(new: String, old: String = hash): Boolean {
        val newHash = PasswordHash.hashPWD(new.toCharArray(), old.split(":")[0].toInt());
        if (newHash == old) return false;

        FileUtils.replaceInFile(LoginManager.loginHolder, "$username,$hash", "$username,$hash");
        return true;
    }

}