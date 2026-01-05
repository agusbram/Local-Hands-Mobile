package com.undef.localhandsbrambillafunes.ui.viewmodel.profile

import android.util.Log
import androidx.compose.animation.core.copy
import androidx.compose.ui.semantics.password
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.data.repository.SellerRepository
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import com.undef.localhandsbrambillafunes.util.PasswordManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * ViewModel encargado de gestionar la lógica de negocio de la pantalla de perfil.
 *
 * Responsabilidades principales:
 * - Obtener el usuario actualmente autenticado.
 * - Exponer los datos del perfil mediante un estado reactivo.
 * - Gestionar la edición de los campos del perfil.
 * - Persistir los cambios en base de datos local y API.
 * - Emitir eventos de UI de un solo uso (Toast / Snackbar).
 * - Manejar el cierre de sesión.
 *
 * Sigue el patrón MVVM y utiliza [StateFlow] para estado observable
 * y [Channel] para eventos efímeros.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sellerRepository: SellerRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // --------------------------------------------------
    // ESTADO DE EDICIÓN DEL PERFIL
    // --------------------------------------------------

    /**
     * Estado interno mutable que contiene los valores que el usuario
     * está escribiendo en los campos editables del perfil.
     */
    private val _editState = MutableStateFlow(EditProfileState())

    /**
     * Estado público e inmutable expuesto a la UI.
     *
     * La interfaz observa este StateFlow para reflejar los cambios
     * en tiempo real mientras el usuario edita los campos.
     */
    val editState: StateFlow<EditProfileState> = _editState.asStateFlow()


    /**
     * Rol del usuario actualmente autenticado.
     *
     * Se utiliza para determinar la lógica de guardado
     * (cliente o vendedor).
     */
    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    /**
     * Copia original del usuario obtenida desde la base de datos.
     * Se utiliza como referencia para la actualización.
     */
    private var originalUser: User? = null

    /**
     * Copia original del vendedor obtenida desde la base de datos o API.
     */
    private var originalSeller: Seller? = null

    /**
     * Job utilizado para controlar la corrutina de carga de datos,
     * permitiendo su cancelación ante recargas.
     */
    private var dataLoadingJob: Job? = null

    // --------------------------------------------------
    // EVENTOS DE UI (ONE-TIME EVENTS)
    // --------------------------------------------------

    /**
     * Canal para emitir eventos de UI de un solo uso,
     * como mensajes Toast o Snackbars.
     */
    private val _uiEventChannel = Channel<UiEvent>()

    /**
     * Flujo observable de eventos de UI.
     */
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    // --------------------------------------------------
    // INICIALIZACIÓN
    // --------------------------------------------------
    /**
     * Al inicializar el ViewModel:
     * - Se obtiene el usuario actualmente logueado.
     * - Se cargan sus datos en el estado de edición.
     */
    init {
        refreshUserProfile()
    }

    /**
     * Refresca los datos del perfil del usuario desde la base de datos local
     * y, en caso de ser vendedor, sincroniza con la API.
     */
    fun refreshUserProfile() {
        dataLoadingJob?.cancel()
        dataLoadingJob = viewModelScope.launch {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return@launch

            // Obtiene el usuario de la base de datos
            userRepository.getUserById(userId).collectLatest { user ->
                if (user == null) return@collectLatest

                originalUser = user
                _userRole.value = user.role

                if (user.role == UserRole.SELLER) {
                    // Obtiene la información del vendedor de la base de datos
                    sellerRepository.getSellerById(userId).collectLatest { seller ->
                        if (seller != null) {
                            originalSeller = seller
                            updateEditStateFromSeller(seller)
                        } else {
                            // Si no existe el vendedor en la base de datos, intenta obtenerlo de la API
                            try {
                                val apiSeller = sellerRepository.getSellerByEmail(user.email)
                                if (apiSeller != null) {
                                    // Se guarda en la base de datos el vendedor
                                    sellerRepository.updateSeller(apiSeller)
                                    originalSeller = apiSeller
                                    updateEditStateFromSeller(apiSeller)
                                } else {
                                    // Actualiza el estado de los datos del usuario modificado
                                    updateEditStateFromUser(user)
                                }
                            } catch (e: Exception) {
                                updateEditStateFromUser(user)
                            }
                        }
                    }
                } else {
                    // Si es CLIENT, actualizar informacion del usuario
                    originalSeller = null
                    updateEditStateFromUser(user)
                }
            }
        }
    }

    // --------------------------------------------------
    // ACTUALIZACIÓN DE CAMPOS (EDICIÓN EN TIEMPO REAL)
    // --------------------------------------------------

    /**
     * Actualiza el estado de edición del perfil.
     *
     * @param newState Nuevo estado con los valores editados.
     */
    fun onFieldChange(newState: EditProfileState) {
        _editState.value = newState
    }

    // --------------------------------------------------
    // GUARDADO DE CAMBIOS
    // --------------------------------------------------

    /**
     * Guarda los cambios del perfil según el rol del usuario.
     */
    fun saveChanges() {
        viewModelScope.launch {
            when (_userRole.value) { // Ahora el rol será el correcto y actualizado
                UserRole.SELLER -> saveSellerProfile()
                UserRole.CLIENT -> saveClientProfile()
                null -> _uiEventChannel.send(UiEvent.ShowToast("Error: Rol de usuario desconocido."))
            }
        }
    }

    /**
     * Persiste los cambios del perfil de un usuario cliente.
     */
    private suspend fun saveClientProfile() {
        val userToUpdate = originalUser?.copy(
            name = editState.value.name,
            lastName = editState.value.lastName,
            address = editState.value.address,
            phone = editState.value.phone,
            email = editState.value.email
        )
        if (userToUpdate != null) {
            userRepository.updateUser(userToUpdate)
            _uiEventChannel.send(UiEvent.ShowToast("Perfil de cliente actualizado."))
        }
    }

    /**
     * Persiste los cambios del perfil de un usuario vendedor.
     *
     * La actualización se realiza primero en la API remota
     * y luego se sincroniza con la base de datos local.
     */
    private suspend fun saveSellerProfile() {
        Log.d("ProfileViewModel", "=== INICIO SAVE SELLER PROFILE ===")

        // Obtener el usuario actualmente logueado
        val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: -1
        if (userId == -1) {
            _uiEventChannel.send(UiEvent.ShowToast("Error: No hay sesión activa."))
            return
        }

        // Obtener el usuario de la base de datos
        val currentUser = userRepository.getUserById(userId).firstOrNull()
        if (currentUser == null) {
            _uiEventChannel.send(UiEvent.ShowToast("Error: Usuario no encontrado."))
            return
        }

        Log.d("ProfileViewModel", "Usuario actual: ID=$userId, Email=${currentUser.email}")

        // Buscar el seller en la API para obtener su ID REAL
        val apiSeller = sellerRepository.getSellerByEmail(currentUser.email)

        if (apiSeller == null) {
            Log.e("ProfileViewModel", "CRÍTICO: Seller no encontrado en API para email: ${currentUser.email}")
            _uiEventChannel.send(UiEvent.ShowToast("Error: Tu perfil de vendedor no existe en el servidor. Contacta soporte."))
            return
        }

        val realSellerId = apiSeller.id
        Log.d("ProfileViewModel", "Seller encontrado en API. ID real: $realSellerId")

        // Crear un objeto Seller con el ID REAL de la API
        val sellerToUpdate = Seller(
            id = realSellerId,  // Usar el ID de la API, no el del usuario
            name = editState.value.name,
            lastname = editState.value.lastName,
            email = editState.value.email,
            address = editState.value.address,
            phone = editState.value.phone,
            entrepreneurship = editState.value.entrepreneurship
        )

        Log.d("ProfileViewModel", "Datos a actualizar:")
        Log.d("ProfileViewModel", "  - ID: ${sellerToUpdate.id}")
        Log.d("ProfileViewModel", "  - Email: ${sellerToUpdate.email}")
        Log.d("ProfileViewModel", "  - Nombre: ${sellerToUpdate.name}")

        // Actualizar en la API el vendedor
        sellerRepository.updateSellerApi(sellerToUpdate).onSuccess {
            Log.d("ProfileViewModel", "Seller actualizado exitosamente en API")

            // Actualizar User en Room
            val userToUpdate = currentUser.copy(
                name = editState.value.name,
                lastName = editState.value.lastName,
                email = editState.value.email,
                address = editState.value.address,
                phone = editState.value.phone
            )

            userRepository.updateUser(userToUpdate)
            Log.d("ProfileViewModel", "User actualizado en Room")

            // Actualizar Seller en Room
            sellerRepository.updateSeller(sellerToUpdate)
            Log.d("ProfileViewModel", "Seller actualizado en Room")

            // Sincronizar lista completa
            sellerRepository.syncSellersWithApi()

            // Actualizar caché local
            originalSeller = sellerToUpdate
            originalUser = userToUpdate

            _uiEventChannel.send(UiEvent.ShowToast("Perfil actualizado correctamente"))

        }.onFailure { error ->
            Log.e("ProfileViewModel", "Error actualizando seller en API", error)

            // Manejar errores específicos
            val errorMessage = when {
                error.message?.contains("404") == true -> "Error 404: Recurso no encontrado en el servidor"
                error.message?.contains("no encontrado") == true -> "Vendedor no encontrado en el servidor"
                error.message?.contains("Failed to connect") == true -> "Error de conexión. Verifica tu internet"
                else -> "Error: ${error.message ?: "Error desconocido"}"
            }

            _uiEventChannel.send(UiEvent.ShowToast(errorMessage))

            // Como resguardo, actualizar solo localmente
            Log.w("ProfileViewModel", "⚠Actualizando solo localmente como fallback")
            sellerRepository.updateSeller(sellerToUpdate)
            _uiEventChannel.send(UiEvent.ShowToast("Perfil actualizado solo localmente"))
        }

        Log.d("ProfileViewModel", "=== FIN SAVE SELLER PROFILE ===")
    }

    // --- Funciones de ayuda y estado ---

    /**
     * Actualiza el estado de edición a partir de un [User].
     */
    private fun updateEditStateFromUser(user: User) {
        _editState.value = EditProfileState(
            name = user.name,
            lastName = user.lastName,
            email = user.email,
            address = user.address,
            phone = user.phone,
            entrepreneurship = "" // Los clientes no tienen
        )
    }

    /**
     * Actualiza el estado de edición a partir de un [Seller].
     */
    private fun updateEditStateFromSeller(seller: Seller) {
        _editState.value = EditProfileState(
            name = seller.name,
            lastName = seller.lastname,
            email = seller.email,
            address = seller.address,
            phone = seller.phone,
            entrepreneurship = seller.entrepreneurship
        )
    }

    // --------------------------------------------------
    // CAMBIO DE CONTRASEÑA
    // --------------------------------------------------

    /**
     * Cambia la contraseña del usuario actualmente autenticado.
     *
     * El proceso sigue los siguientes pasos:
     * 1. Obtiene el identificador del usuario desde DataStore.
     * 2. Recupera los datos del usuario desde la base de datos local (Room).
     * 3. Verifica que la contraseña actual ingresada coincida con la almacenada,
     *    utilizando verificación segura mediante BCrypt.
     * 4. Genera un nuevo hash BCrypt para la nueva contraseña y actualiza el usuario.
     *
     * Todos los resultados y errores se comunican a la UI mediante eventos
     * enviados a través de [_uiEventChannel].
     *
     * Esta función se ejecuta dentro de [viewModelScope], garantizando:
     * - Cancelación automática cuando el ViewModel es destruido.
     * - Ejecución asíncrona sin bloquear el hilo principal.
     *
     * @param currentPasswordAttempt Contraseña actual ingresada por el usuario
     * en texto plano.
     * @param newPassword Nueva contraseña ingresada por el usuario en texto plano.
     */
    fun changeUserPassword(currentPasswordAttempt: String, newPassword: String) {
        viewModelScope.launch {
            // Obtiene el ID del usuario desde DataStore
            val userId = userPreferencesRepository.userIdFlow.firstOrNull()
            if (userId == null) {
                _uiEventChannel.send(UiEvent.ShowToast("No se encontró el usuario en el DataStore"))
                return@launch
            }

            // Obtiene los datos del usuario desde Room
            val user = userRepository.getUserByIdNonFlow(userId)
            if (user == null) {
                _uiEventChannel.send(UiEvent.ShowToast("No se encontró el usuario en la base de datos"))
                return@launch
            }

            // Verifica la contraseña actual
            if(!PasswordManager.checkPassword(currentPasswordAttempt, user.password)) {
                _uiEventChannel.send(UiEvent.ShowToast("La contraseña actual es incorrecta."))
                return@launch
            }

            // Procede a actualizar (llamando a la función simple del repositorio)
            try {
                val hashedNewPassword = PasswordManager.hashPassword(newPassword)
                val updatedUser = user.copy(password = hashedNewPassword)
                userRepository.updateUser(updatedUser) // Llama a la función que solo actualiza Room
                _uiEventChannel.send(UiEvent.ShowToast("¡Contraseña actualizada con éxito!"))
            } catch (e: CancellationException) {
                _uiEventChannel.send(UiEvent.ShowToast("Se canceló la corrutina por una falla desconocida."))
                throw e
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al actualizar contraseña", e)
                _uiEventChannel.send(UiEvent.ShowToast("Ocurrió un error inesperado al actualizar."))
            }
        }
    }

    // --------------------------------------------------
    // CIERRE DE SESIÓN
    // --------------------------------------------------

    /**
     * Cierra la sesión del usuario actual.
     *
     * Limpia el ID del usuario almacenado en DataStore,
     * provocando que la aplicación vuelva al estado no autenticado.
     */
    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearUserId()
        }
    }
}

/**
 * Estado que representa los valores editables del perfil.
 *
 * Se utiliza para desacoplar la UI del modelo de datos
 * y permitir validaciones en tiempo real.
 */
data class EditProfileState(
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val address: String = "",
    val phone: String = "",
    val entrepreneurship: String = ""
)


/**
 * Eventos de UI de un solo uso.
 */
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}