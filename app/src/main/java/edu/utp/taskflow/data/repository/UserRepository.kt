package edu.utp.taskflow.data.repository

import android.util.Patterns
import edu.utp.taskflow.data.local.UserDao
import edu.utp.taskflow.data.local.UserEntity
import edu.utp.taskflow.model.AuthResult
import edu.utp.taskflow.model.User
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserRepository(
    private val dao: UserDao
) {

    suspend fun register(
        username: String,
        email: String,
        password: String
    ): AuthResult {
        val cleanUsername = username.trim().lowercase()
        val cleanEmail = email.trim().lowercase()

        if (cleanUsername.length < 3) {
            return AuthResult.Error("El usuario debe tener al menos 3 caracteres")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return AuthResult.Error("Ingresa un correo electrónico válido")
        }

        if (password.length < 6) {
            return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        }

        if (dao.findByUsername(cleanUsername) != null) {
            return AuthResult.Error("Ese usuario ya está registrado")
        }

        if (dao.findByEmail(cleanEmail) != null) {
            return AuthResult.Error("Ese correo ya está registrado")
        }

        val salt = generateSalt()

        val entity = UserEntity(
            username = cleanUsername,
            email = cleanEmail,
            passwordHash = hashPassword(password, salt),
            salt = salt.toHex()
        )

        val newId = dao.insert(entity).toInt()

        return AuthResult.Success(User(newId, cleanUsername))
    }

    // identifier puede ser el usuario o el correo
    suspend fun login(
        identifier: String,
        password: String
    ): AuthResult {
        val cleanIdentifier = identifier.trim().lowercase()

        val entity = dao.findByUsername(cleanIdentifier)
            ?: dao.findByEmail(cleanIdentifier)
            ?: return AuthResult.Error("Usuario o contraseña incorrectos")

        val attemptedHash = hashPassword(password, entity.salt.hexToBytes())

        val isValid = MessageDigest.isEqual(
            attemptedHash.toByteArray(),
            entity.passwordHash.toByteArray()
        )

        return if (isValid) {
            AuthResult.Success(User(entity.id, entity.username))
        } else {
            AuthResult.Error("Usuario o contraseña incorrectos")
        }
    }
}


private fun generateSalt(): ByteArray {
    val salt = ByteArray(16)
    SecureRandom().nextBytes(salt)
    return salt
}

private fun hashPassword(password: String, salt: ByteArray): String {
    val spec = PBEKeySpec(password.toCharArray(), salt, 10_000, 256)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    return factory.generateSecret(spec).encoded.toHex()
}

private fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

private fun String.hexToBytes(): ByteArray {
    return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}