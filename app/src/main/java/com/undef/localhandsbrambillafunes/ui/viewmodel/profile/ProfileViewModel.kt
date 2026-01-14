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
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import com.undef.localhandsbrambillafunes.util.FileStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel para la pantalla de Perfil.
 *
 * Gestiona el estado de la UI para la visualización y edición del perfil del usuario.
 * Orquesta la comunicación con los repositorios para cargar y guardar datos del usuario y del vendedor,
 * y maneja la lógica de negocio asociada.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sellerRepository: SellerRepository,
    private val productRepository: ProductRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val fileStorageManager: FileStorageManager
) : ViewModel() {

    // Estado de la UI para datos de visualización (nombre, foto, etc.)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Estado de la UI para los campos del formulario de edición.
    private val _editState = MutableStateFlow(EditProfileState())
    val editState: StateFlow<EditProfileState> = _editState.asStateFlow()

    // Canal para enviar eventos únicos a la UI (ej. Toasts, navegación).
    private val _uiEventChannel = Channel<UiEvent>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    // Estado interno para almacenar el rol del usuario actual.
    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    // Caché de los datos originales para comparar si hay cambios.
    private var originalUser: User? = null
    private var originalSeller: Seller? = null

    init {
        loadInitialData()
    }

    /**
     * Carga los datos iniciales del usuario desde la sesión y los repositorios.
     * Popula los estados de la UI con la información del perfil.
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: -1
            if (userId != -1) {
                val user = userRepository.getUserById(userId).firstOrNull()
                if (user == null) {
                    _uiEventChannel.send(UiEvent.ShowToast("Error: Usuario no encontrado."))
                    return@launch
                }

                originalUser = user
                _userRole.value = user.role
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSeller = user.role == UserRole.SELLER,
                    name = user.name,
                    lastName = user.lastName,
                    email = user.email,
                    photoUrl = user.photoUrl
                )

                if (user.role == UserRole.SELLER) {
                    try {
                        val apiSeller = sellerRepository.getSellerByEmail(user.email)
                        if (apiSeller != null) {
                            sellerRepository.updateSeller(apiSeller)
                            originalSeller = apiSeller
                            updateEditStateFromSeller(apiSeller)
                            _uiState.value = _uiState.value.copy(photoUrl = apiSeller.photoUrl)
                        } else {
                            updateEditStateFromUser(user)
                        }
                    } catch (_: Exception) {
                        updateEditStateFromUser(user)
                    }
                } else {
                    originalSeller = null
                    updateEditStateFromUser(user)
                }
            }
        }
    }

    /**
     * Actualiza el estado del formulario de edición en respuesta a la entrada del usuario.
     */
    fun onFieldChange(newState: EditProfileState) {
        _editState.value = newState
    }

    /**
     * Punto de entrada para guardar los cambios del perfil.
     * Delega a la función de guardado apropiada según el rol del usuario.
     */
    fun saveChanges() {
        viewModelScope.launch {
            when (_userRole.value) {
                UserRole.SELLER -> saveSellerProfile()
                UserRole.CLIENT -> saveClientProfile()
                null -> _uiEventChannel.send(UiEvent.ShowToast("Error: Rol de usuario desconocido."))
            }
        }
    }

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

    private suspend fun saveSellerProfile() {
        val oldEntrepreneurship = originalSeller?.entrepreneurship ?: ""
        val newEntrepreneurship = editState.value.entrepreneurship

        val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: -1
        if (userId == -1) {
            _uiEventChannel.send(UiEvent.ShowToast("Error: No hay sesión activa."))
            return
        }

        val currentUser = userRepository.getUserById(userId).firstOrNull()
        if (currentUser == null) {
            _uiEventChannel.send(UiEvent.ShowToast("Error: Usuario no encontrado."))
            return
        }

        val apiSeller = sellerRepository.getSellerByEmail(currentUser.email)
        if (apiSeller == null) {
            _uiEventChannel.send(UiEvent.ShowToast("Error: Tu perfil de vendedor no existe en el servidor."))
            return
        }

        val realSellerId = apiSeller.id
        val sellerToUpdate = Seller(
            id = realSellerId,
            name = editState.value.name,
            lastname = editState.value.lastName,
            email = editState.value.email,
            address = editState.value.address,
            phone = editState.value.phone,
            entrepreneurship = editState.value.entrepreneurship,
            photoUrl = _uiState.value.photoUrl ?: ""
        )

        sellerRepository.updateSellerApi(sellerToUpdate).onSuccess { 
            if (oldEntrepreneurship != newEntrepreneurship && newEntrepreneurship.isNotBlank()) {
                try {
                    productRepository.updateProductsProducerByOwner(userId, newEntrepreneurship)
                    delay(500)
                } catch (_: Exception) {
                    Log.e("ProfileViewModel", "Error actualizando productos")
                }
            }

            userPreferencesRepository.saveUserEntrepreneurship(newEntrepreneurship)

            val userToUpdate = currentUser.copy(
                name = editState.value.name,
                lastName = editState.value.lastName,
                email = editState.value.email,
                address = editState.value.address,
                phone = editState.value.phone,
                photoUrl = _uiState.value.photoUrl
            )
            userRepository.updateUser(userToUpdate)
            sellerRepository.updateSeller(sellerToUpdate)
            sellerRepository.syncSellersWithApi()

            originalSeller = sellerToUpdate
            originalUser = userToUpdate

            _uiEventChannel.send(UiEvent.ShowToast("Perfil actualizado correctamente"))

        }.onFailure { error ->
            val errorMessage = when {
                error.message?.contains("404") == true -> "Error 404: Recurso no encontrado"
                error.message?.contains("no encontrado") == true -> "Vendedor no encontrado"
                error.message?.contains("Failed to connect") == true -> "Error de conexión"
                else -> "Error: ${error.message ?: "desconocido"}"
            }
            _uiEventChannel.send(UiEvent.ShowToast(errorMessage))

            Log.w("ProfileViewModel", "Actualizando solo localmente como fallback")
            sellerRepository.updateSeller(sellerToUpdate)
            _uiEventChannel.send(UiEvent.ShowToast("Perfil actualizado solo localmente"))
        }
    }

    private fun updateEditStateFromUser(user: User) {
        _editState.value = EditProfileState(
            name = user.name,
            lastName = user.lastName,
            email = user.email,
            address = user.address,
            phone = user.phone,
            entrepreneurship = ""
        )
    }

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

    fun changeProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            val currentUser = userRepository.getUserByIdNonFlow(userId) ?: return@launch

            try {
                val currentPhotoUrl = currentUser.photoUrl
                if (currentPhotoUrl != null && currentPhotoUrl.isNotEmpty()) {
                    val oldFile = File(currentPhotoUrl)
                    if (oldFile.exists()) {
                        oldFile.delete()
                    }
                }

                val newImagePath = fileStorageManager.saveImageToInternalStorage(imageUri)

                if (newImagePath != null) {
                    fileStorageManager.cleanupOldProfileImages(newImagePath)
                    val updatedUser = currentUser.copy(photoUrl = newImagePath)
                    userRepository.updateUser(updatedUser)

                    val seller = sellerRepository.getSellerByIdNonFlow(userId)
                    if (seller != null) {
                        val updatedSeller = seller.copy(photoUrl = newImagePath)
                        sellerRepository.updateSellerApi(updatedSeller)
                    }

                    _uiState.value = _uiState.value.copy(photoUrl = newImagePath)
                    _uiEventChannel.send(UiEvent.ShowToast("Foto de perfil actualizada."))
                } else {
                    _uiEventChannel.send(UiEvent.ShowToast("Error al guardar la imagen."))
                }
            } catch (_: Exception) {
                _uiEventChannel.send(UiEvent.ShowToast("Error al cambiar la foto de perfil."))
            }
        }
    }
    
    fun refreshPhotoUrl() {
        viewModelScope.launch {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            val user = userRepository.getUserById(userId).firstOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(photoUrl = user.photoUrl)
        }
    }
    
    fun changeUserPassword(password: String) {
        viewModelScope.launch {
             userRepository.updateUserPassword(password) 
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            userRepository.deleteUserAccount()
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            val currentUser = userRepository.getUserByIdNonFlow(userId) ?: return@launch

            val currentPhotoUrl = currentUser.photoUrl
            if (currentPhotoUrl != null && currentPhotoUrl.isNotEmpty()) {
                try {
                    val oldFile = File(currentPhotoUrl)
                    if (oldFile.exists()) {
                        oldFile.delete()
                    }

                    val updatedUser = currentUser.copy(photoUrl = "")
                    userRepository.updateUser(updatedUser)

                    val seller = sellerRepository.getSellerByIdNonFlow(userId)
                    if (seller != null) {
                        val updatedSeller = seller.copy(photoUrl = "")
                        sellerRepository.updateSellerApi(updatedSeller)
                    }

                    _uiState.value = _uiState.value.copy(photoUrl = "")
                    _uiEventChannel.send(UiEvent.ShowToast("Foto de perfil eliminada."))

                } catch (_: Exception) {
                    _uiEventChannel.send(UiEvent.ShowToast("Error al eliminar la foto de perfil."))
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class NavigateAndClearStack(val route: String) : UiEvent()
    }

    data class ProfileUiState(
        val isLoading: Boolean = true,
        val isSeller: Boolean = false,
        val name: String = "",
        val lastName: String = "",
        val email: String = "",
        val photoUrl: String? = null
    )

    data class EditProfileState(
        val name: String = "",
        val lastName: String = "",
        val email: String = "",
        val address: String = "",
        val phone: String = "",
        val entrepreneurship: String = ""
    )
}
