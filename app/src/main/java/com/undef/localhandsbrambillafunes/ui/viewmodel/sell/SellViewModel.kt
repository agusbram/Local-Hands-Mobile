package com.undef.localhandsbrambillafunes.ui.viewmodel.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.data.repository.SellerRepository
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Representa los posibles estados del proceso de creación o validación
 * del perfil de vendedor.
 *
 * Esta enumeración es observada por la UI para decidir:
 * - Navegación automática
 * - Visualización de diálogos
 * - Estados de carga o error
 */
enum class SellerCreationStatus {
    /** Estado inicial, sin acciones en curso. */
    IDLE,

    /** Indica que el proceso está en ejecución. */
    LOADING,

    /** El vendedor fue creado y el rol de usuario actualizado correctamente. */
    SUCCESS,

    /** El usuario ya era un vendedor. */
    ALREADY_EXISTS,

    /** Ocurrió un error durante el proceso. */
    ERROR
}

/**
 * ViewModel responsable de la lógica de negocio para convertir
 * un usuario cliente en vendedor.
 *
 * Responsabilidades principales:
 * - Verificar el rol actual del usuario autenticado.
 * - Orquestar el proceso de conversión a vendedor.
 * - Exponer el estado del proceso de forma reactiva.
 * - Sincronizar periódicamente los datos de vendedores con la API.
 *
 * Sigue el patrón MVVM utilizando [StateFlow] para un estado observable
 * y coroutines para la ejecución asíncrona.
 *
 * @property sellerRepository Repositorio encargado de la lógica de vendedores.
 * @property userRepository Repositorio de usuarios.
 * @property userPreferencesRepository Repositorio de preferencias del usuario (DataStore).
 */
@HiltViewModel
class SellViewModel @Inject constructor(
    private val sellerRepository: SellerRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    /**
     * Estado interno mutable que representa el estado del proceso
     * de creación o validación del vendedor.
     */
    private val _status = MutableStateFlow<SellerCreationStatus>(SellerCreationStatus.IDLE)

    /**
     * Estado público e inmutable expuesto a la UI.
     */
    val status: StateFlow<SellerCreationStatus> = _status

    /**
     * Estado que contiene el nombre del emprendimiento ingresado por el usuario.
     */
    private val _entrepreneurshipName = MutableStateFlow("")

    /**
     * Flujo observable del nombre del emprendimiento.
     */
    val entrepreneurshipName: StateFlow<String> = _entrepreneurshipName

    /**
     * Al inicializar el ViewModel se inicia la sincronización periódica
     * de vendedores con la API.
     */
    init {
        startPeriodicSync()
        loadEntrepreneurshipFromDataStore()
    }

    /**
     * Carga el nombre del emprendimiento almacenado en DataStore.
     *
     * Esta función se ejecuta en un contexto de corrutina asociado al
     * ciclo de vida del ViewModel y actualiza el estado interno con el
     * valor persistido, permitiendo que la UI refleje el dato guardado.
     */
    private fun loadEntrepreneurshipFromDataStore() {
        viewModelScope.launch {
            val entrepreneurship = userPreferencesRepository.getUserEntrepreneurship()
            _entrepreneurshipName.value = entrepreneurship
        }
    }

    /**
     * Guarda el nombre del emprendimiento cuando un usuario se convierte en vendedor.
     *
     * El valor se persiste en DataStore y, simultáneamente, se actualiza
     * el estado interno del ViewModel para reflejar el cambio de forma inmediata
     * en la interfaz de usuario.
     *
     * @param entrepreneurship Nombre del emprendimiento a almacenar.
     */
    fun saveEntrepreneurship(entrepreneurship: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveUserEntrepreneurship(entrepreneurship)
            _entrepreneurshipName.value = entrepreneurship
        }
    }

    /**
     * Obtiene el nombre del emprendimiento actualmente disponible para la UI.
     *
     * Este método devuelve el valor en memoria sin acceder a DataStore,
     * por lo que es inmediato y no bloqueante.
     *
     * @return Nombre del emprendimiento cargado en el ViewModel.
     */
    fun getEntrepreneurshipForUI(): String {
        // Esta función puede ser suspend si necesitas esperar
        return _entrepreneurshipName.value
    }

    /**
     * Fuerza la carga del nombre del emprendimiento desde DataStore.
     *
     * A diferencia de [getEntrepreneurshipForUI], este método accede
     * explícitamente al almacenamiento persistente y sincroniza el valor
     * recuperado con el estado interno del ViewModel.
     *
     * @return Nombre del emprendimiento obtenido desde DataStore.
     */
    suspend fun loadEntrepreneurshipForUI(): String {
        return userPreferencesRepository.getUserEntrepreneurship().also {
            _entrepreneurshipName.value = it
        }
    }

    /**
     * Refresca el nombre del emprendimiento desde DataStore.
     *
     * Resulta útil cuando el valor puede haber cambiado desde otra pantalla
     * o flujo de la aplicación y se necesita asegurar que el estado del
     * ViewModel esté actualizado.
     */
    fun refreshEntrepreneurship() {
        loadEntrepreneurshipFromDataStore()
    }

    /**
     * Limpia el estado del nombre del emprendimiento para nuevas conversiones.
     */
    fun resetConversionState() {
        _entrepreneurshipName.value = ""
    }

    /**
     * Actualiza el nombre del emprendimiento ingresado por el usuario.
     *
     * @param name Nombre del emprendimiento.
     */
    fun onEntrepreneurshipNameChange(name: String) {
        _entrepreneurshipName.value = name
    }

    /**
     * Verifica el estado actual del usuario autenticado.
     *
     * Esta función determina si el usuario ya es vendedor o no,
     * permitiendo a la UI decidir si debe:
     * - Navegar directamente a la pantalla de ventas
     * - Mostrar el flujo de confirmación y registro
     */
    fun checkCurrentUserStatus() {
        viewModelScope.launch {
            _status.value = SellerCreationStatus.LOADING
            try {
                val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: throw Exception("Usuario no logueado.")
                val user = userRepository.getUserById(userId).firstOrNull() ?: throw Exception("Usuario no encontrado.")

                if (user.role == UserRole.SELLER) {
                    _status.value = SellerCreationStatus.ALREADY_EXISTS
                } else {
                    // Si no es vendedor, volvemos a IDLE para que la UI muestre el diálogo.
                    _status.value = SellerCreationStatus.IDLE
                }
            } catch (e: Exception) {
                _status.value = SellerCreationStatus.ERROR
            }
        }
    }

    /**
     * Determina si el usuario autenticado ya posee el rol de vendedor.
     *
     * Esta función consulta el identificador del usuario almacenado en
     * [UserPreferencesRepository] y obtiene sus datos desde [UserRepository]
     * para validar su rol actual.
     *
     * @return `true` si el usuario existe y su rol es [UserRole.SELLER];
     * `false` si el usuario no está autenticado, no existe, no es vendedor
     * o si ocurre cualquier error durante el proceso.
     */
    suspend fun isUserAlreadySeller(): Boolean {
        return try {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return false
            val user = userRepository.getUserById(userId).firstOrNull() ?: return false
            user.role == UserRole.SELLER
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Convierte al usuario actual en vendedor.
     *
     * Esta función se ejecuta únicamente cuando el usuario confirma
     * la creación del emprendimiento y proporciona un nombre válido.
     *
     * El flujo incluye:
     * - Obtención del usuario actual.
     * - Validación del rol.
     * - Creación del vendedor en la API.
     * - Actualización del rol del usuario localmente.
     */
    fun convertUserToSeller() {
        if (_entrepreneurshipName.value.isBlank()) {
            // Si el nombre está vacío, no hacemos nada o mostramos un error específico.
            // La UI ya debería prevenir esto con `enabled = false`.
            return
        }

        viewModelScope.launch {
            _status.value = SellerCreationStatus.LOADING
            try {
                val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: throw Exception("Usuario no logueado.")
                val user = userRepository.getUserById(userId).firstOrNull() ?: throw Exception("Usuario no encontrado.")

                // La verificación de si ya es vendedor es redundante aquí, pero es una buena salvaguarda.
                if (user.role == UserRole.SELLER) {
                    _status.value = SellerCreationStatus.ALREADY_EXISTS
                    return@launch
                }

                // Llamar a la función del repositorio que hace la magia.
                sellerRepository.convertToSeller(user, _entrepreneurshipName.value)
                    .onSuccess {
                        // Guardar emprendimiento en DataStore
                        userPreferencesRepository.saveUserEntrepreneurship(_entrepreneurshipName.value)

                        // Actualizar estado local
                        _entrepreneurshipName.value = _entrepreneurshipName.value

                        _status.value = SellerCreationStatus.SUCCESS

                        resetConversionState()
                    }
                    .onFailure { error ->
                        throw error
                    }
            } catch (e: Exception) {
                println("Error en el proceso de convertUserToSeller: ${e.message}")
                _status.value = SellerCreationStatus.ERROR
            }
        }
    }

    /**
     * Inicia un proceso de sincronización periódica de vendedores
     * con la API remota.
     *
     * La sincronización:
     * - Se ejecuta una vez al iniciar el ViewModel.
     * - Luego se repite cada una hora para mantener consistencia
     *   entre el servidor y la base de datos local.
     */
    private fun startPeriodicSync() {
        viewModelScope.launch {
            // Sync inmediato (al abrir la pantalla)
            sellerRepository.syncSellersWithApi()

            // Luego cada hora se actualiza (a intervalos regulares)
            val refreshIntervalMs = 60 * 60 * 1000L // 1 hora

            while (true) {
                delay(refreshIntervalMs)
                sellerRepository.syncSellersWithApi()
            }
        }
    }
}
