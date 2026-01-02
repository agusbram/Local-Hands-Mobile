package com.undef.localhandsbrambillafunes.ui.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica de la pantalla de perfil del usuario.
 *
 * Se encarga de:
 * - Obtener el usuario actualmente logueado
 * - Exponer los datos del perfil en tiempo real
 * - Manejar el estado de edición de los campos
 * - Persistir los cambios realizados por el usuario
 * - Gestionar el cierre de sesión
 *
 * Sigue el patrón MVVM utilizando StateFlow para un estado reactivo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
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

    // --------------------------------------------------
    // USUARIO LOGUEADO
    // --------------------------------------------------

    /**
     * Flujo que contiene el ID del usuario actualmente logueado.
     *
     * Este valor se obtiene desde DataStore y se actualiza en tiempo real.
     */
    private val loggedInUserIdFlow = userPreferencesRepository.userIdFlow

    /**
     * Flujo que expone el perfil completo del usuario logueado.
     *
     * - Si existe un ID válido, obtiene el usuario desde la base de datos.
     * - Si no hay sesión activa, emite null.
     * - Se mantiene activo mientras existan observadores.
     */
    val userProfile: StateFlow<User?> = loggedInUserIdFlow.flatMapLatest { userId ->
        if (userId != -1) {
            userRepository.getUserById(userId)
        } else {
            flowOf(null) // Si no hay usuario, emite null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null // El valor inicial es null mientras carga
    )

    // --------------------------------------------------
    // INICIALIZACIÓN
    // --------------------------------------------------


    /**
     * Al inicializar el ViewModel:
     * - Se obtiene el usuario actualmente logueado
     * - Se cargan sus datos en el estado de edición
     *
     * Esto permite que los TextFields muestren los datos actuales
     * del perfil apenas se renderiza la pantalla.
     */
    init {
        viewModelScope.launch {
            userRepository.getUserById(userPreferencesRepository.userIdFlow.first())
                .collect { user ->
                    user?.let {
                        _editState.value = EditProfileState(
                            name = it.name,
                            lastName = it.lastName,
                            address = it.address,
                            phone = it.phone,
                            email = it.email
                        )
                    }
                }
        }
    }

    // --------------------------------------------------
    // ACTUALIZACIÓN DE CAMPOS (EDICIÓN EN TIEMPO REAL)
    // --------------------------------------------------

    /**
     * Actualiza el nombre mientras el usuario escribe.
     */
    fun onNameChange(newName: String) {
        _editState.value = _editState.value.copy(name = newName)
    }

    /**
     * Actualiza el apellido mientras el usuario escribe.
     */
    fun onLastnameChange(newLastname: String) {
        _editState.value = _editState.value.copy(lastName = newLastname)
    }

    /**
     * Actualiza el domicilio mientras el usuario escribe.
     */
    fun onAddressChange(newAddress: String) {
        _editState.value = _editState.value.copy(address = newAddress)
    }

    /**
     * Actualiza el teléfono mientras el usuario escribe.
     */
    fun onPhoneChange(newPhone: String) {
        _editState.value = _editState.value.copy(phone = newPhone)
    }

    /**
     * Actualiza el email mientras el usuario escribe.
     */
    fun onEmailChange(newEmail: String) {
        _editState.value = _editState.value.copy(email = newEmail)
    }


    // --------------------------------------------------
    // GUARDADO DE CAMBIOS
    // --------------------------------------------------

    /**
     * Guarda los cambios realizados en el perfil del usuario.
     *
     * - Obtiene el usuario actual desde memoria
     * - Crea una copia con los campos modificados
     * - Persiste los cambios en la base de datos
     */
    fun saveProfileChanges() {
        viewModelScope.launch {
            // Obtenemos el usuario actual que ya tenemos en memoria.
            // Este objeto ya tiene la contraseña, el estado de verificación, etc.
            val currentUser = userProfile.value
            val currentEditState = editState.value

            if (currentUser != null) {
                // Usamos .copy() sobre el usuario existente.
                // Solo especificamos los campos que han cambiado.
                val updatedUser = currentUser.copy(
                    name = currentEditState.name,
                    lastName = currentEditState.lastName,
                    address = currentEditState.address,
                    phone = currentEditState.phone,
                    email = currentEditState.email
                )
                // Actualizamos en la base de datos con el objeto modificado.
                userRepository.updateUser(updatedUser)
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
 * Estado que representa los valores editables del perfil del usuario.
 *
 * Se utiliza para desacoplar los TextFields del modelo de datos
 * y permitir validaciones y cambios en tiempo real.
 */
data class EditProfileState(
    val name: String = "",
    val lastName: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = ""
)