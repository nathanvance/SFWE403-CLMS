package project2

import java.io.File
import java.io.FileWriter

object FileUtils {

    fun appendToFile(file: File, data: List<String>) {
        FileWriter(file, true).use { writer ->
            data.forEach { writer.appendLine(it); }
        }
    }

    fun replaceInFile(file: File, toReplace: String, replaceString: String): Boolean {
        val content = file.readText();
        val newContent = content.replace(toReplace, replaceString);

        if (newContent != content) {
            file.writeText(content)
            return true;
        }

        return false;
    }

    fun findLine(file: File, query: String): String? {
        val lines = file.readLines();
        return lines.find { it.contains(query) };
    }

    fun appendLine(file: File, lineQuery: String, data: List<String>): Boolean {
        val lines = file.readLines().toMutableList();
        val index = lines.indexOfFirst { it.contains(lineQuery) };
        if (index == -1) return false;

        val appendText = data.joinToString(", ");
        lines[index] = lines[index] + " " + appendText;

        file.writeText(lines.joinToString("\n"));
        return true;
    }
}