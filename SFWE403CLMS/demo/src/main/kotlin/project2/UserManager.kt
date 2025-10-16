package project2

import java.io.File

object UserManager {
    private val user_data = File("demo/src/data/user_pass.csv")
    private val users = parseFile();

    private fun parseFile() {
        val lines = user_data.readLines();
        val map = mutableMapOf<String, UserKT>();

        for (line in lines) {
            var pair = line.split(",");
        }
    }
}