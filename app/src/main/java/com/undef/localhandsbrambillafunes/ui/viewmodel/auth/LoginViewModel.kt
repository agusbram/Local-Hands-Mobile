package com.undef.localhandsbrambillafunes.ui.viewmodel.auth

import androidx.compose.ui.semantics.password
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.repository.AuthRepository
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI para autenticación
 *
 * @property isLoading Indica operación en curso
 * @property isSuccess Indica éxito de login
 * @property errorMessage Mensaje de error
 * @property user Usuario autenticado
 */
data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null
)

/**
 * ViewModel para operaciones de inicio de sesión
 *
 * @property authRepository Repositorio de autenticación inyectado
 *
 * @param email Email del usuario
 * @param password Contraseña del usuario
 *
 * @method login Valida credenciales y verifica email
 * @method clearState Reinicia el estado de la UI
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    /**
     * Autentica usuario con email y contraseña
     * Además, guarda el ID del usuario en el DataStore cuando el login es exitoso
     *
     * @param email Email del usuario
     * @param password Contraseña del usuario
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.loginUser(email, password)
                .onSuccess {  user ->
                    // Se guarda el ID del usuario en el DataStore una vez que el login fue exitoso
                    userPreferencesRepository.saveUserId(user.id)

                    // Verificar si el email está verificado
                    if (user.isEmailVerified) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,  // Indicar éxito
                            user = user
                        )
                    } else {
                        // Manejo email no verificado
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Por favor verifica tu email primero"
                        )
                    }
                }
                .onFailure { exception ->
                    // Manejo errores autenticación
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = exception.message
                    )
                }

        }
    }

    /**
     * Reinicia estado de la UI
     */
    fun clearState() {
        _uiState.value = AuthState()
    }
}