package edu.utp.taskflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.utp.taskflow.ui.components.PasswordTextField
import edu.utp.taskflow.ui.theme.Muted


@Composable
fun LoginScreen(
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onGoToRegister: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "TASKFLOW",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("Bienvenido de nuevo", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Inicia sesión para ver tus tareas.",
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario o correo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña"
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = { onLogin(username, password) },
                        enabled = username.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Iniciar sesión") }
                }
            }

            OutlinedButton(
                onClick = onGoToRegister,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Crear una cuenta") }
        }
    }
}