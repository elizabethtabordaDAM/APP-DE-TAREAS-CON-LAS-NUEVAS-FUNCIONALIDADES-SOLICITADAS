package edu.utp.taskflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import edu.utp.taskflow.data.repository.UserRepository
import edu.utp.taskflow.model.AuthResult
import edu.utp.taskflow.model.User


class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() {

    // USUARIO CON SESIÓN ACTIVA (null = nadie ha iniciado sesión)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // MENSAJE DE ERROR PARA LOGIN / REGISTRO
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // INICIAR SESIÓN (con usuario o correo)
    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            handleResult(repository.login(identifier, password))
        }
    }

    // REGISTRAR USUARIO
    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (password != confirmPassword) {
            _errorMessage.value = "Las contraseñas no coinciden"
            return
        }

        viewModelScope.launch {
            handleResult(repository.register(username, email, password))
        }
    }

    // CERRAR SESIÓN
    fun logout() {
        _errorMessage.value = null
        _currentUser.value = null
    }

    // LIMPIAR ERROR
    fun clearError() {
        _errorMessage.value = null
    }

    private fun handleResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _errorMessage.value = null
                _currentUser.value = result.user
            }
            is AuthResult.Error -> {
                _errorMessage.value = result.message
            }
        }
    }
}


class AuthViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(
            modelClass.isAssignableFrom(
                AuthViewModel::class.java
            )
        )

        return AuthViewModel(repository) as T
    }
}