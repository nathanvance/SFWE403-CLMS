package project2

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHash {
    private val secureRandom = SecureRandom();
    private const val SALT_BYTES = 16;
    private const val HASH_BYTES = 32;
    private const val DEFAULT_IT = 100_000;

    fun hashPWD(password: CharArray, iterations: Int = DEFAULT_IT) : String {
        val salt = ByteArray(SALT_BYTES);
        secureRandom.nextBytes(salt);

        val hash = pbkdf2(password, salt, iterations, SALT_BYTES);
        val saltB64 = Base64.getEncoder().encodeToString(salt);
        val hashB64 = Base64.getEncoder().encodeToString(hash);

        password.fill('\u0000');
        return "$iterations:$saltB64:$hashB64";
    }

    fun verifyPWD(password: CharArray, stored: String): Boolean {
        val parts = stored.split(':');
        if (parts.size != 3) return false;

        val iterations = parts[0].toIntOrNull() ?: return false;

        val salt = Base64.getDecoder().decode(parts[1]);
        val hash = Base64.getDecoder().decode(parts[2]);
        val computed = pbkdf2(password, salt, iterations, hash.size);

        password.fill('\u0000');

        return slowEquals(hash, computed);
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations : Int, salt_bytes: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, salt_bytes * 8);
        val skf = SecretKeyFactory.getInstance(("PBKDF2WithHmacSHA256"));
        val key = skf.generateSecret(spec).encoded;
        spec.clearPassword();

        return key;
    }

    private fun slowEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false;
        var diff = 0;

        for (i in a.indices) {
            diff = diff or (a[i].toInt() xor b[i].toInt());
        }

        return diff == 0;
    }

}