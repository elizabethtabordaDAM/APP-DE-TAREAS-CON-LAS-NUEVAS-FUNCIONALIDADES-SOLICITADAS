## Nuevas funcionalidades solicitadas

En esta versión se agregaron cuatro funcionalidades sobre la base de la app de tareas:
**registro de usuarios, inicio de sesión, edición de tareas (incluida la prioridad) y cierre de sesión**.
Todo se guarda en Room, siguiendo la arquitectura MVVM.

### 1. Registro de usuarios

**¿Qué hace?** Desde la pantalla de inicio de sesión, el botón de crear cuenta lleva a la pantalla
de registro, donde se piden usuario, correo, contraseña y confirmación de contraseña.

**¿Cómo funciona?**

- `RegisterScreen` muestra el formulario. El botón **Registrarme** solo se habilita cuando
  todos los campos tienen texto.
- `PasswordTextField` es un componente reutilizable con el botón Mostrar/Ocultar contraseña.
- `AuthViewModel.register()` primero revisa que las dos contraseñas coincidan y luego llama a
  `UserRepository.register()`.
- El repositorio valida: usuario de mínimo 3 caracteres, correo con formato válido, contraseña de
  mínimo 6 caracteres, y que el usuario y el correo no estén ya registrados. Si algo falla,
  devuelve un `AuthResult.Error` y la pantalla muestra el mensaje en rojo.
- Si todo es correcto, el usuario se guarda en la tabla `users` de Room (`UserEntity` + `UserDao`).
  La contraseña **nunca se guarda en texto plano**: se genera un *salt* aleatorio y se guarda el hash
  con PBKDF2 (`PBKDF2WithHmacSHA256`).
- Después de registrarse, la app inicia sesión automáticamente y navega a la lista de tareas.

### 2. Inicio de sesión

**¿Qué hace?** Permite entrar con el usuario **o** con el correo, y cada persona ve únicamente
sus propias tareas.

**¿Cómo funciona?**

- `UserRepository.login()` busca primero por usuario y, si no lo encuentra, por correo. Calcula el
  hash de la contraseña escrita con el salt guardado y lo compara con el hash almacenado.
- Si los datos son incorrectos, muestra "Usuario o contraseña incorrectos".
- `AuthViewModel` guarda el usuario con sesión activa en `currentUser` (un `StateFlow`).
- Cada tarea tiene ahora un `userId` (dueño). `TaskViewModel` observa las tareas con
  `flatMapLatest` según el usuario actual, así que al cambiar de usuario la lista cambia sola.

### 3. Edición de tareas y cambio de prioridad

**¿Qué hace?** Cada tarjeta tiene un botón ✎. Al tocarlo se abre una hoja inferior (`ModalBottomSheet`)
con el formulario **Editar tarea**, ya cargado con el título y la prioridad actuales. Se puede cambiar
el título y la prioridad (Alta, Media o Baja) y guardar.

**¿Cómo funciona?**

- `EditTaskForm` es el formulario. El botón **Guardar cambios** se deshabilita si el título está vacío.
- `TaskViewModel.updateTask()` llama a `TaskRepository.updateTask()`, que busca la tarea por id,
  limpia el título y actualiza título y prioridad con `copy()` en Room (`@Update` del `TaskDao`).
- Como la lista viene de un `Flow`, la tarjeta se actualiza sola al guardar y el cambio se
  conserva al cerrar y volver a abrir la app.

### 4. Cerrar sesión

**¿Qué hace?** En la parte superior de la pantalla de tareas aparece el saludo "Hola, usuario" y el
botón **Cerrar sesión**, que devuelve al login.

**¿Cómo funciona?**

- `AuthViewModel.logout()` borra el usuario activo y los errores.
- En `MainActivity`, un `LaunchedEffect(currentUser)` detecta que ya no hay sesión y navega al login
  limpiando el historial de pantallas, para que el botón Atrás no permita volver a las tareas.
- `TaskViewModel` recibe `null` como usuario y la lista de tareas queda vacía.

### Cambios en la base de datos

- Se agregó la tabla `users` (`UserEntity`) con índice único en el usuario.
- Se agregó la columna `userId` a `tasks` y la columna `email` a `users`.


### Cómo probar

1. Abrir la app e ir a **Crear cuenta**. Probar un correo inválido, una contraseña de menos de
   6 caracteres y contraseñas distintas: aparecen los errores.
2. Registrarse con datos válidos: entra directo a la lista de tareas.
3. Crear dos o tres tareas con distintas prioridades.
4. Tocar ✎ en una tarea, cambiar el título y la prioridad, y **Guardar cambios**.
5. Tocar **Cerrar sesión**: vuelve al login.
6. Registrar un segundo usuario: su lista está vacía, no ve las tareas del primero.
7. Cerrar sesión e iniciar con el primer usuario, usando el usuario o el correo: sus tareas siguen ahí.
8. Cerrar la app por completo y abrirla de nuevo: usuarios y tareas siguen guardados.