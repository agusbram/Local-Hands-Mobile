package com.undef.localhandsbrambillafunes.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.data.repository.AuthRepository
import com.undef.localhandsbrambillafunes.service.EmailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de UI para el flujo de registro de usuarios.
 *
 * Representa de forma inmutable el estado actual del proceso de registro,
 * permitiendo a la UI reaccionar de manera declarativa ante los cambios.
 *
 * @property isLoading Indica si hay una operación en curso.
 * @property isSuccess Indica si el registro se completó correctamente.
 * @property errorMessage Mensaje de error en caso de fallo.
 * @property needsVerification Indica si se requiere validación por código.
 * @property userEmail Email del usuario en proceso de registro.
 * @property tempUser Usuario almacenado temporalmente hasta completar la verificación.
 */
data class RegisterState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val needsVerification: Boolean = false,
    val userEmail: String? = null,
    val tempUser: User? = null // Alamacenar usuario temporalmente
)

/**
 * ViewModel encargado del registro de usuarios con verificación por correo electrónico.
 *
 * Coordina la lógica de negocio entre la capa de UI y los repositorios,
 * manejando estado reactivo mediante [StateFlow].
 *
 * @property authRepository Repositorio de autenticación y persistencia.
 * @property emailService Servicio responsable del envío de emails.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val emailService: EmailService
) : ViewModel() {
    /**
     * Estado interno mutable del ViewModel.
     */
    private val _uiState = MutableStateFlow(RegisterState())

    /**
     * Estado expuesto a la UI de forma inmutable.
     */
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    /**
     * Usuario temporal almacenado hasta completar la verificación por email.
     */
    private var tempUser: User? = null

    /**
     * Código de verificación generado y almacenado en memoria.
     */
    private var verificationCode: String? = null

    /**
     * Contador de intentos fallidos de verificación.
     */
    private var verificationAttempts = 0

    /**
     * Cantidad máxima de intentos permitidos para ingresar el código.
     */
    private val MAX_ATTEMPTS = 3

    /**
     * Prepara el registro del usuario.
     *
     * Verifica si el email ya se encuentra registrado y, en caso contrario,
     * almacena el usuario temporalmente y envía un código de verificación.
     *
     * @param name Nombre del usuario.
     * @param lastName Apellido del usuario.
     * @param email Email del usuario.
     * @param password Contraseña.
     * @param phone Teléfono de contacto.
     * @param address Dirección del usuario.
     */
    fun prepareRegistration(
        name: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
        address: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Verificar si el email ya existe
            authRepository.isEmailExists(email)
                .onSuccess { exists ->
                    if (exists) {
                        // Manejo email existente
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "El email ya está registrado"
                        )
                    } else {
                        // Guardar usuario temporalmente (NO en base de datos)
                        tempUser = User(
                            name = name,
                            lastName = lastName,
                            email = email,
                            password = password,
                            phone = phone,
                            address = address,
                            isEmailVerified = false,
                            role = UserRole.CLIENT
                        )

                        // Enviar código de verificación
                        sendVerificationCode(email)
                    }
                }
                // Manejo de errores
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    /**
     * Genera y envía un código de verificación al email del usuario.
     *
     * @param email Dirección de correo destino.
     */
    fun sendVerificationCode(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.generateVerificationCode(email)
                .onSuccess { code ->
                    // Guardar el código generado en memoria
                    verificationCode = code

                    emailService.sendVerificationEmail(email, code)
                        .onSuccess {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                needsVerification = true,
                                userEmail = email
                            )
                        }
                }
        }
    }

    /**
     * Verifica el código ingresado por el usuario.
     *
     * Si el código es correcto, registra definitivamente al usuario.
     * En caso contrario, controla la cantidad de intentos permitidos.
     *
     * @param email Email del usuario.
     * @param code Código ingresado.
     */
    fun verifyCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            verificationCode?.let { expectedCode ->
                authRepository.verifyCode(email, code, expectedCode)
                    .onSuccess {
                        // Resetear intentos en éxito
                        verificationAttempts = 0

                        tempUser?.let { user ->
                            authRepository.registerUser(user.copy(isEmailVerified = true))
                                .onSuccess {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        isSuccess = true,
                                        needsVerification = false
                                    )
                                }
                        }
                    }
                    .onFailure { exception ->
                        // Manejo de intentos fallidos
                        verificationAttempts++

                        if (verificationAttempts >= MAX_ATTEMPTS) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Demasiados intentos. Por favor solicita un nuevo código",
                                needsVerification = false
                            )
                            verificationAttempts = 0
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Código incorrecto. Intentos restantes: ${MAX_ATTEMPTS - verificationAttempts}"
                            )
                        }
                    }
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: No se encontró código de verificación"
                )
            }
        }
    }

    /**
     * Restaura el estado inicial del ViewModel y limpia datos temporales.
     */
    fun clearState() {
        _uiState.value = RegisterState()
        // Limpiar datos temporales
        tempUser = null
    }

    /**
     * Elimina todos los usuarios almacenados.
     *
     * Método destinado exclusivamente a entornos de prueba.
     */
    fun clearUsers() {
        viewModelScope.launch {
            authRepository.clearUsersTable()
        }
    }
}