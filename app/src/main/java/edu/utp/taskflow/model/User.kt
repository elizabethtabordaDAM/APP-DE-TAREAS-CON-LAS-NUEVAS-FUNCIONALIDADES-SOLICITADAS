package edu.utp.taskflow.model

data class User(
    val id: Int,
    val username: String
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}