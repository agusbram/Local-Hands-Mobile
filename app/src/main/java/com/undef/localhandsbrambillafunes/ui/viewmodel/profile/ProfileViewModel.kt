package com.undef.localhandsbrambillafunes.ui.viewmodel.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
import com.undef.localhandsbrambillafunes.data.repository.SellerRepository
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.util.FileStorageManager
import com.undef.localhandsbrambillafunes.util.PasswordManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File

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
    private val userPreferencesRepository: UserPreferencesRepository,
    private val fileStorageManager: FileStorageManager,
    private val productRepository: ProductRepository
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

    /**
     * Estado de la UI
     * En nuestro caso, contiene únicamente la foto de perfil
     * almacenada por el usuario en tiempo real
     */
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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
     * Refresca y sincroniza el perfil del usuario actualmente autenticado.
     *
     * Este método centraliza la lógica de carga y actualización del estado
     * del perfil, incluyendo:
     * - Usuario base
     * - Rol del usuario
     * - Foto de perfil
     * - Información asociada de vendedor (si corresponde)
     *
     * El flujo de ejecución es el siguiente:
     *
     * 1. Cancela cualquier job de carga previo para evitar ejecuciones concurrentes.
     * 2. Obtiene el `userId` desde preferencias persistidas.
     * 3. Escucha de forma reactiva los cambios del usuario desde Room.
     * 4. Actualiza el estado base de la UI con los datos del usuario.
     * 5. Si el usuario tiene rol [UserRole.SELLER]:
     *    - Verifica si la foto del usuario existe pero no está sincronizada con el seller.
     *    - Sincroniza la foto entre usuario y vendedor si es necesario.
     *    - Intenta obtener el seller desde la base de datos local.
     *    - Si no existe localmente, intenta recuperarlo desde la API.
     *    - Actualiza el estado de edición y la foto de perfil según la fuente válida.
     * 6. Si el usuario tiene rol CLIENT:
     *    - Se actualiza únicamente la información del usuario.
     *
     * Este método mantiene consistencia entre:
     * - Base de datos local (Room)
     * - API remota
     * - Estado de UI expuesto al composable
     *
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

                // Se actualiza la foto de perfil del usuario en el estado de la UI
                _uiState.value = _uiState.value.copy(photoUrl = user.photoUrl)

                // Sincronizar foto de perfil si el usuario tiene pero el seller no
                if (user.role == UserRole.SELLER) {
                    val seller = sellerRepository.getSellerByIdNonFlow(userId)
                    if (seller != null && user.photoUrl != null && seller.photoUrl == null) {
                        sellerRepository.syncUserPhotoToSeller(userId, user.photoUrl)
                    }
                }

                if (user.role == UserRole.SELLER) {
                    // Obtiene la información del vendedor de la base de datos
                    sellerRepository.getSellerById(userId).collectLatest { seller ->
                        if (seller != null) {
                            originalSeller = seller
                            updateEditStateFromSeller(seller)

                            // Se actualiza la foto de perfil del vendedor en el estado de la UI
                            _uiState.value = _uiState.value.copy(photoUrl = seller.photoUrl)
                        } else {
                            // Si no existe el vendedor en la base de datos, intenta obtenerlo de la API
                            try {
                                val apiSeller = sellerRepository.getSellerByEmail(user.email)
                                if (apiSeller != null) {
                                    // Se guarda en la base de datos el vendedor
                                    sellerRepository.updateSeller(apiSeller)
                                    originalSeller = apiSeller
                                    updateEditStateFromSeller(apiSeller)

                                    // Se actualiza la foto de perfil del vendedor en el estado de la UI con la URL de la API
                                    _uiState.value = _uiState.value.copy(photoUrl = apiSeller.photoUrl)
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
            email = editState.value.email,
            photoUrl = _uiState.value.photoUrl
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
        val oldEntrepreneurship = originalSeller?.entrepreneurship ?: ""
        val newEntrepreneurship = editState.value.entrepreneurship

        Log.d("ProfileViewModel", "=== INICIO SAVE SELLER PROFILE ===")
        Log.d("ProfileViewModel", "Entrepreneurship actual: $oldEntrepreneurship")
        Log.d("ProfileViewModel", "Entrepreneurship nuevo: $newEntrepreneurship")

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
            entrepreneurship = editState.value.entrepreneurship,
            photoUrl = _uiState.value.photoUrl ?: ""
        )

        Log.d("ProfileViewModel", "Datos a actualizar:")
        Log.d("ProfileViewModel", "  - ID: ${sellerToUpdate.id}")
        Log.d("ProfileViewModel", "  - Email: ${sellerToUpdate.email}")
        Log.d("ProfileViewModel", "  - Nombre: ${sellerToUpdate.name}")

        // Actualizar en la API el vendedor
        sellerRepository.updateSellerApi(sellerToUpdate).onSuccess {
            Log.d("ProfileViewModel", "Seller actualizado exitosamente en API")

            Log.d("Emprendimiento nuevo a guardar en la API:", sellerToUpdate.entrepreneurship)

            // Si cambió el nombre del emprendimiento, actualizar productos
            if (oldEntrepreneurship != newEntrepreneurship && newEntrepreneurship.isNotBlank()) {
                try {
                    productRepository.updateProductsProducerByOwner(userId, newEntrepreneurship)

                    delay(500) // Pequeño delay para asegurar que Room haya terminado
                    Log.d("ProfileViewModel", "✅ Productos actualizados correctamente")
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error actualizando productos", e)
                }
            }


            // Actualizar DataStore con el nuevo entrepreneurship
            userPreferencesRepository.saveUserEntrepreneurship(newEntrepreneurship)


            // Actualizar User en Room
            val userToUpdate = currentUser.copy(
                name = editState.value.name,
                lastName = editState.value.lastName,
                email = editState.value.email,
                address = editState.value.address,
                phone = editState.value.phone,
                photoUrl = _uiState.value.photoUrl
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
    // FOTO DE PERFIL
    // --------------------------------------------------

    /**
     * Cambia la foto de perfil del usuario actual.
     *
     * Flujo de la operación:
     * 1. Obtiene el ID del usuario desde las preferencias.
     * 2. Recupera el usuario actual desde la base de datos local.
     * 3. Elimina el archivo físico de la foto anterior, si existe.
     * 4. Guarda la nueva imagen seleccionada en el almacenamiento interno.
     * 5. Limpia imágenes antiguas de perfil para evitar acumulación de archivos.
     * 6. Actualiza la ruta de la nueva imagen en la entidad User (Room).
     * 7. Si el usuario es vendedor, sincroniza la foto también con la entidad Seller
     *    y actualiza la información en la API remota.
     * 8. Actualiza el estado de la UI para reflejar el cambio inmediatamente.
     * 9. Notifica el resultado mediante un evento de UI.
     *
     * @param imageUri URI de la imagen seleccionada por el usuario.
     */
    fun changeProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            // Obtén el usuario actual para tener su objeto y ID
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            val currentUser = userRepository.getUserByIdNonFlow(userId) ?: return@launch

            try {
                // Borrar foto anterior si existe
                val currentPhotoUrl = currentUser.photoUrl
                if (currentPhotoUrl != null && currentPhotoUrl.isNotEmpty()) {
                    val oldFile = File(currentPhotoUrl)
                    if (oldFile.exists()) {
                        oldFile.delete()
                        Log.d("ProfileViewModel", "Foto anterior eliminada: $currentPhotoUrl")
                    }
                }

                // Copia la nueva imagen al almacenamiento interno y obtén la ruta persistente
                val newImagePath = fileStorageManager.saveImageToInternalStorage(imageUri)

                if (newImagePath != null) {
                    // Limpiar fotos antiguas
                    fileStorageManager.cleanupOldProfileImages(newImagePath)
                    // Actualizar usuario en la BD
                    val updatedUser = currentUser.copy(photoUrl = newImagePath)
                    userRepository.updateUser(updatedUser)

                    // Si es vendedor, actualizar también en Seller
                    val seller = sellerRepository.getSellerByIdNonFlow(userId)
                    if (seller != null) {
                        val updatedSeller = seller.copy(photoUrl = newImagePath)
                        sellerRepository.updateSeller(updatedSeller)
                        sellerRepository.updateSellerApi(updatedSeller)
                    }

                    // Actualizar estado de UI
                    _uiState.value = _uiState.value.copy(photoUrl = newImagePath)
                    _uiEventChannel.send(UiEvent.ShowToast("Foto de perfil actualizada."))

                } else {
                    _uiEventChannel.send(UiEvent.ShowToast("Error al guardar la imagen en el dispositivo."))
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al cambiar la foto de perfil", e)
                _uiEventChannel.send(UiEvent.ShowToast("Error al guardar la foto en la base de datos."))
            }
        }
    }

    /**
     * Refresca la URL de la foto de perfil en el estado de la UI.
     *
     * Esta función:
     * - Obtiene el usuario actual desde la base de datos local.
     * - Verifica que la ruta de la foto no sea nula ni vacía.
     * - Comprueba que el archivo físico exista en el almacenamiento interno.
     * - Actualiza el estado de la UI con una ruta válida o una cadena vacía
     *   en caso de que la imagen no exista.
     *
     * Se utiliza para evitar que la UI intente renderizar rutas inválidas
     * o archivos eliminados.
     */
    fun refreshPhotoUrl() {
        viewModelScope.launch {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            val user = userRepository.getUserByIdNonFlow(userId) ?: return@launch

            // Manejo seguro de nulos
            val photoUrlToShow = if (!user.photoUrl.isNullOrEmpty()) {
                val file = File(user.photoUrl)
                if (file.exists()) {
                    user.photoUrl
                } else {
                    ""
                }
            } else {
                ""
            }

            // Actualizar UI state
            _uiState.value = _uiState.value.copy(photoUrl = photoUrlToShow)
            Log.d("ProfileViewModel", "Photo URL refrescada: '$photoUrlToShow'")
        }
    }

    /**
     * Elimina la foto de perfil actual del usuario.
     *
     * Flujo de la operación:
     * 1. Obtiene el ID del usuario desde las preferencias.
     * 2. Recupera el usuario actual desde la base de datos local.
     * 3. Elimina el archivo físico de la imagen de perfil si existe.
     * 4. Actualiza la entidad User en Room estableciendo photoUrl en null.
     * 5. Si el usuario es vendedor:
     *    - Actualiza la entidad Seller localmente.
     *    - Sincroniza la eliminación de la foto con la API remota.
     * 6. Resetea el estado de la UI para evitar referencias a rutas obsoletas.
     * 7. Refresca explícitamente la foto mostrada en la UI.
     * 8. Emite un evento de UI indicando el resultado de la operación.
     *
     * En caso de error, se notifica mediante un mensaje de UI sin interrumpir
     * la ejecución de la aplicación.
     */
    fun deleteProfilePicture() {
        viewModelScope.launch {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            val currentUser = userRepository.getUserByIdNonFlow(userId) ?: return@launch

            try {
                // Borrar archivo físico si existe
                val currentPhotoUrl = currentUser.photoUrl
                if (currentPhotoUrl != null) {
                    val file = File(currentPhotoUrl)
                    if (file.exists()) {
                        file.delete()
                        Log.d("ProfileViewModel", "Archivo físico eliminado: $currentPhotoUrl")
                    }
                }

                // Actualizar usuario en Room
                val updatedUser = currentUser.copy(photoUrl = null)
                userRepository.updateUser(updatedUser)
                Log.d("ProfileViewModel", "Usuario actualizado en Room (photoUrl = null)")

                // Si es vendedor, actualizar también en Seller
                val seller = sellerRepository.getSellerByIdNonFlow(userId)
                if (seller != null) {
                    val updatedSeller = seller.copy(photoUrl = "")
                    sellerRepository.updateSeller(updatedSeller)

                    val updateResult = sellerRepository.updateSellerApi(updatedSeller)
                    if (updateResult.isSuccess) {
                        Log.d("ProfileViewModel", "Seller actualizado en API (photoUrl = \\\"\\\")")
                    } else {
                        Log.e("ProfileViewModel", "Error actualizando seller en API: ${updateResult.exceptionOrNull()?.message}")
                    }

                    Log.d("ProfileViewModel", "Seller actualizado (photoUrl = null)")
                }

                // Actualizar estado de UI
                _uiState.value = ProfileUiState(photoUrl = "")

                refreshPhotoUrl()

                _uiEventChannel.send(UiEvent.ShowToast("Foto de perfil eliminada."))

            } catch (e: Exception) {
                _uiEventChannel.send(UiEvent.ShowToast("Error al eliminar la foto de perfil."))
            }
        }
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
    // ELIMINACION DE CUENTA DE USUARIO/VENDEDOR
    // --------------------------------------------------

    /**
     * Elimina de forma permanente la cuenta del usuario actualmente autenticado.
     *
     * Este método coordina el proceso completo de eliminación de cuenta, delegando
     * la lógica de borrado al repositorio correspondiente y gestionando el estado
     * de sesión y la navegación posterior.
     *
     * Cualquier error ocurrido durante el proceso (fallos de red, base de datos
     * o inconsistencias de datos) es capturado y comunicado a la UI mediante
     * eventos de tipo [UiEvent.ShowToast].
     */
    fun deleteAccount() {
        viewModelScope.launch {
            // Obtener el ID del usuario desde DataStore para saber a quién borrar
            val userId = userPreferencesRepository.userIdFlow.firstOrNull()
            if (userId == null) {
                _uiEventChannel.send(UiEvent.ShowToast("Error: No se pudo identificar al usuario."))
                return@launch
            }

            try {
                // Llama al repositorio para que maneje la lógica de borrado.
                // El ViewModel no necesita saber si es vendedor o no, solo da la orden.
                userRepository.deleteUserAndAssociatedData(userId)

                // Notifica a la UI que la operación fue exitosa
                _uiEventChannel.send(UiEvent.ShowToast("Cuenta eliminada con éxito."))

                // Limpia la sesión local (muy importante)
                userPreferencesRepository.clearUserSession()

                // Navega fuera de la pantalla de perfil (por ejemplo, a la pantalla de login)
                _uiEventChannel.send(UiEvent.NavigateAndClearStack(AppScreens.LoginScreen.route))

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al eliminar la cuenta", e)
                _uiEventChannel.send(UiEvent.ShowToast("Error al eliminar la cuenta: ${e.message}"))
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
            userPreferencesRepository.clearUserSession()
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
 * Representa el estado de la interfaz de usuario del perfil.
 *
 * Esta clase modela los datos necesarios para renderizar la pantalla
 * de perfil del usuario y es utilizada como estado observable desde
 * la capa UI (Jetpack Compose).
 *
 * @property photoUrl
 * Ruta local o remota de la foto de perfil del usuario.
 * - Puede ser `null` si el usuario nunca configuró una foto.
 * - Puede ser una cadena vacía si se desea forzar la ausencia de imagen
 *   en la UI sin utilizar valores nulos.
 *
 */
data class ProfileUiState(
    val photoUrl: String? = null,
    // Se pueden añadir más campos si se necesitan en la UI
)

/**
 * Eventos de UI de un solo uso.
 */
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class NavigateAndClearStack(val route: String) : UiEvent()
}