package project2

import java.io.File

object UserManager {
    private val user_data = File("demo/src/data/user_pass.csv")
    private val users = parseFile();

    private fun parseFile(): MutableMap<String, UserKT> {
        val map = mutableMapOf<String, UserKT>();

        if (!user_data.exists()) {
            println("⚠️  Warning: user_pass.csv not found at ${user_data.path}")
            return map
        }

        val lines = user_data.readLines();

        for (line in lines) {
            val pair = line.split(",");
            if (pair.size < 3) continue;
            val type = pair[2];

            when (type) {
                "Patient" -> map[pair[0]] = Patient(pair[0], pair[1]);
                "Admin" -> map[pair[0]] = Admin(pair[0], pair[1]);
                "Pharmacist" -> map[pair[0]] = Pharmacist(pair[0], pair[1]);
            }
        }
        return map;
    }

    fun removeUser(username: String): Boolean {
        return users.remove(username) != null
    }

    fun getUser(username: String): UserKT? = users[username]

    fun getAllUsers(): Collection<UserKT> {
        return users.values
    }

}