package com.undef.localhandsbrambillafunes.ui.screens.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.ui.components.ErrorDialog
import com.undef.localhandsbrambillafunes.ui.components.LoadingDialog
import com.undef.localhandsbrambillafunes.ui.components.LocationMapSelector
import com.undef.localhandsbrambillafunes.ui.components.PasswordField
import com.undef.localhandsbrambillafunes.ui.components.RepeatPasswordField
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.auth.RegisterViewModel

/**
 * Pantalla de registro de nuevos usuarios con verificación de email
 *
 * @param navController Controlador para navegación post-registro
 *
 * @property viewModel ViewModel que gestiona el registro
 * @property uiState Estado del proceso (verificación, errores)
 * @property name Nombre del usuario
 * @property lastName Apellido del usuario
 * @property email Email del usuario
 * @property password Contraseña del usuario
 * @property repeatPassword Confirmación de contraseña
 * @property showPassword Control de visibilidad de contraseña
 */
@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

// Observar éxito para navegar
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Navegar a login con limpieza de stack
            navController.navigate(AppScreens.LoginScreen.route) {
                popUpTo(AppScreens.RegisterScreen.route) { inclusive = true }
            }
            // Limpiar estado después de navegar
            viewModel.clearState()
        }
    }

    // Estados locales
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showMapSelector by remember { mutableStateOf(false) }

    // Validaciones
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$").matches(password)
    val doPasswordsMatch = password == repeatPassword && repeatPassword.isNotBlank()

    // Mostrar diálogos
    if (uiState.isLoading) {
        LoadingDialog(isLoading = true)
    }

    uiState.errorMessage?.let { error ->
        ErrorDialog(errorMessage = error) {
            viewModel.clearState()
        }
    }

    // Verificación de email
    if (uiState.needsVerification) {
        var verificationCode by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.clearState() },
            title = { Text("Verificación de Email") },
            text = {
                Column {
                    Text("Ingrese el código enviado a ${uiState.userEmail}")
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = { Text("Código") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Mostrar mensaje de intentos restantes
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        uiState.userEmail?.let { email ->
                            viewModel.verifyCode(email, verificationCode)
                        }
                    },
                    enabled = verificationCode.length == 4
                ) {
                    Text("Verificar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        viewModel.clearState()
                        // Lógica para reenviar el código
                        uiState.userEmail?.let { email ->
                            viewModel.sendVerificationCode(email)
                        }
                    }
                ) {
                    Text("Reenviar Código")
                }
            }
        )
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF242424))) {
        Column(Modifier.align(Alignment.Center).padding(16.dp).verticalScroll(rememberScrollState())) {


            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Registro de Usuario", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))

                    // Campos del formulario
                    ValidatedInputField("Nombre", name, name.length >= 4) { name = it }
                    ValidatedInputField("Apellido", lastName, lastName.length >= 4) { lastName = it }
                    EmailField("Email", email, isEmailValid) { email = it }

                    // Usando componentes reutilizables
                    PasswordField(
                        label = "Contraseña",
                        value = password,
                        visible = showPassword,
                        isValid = isPasswordValid,
                        onChange = { password = it },
                        onToggleVisibility = { showPassword = !showPassword } // Callback añadido
                    )

                    RepeatPasswordField(
                        label = "Repetir Contraseña",
                        originalPassword = password,
                        repeatPassword = repeatPassword,
                        visible = showPassword,
                        onChange = { repeatPassword = it },
                        onToggleVisibility = { showPassword = !showPassword } // Callback añadido
                    )

                    PhoneField("Teléfono", phone, phone.length >= 8) { phone = it }
                    
                    // Campo: Domicilio (selección con Google Maps)
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)) {
                        Text("Domicilio", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { showMapSelector = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (address.isEmpty()) "Seleccionar en Mapa" else address)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.prepareRegistration(
                                name, lastName, email, password, phone, address
                            )
                        },
                        enabled = isEmailValid && isPasswordValid && doPasswordsMatch,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrar")
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        color = Color.Blue,
                        modifier = Modifier
                            .clickable { navController.navigate(AppScreens.LoginScreen.route) }
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    // Selector de ubicación con Google Maps - abre diálogo para seleccionar domicilio
    if (showMapSelector) {
        LocationMapSelector(
            title = "Selecciona tu domicilio",
            initialAddress = address,
            context = context,
            onLocationSelected = { selectedAddress, _, _ ->
                address = selectedAddress
                showMapSelector = false
            },
            onDismiss = { showMapSelector = false },
            confirmButtonText = "Confirmar Ubicación"
        )
    }
}

/**
 * Campo de entrada de teléfono con validación en tiempo real.
 * Muestra colores verde (válido) o rojo (inválido) según el estado.
 *
 * @param label Etiqueta del campo
 * @param value Valor actual del teléfono
 * @param isValid Indica si el teléfono es válido
 * @param keyboardType Tipo de teclado a mostrar
 * @param onChange Callback invocado cuando cambia el valor
 */
@Composable
fun PhoneField(
    label: String,
    value: String,
    isValid: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit //Verifica en tiempo real el valor ingresado en el teléfono, necesario para hacer validaciones. Es una función lambda
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        singleLine = true,
        // Indica validación con colores: verde si es válido, rojo si no
        colors = TextFieldDefaults.colors(
            focusedLabelColor = if (isValid) Color.Green else Color.Red,
            focusedIndicatorColor = if (isValid) Color.Green else Color.Red,
            unfocusedIndicatorColor = if (isValid) Color.Green.copy(alpha = 0.6f) else Color.Red.copy(alpha = 0.6f)
        )
    )
}

/**
 * Campo de entrada de email con validación en tiempo real.
 * Muestra colores verde (válido) o rojo (inválido) según el formato.
 *
 * @param label Etiqueta del campo
 * @param value Valor actual del email
 * @param isValid Indica si el email tiene formato válido
 * @param keyboardType Tipo de teclado a mostrar
 * @param onChange Callback invocado cuando cambia el valor
 */
@Composable
fun EmailField(
    label: String,
    value: String,
    isValid: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedLabelColor = if (isValid) Color.Green else Color.Red,
            focusedIndicatorColor = if (isValid) Color.Green else Color.Red,
            unfocusedIndicatorColor = if (isValid) Color.Green.copy(alpha = 0.6f) else Color.Red.copy(alpha = 0.6f)
        )
    )
}

/**
 * Campo de entrada genérico con validación visual.
 * Muestra colores verde (válido) o rojo (inválido) según el estado.
 *
 * @param label Etiqueta del campo
 * @param value Valor actual
 * @param isValid Indica si el valor es válido
 * @param keyboardType Tipo de teclado a mostrar
 * @param onChange Callback invocado cuando cambia el valor
 */
@Composable
fun ValidatedInputField(
    label: String,
    value: String,
    isValid: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedLabelColor = if (isValid) Color.Green else Color.Red,
            focusedIndicatorColor = if (isValid) Color.Green else Color.Red,
            unfocusedIndicatorColor = if (isValid) Color.Green.copy(alpha = 0.6f) else Color.Red.copy(alpha = 0.6f)
        )
    )
}