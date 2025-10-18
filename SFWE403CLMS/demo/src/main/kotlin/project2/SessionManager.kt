package project2

object SessionManager {
    private var currentUsername: String? = null
    private var currentRole: String? = null

    fun startSession(username: String, role: String?) {
        currentUsername = username
        currentRole = role
    }

    fun clear() {
        currentUsername = null
        currentRole = null
    }

    fun getUsername(): String? = currentUsername

    fun getRole(): String? = currentRole
}

