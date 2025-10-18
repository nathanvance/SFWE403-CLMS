package project2

import java.io.File
import java.io.FileWriter
import java.io.RandomAccessFile

object FileUtils {

    private fun ensureReady(file: File) {
        file.parentFile?.mkdirs()
        if (!file.exists()) file.createNewFile()
    }

    private fun needsLeadingNewline(file: File): Boolean {
        if (!file.exists()) return false
        val len = file.length()
        if (len == 0L) return false
        RandomAccessFile(file, "r").use { raf ->
            raf.seek(len - 1)
            val last = raf.read()
            // treat both \n and \r as newline endings
            return last != '\n'.code && last != '\r'.code
        }
    }

    fun appendToFile(file: File, data: List<String>) {
        ensureReady(file)
        FileWriter(file, true).use { w ->
            if (needsLeadingNewline(file)) w.append(System.lineSeparator())
            data.forEach { w.append(it).append(System.lineSeparator()) }
        }
    }

    fun replaceInFile(file: File, toReplace: String, replaceString: String): Boolean {
        if (!file.exists()) return false
        val content = file.readText()
        val newContent = content.replace(toReplace, replaceString)
        if (newContent == content) return false
        file.writeText(newContent)   // FIX: write the updated content
        return true
    }

    fun findLine(file: File, query: String): String? {
        if (!file.exists()) return null
        return file.readLines().find { it.contains(query) }
    }

    fun appendLine(file: File, lineQuery: String, data: List<String>): Boolean {
        if (!file.exists()) return false
        val lines = file.readLines().toMutableList()
        val index = lines.indexOfFirst { it.contains(lineQuery) }
        if (index == -1) return false

        // CSV-safe: join with commas, no extra spaces
        val appendText = data.joinToString(",")
        lines[index] = lines[index] + "," + appendText

        file.writeText(lines.joinToString(System.lineSeparator()))
        return true
    }

    fun deleteLine(file: File, query: String): Boolean {
        if (!file.exists()) return false

        val lines = file.readLines()
        val filtered = lines.filterNot { it.contains(query) }

        if (filtered.size == lines.size) return false

        file.writeText(filtered.joinToString("\n"))
        return true
    }
}