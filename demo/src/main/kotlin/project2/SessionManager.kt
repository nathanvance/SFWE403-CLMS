package project2

object SessionManager {
    var currentUsername: String? = null
    var currentRole: String? = null

    fun startSession(username: String, role: String?) {
        LoginManager.resetFailures(username)
        currentUsername = username
        currentRole = role
    }

    fun clear() {
        currentUsername = null
        currentRole = null
    }

}

