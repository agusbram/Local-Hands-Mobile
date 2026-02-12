package com.undef.localhandsbrambillafunes.ui.viewmodel.sell

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.core.net.toUri
import java.io.IOException

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
    val entrepreneurshipName: StateFlow<String> = _entrepreneurshipName

    /**
     * Estado que contiene la dirección del emprendimiento seleccionada.
     */
    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address

    /**
     * Estado que contiene la latitud de la ubicación seleccionada.
     */
    private val _latitude = MutableStateFlow<Double>(0.0)
    val latitude: StateFlow<Double> = _latitude

    /**
     * Estado que contiene la longitud de la ubicación seleccionada.
     */
    private val _longitude = MutableStateFlow<Double>(0.0)
    val longitude: StateFlow<Double> = _longitude

    /**
     * Al inicializar el ViewModel se inicia la sincronización periódica
     * de vendedores con la API.
     */
    init {
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
        // Intentamos obtenerlo del DataStore
        var entrepreneurship = userPreferencesRepository.getUserEntrepreneurship()

        // Si está vacío, lo buscamos en la base de datos local usando el ID del usuario
        if (entrepreneurship.isEmpty()) {
            val userId = userPreferencesRepository.userIdFlow.firstOrNull()
            if (userId != null && userId != -1) {
                val seller = sellerRepository.getSellerByIdNonFlow(userId)
                if (seller != null) {
                    entrepreneurship = seller.entrepreneurship
                    // Lo guardamos en DataStore para la próxima vez
                    userPreferencesRepository.saveUserEntrepreneurship(entrepreneurship)
                }
            }
        }

        return entrepreneurship.also {
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
     * Limpia el estado del nombre del emprendimiento y la ubicación para nuevas conversiones.
     */
    fun resetConversionState() {
        _entrepreneurshipName.value = ""
        _address.value = null
        _latitude.value = 0.0
        _longitude.value = 0.0
    }

    /**
     * Inicializa el formulario pre-cargando la dirección del usuario logueado
     * y geocodificándola automáticamente para obtener las coordenadas.
     *
     * Este método:
     * 1. Obtiene el usuario actual desde la base de datos
     * 2. Toma su dirección registrada (User.address)
     * 3. Geocodifica esa dirección para obtener latitud/longitud
     * 4. Pre-carga estos datos en los campos del formulario
     *
     * @param context Contexto de aplicación necesario para la geocodificación
     */
    fun initializeWithUserAddress(context: android.content.Context) {
        viewModelScope.launch {
            try {
                Log.d("SellViewModel", "🔄 Inicializando con dirección del usuario...")
                
                // Obtener ID del usuario logueado
                val userId = userPreferencesRepository.userIdFlow.firstOrNull()
                if (userId == null) {
                    Log.e("SellViewModel", "❌ No hay usuario logueado")
                    return@launch
                }

                // Obtener datos del usuario
                val user = userRepository.getUserById(userId).firstOrNull()
                if (user == null) {
                    Log.e("SellViewModel", "❌ Usuario no encontrado: $userId")
                    return@launch
                }

                if (user.address.isBlank()) {
                    Log.w("SellViewModel", "⚠️ Usuario no tiene dirección registrada")
                    return@launch
                }

                Log.d("SellViewModel", "✅ Encontrado usuario: ${user.name}, dirección: ${user.address}")
                
                // Pre-cargar la dirección
                _address.value = user.address
                
                // Geocodificar la dirección para obtener coordenadas
                val geocoder = Geocoder(context)
                try {
                    val addresses = geocoder.getFromLocationName(user.address, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        _latitude.value = address.latitude
                        _longitude.value = address.longitude
                        Log.d("SellViewModel", "✅ Geocodificación exitosa: lat=${address.latitude}, lon=${address.longitude}")
                    } else {
                        Log.w("SellViewModel", "⚠️ No se encontraron coordenadas para: ${user.address}")
                        // Mantener los valores por defecto (0.0, 0.0)
                    }
                } catch (e: IOException) {
                    Log.e("SellViewModel", "❌ Error en geocodificación: ${e.message}")
                    // Mantener los valores por defecto (0.0, 0.0)
                }
            } catch (e: Exception) {
                Log.e("SellViewModel", "💥 Error en initializeWithUserAddress: ${e.message}", e)
            }
        }
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
     * Actualiza la dirección y coordenadas de la ubicación seleccionada.
     *
     * @param address Dirección de la nueva ubicación.
     */
    fun onLocationChange(address: String) {
        _address.value = address
    }

    /**
     * Actualiza las coordenadas de la ubicación seleccionada.
     *
     * @param latitude Latitud de la ubicación.
     * @param longitude Longitud de la ubicación.
     */
    fun onCoordinatesChange(latitude: Double, longitude: Double) {
        _latitude.value = latitude
        _longitude.value = longitude
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
        if (_entrepreneurshipName.value.isBlank() || _address.value.isNullOrBlank()) {
            // Si el nombre o la dirección están vacíos, no hacemos nada.
            // La UI ya debería prevenir esto con `enabled = false`.
            Log.w("SellViewModel", "⚠️ Validación fallida: entrepreneurship='${_entrepreneurshipName.value}', address='${_address.value}'")
            return
        }

        viewModelScope.launch {
            _status.value = SellerCreationStatus.LOADING
            Log.d("SellViewModel", "📍 Iniciando convertUserToSeller: lat=${_latitude.value}, lon=${_longitude.value}")
            try {
                val userId = userPreferencesRepository.userIdFlow.firstOrNull()
                if (userId == null) {
                    Log.e("SellViewModel", "❌ userId es null")
                    _status.value = SellerCreationStatus.ERROR
                    return@launch
                }

                val user = userRepository.getUserById(userId).firstOrNull()
                if (user == null) {
                    Log.e("SellViewModel", "❌ Usuario no encontrado: $userId")
                    _status.value = SellerCreationStatus.ERROR
                    return@launch
                }

                // La verificación de si ya es vendedor es redundante aquí, pero es una buena salvaguarda.
                if (user.role == UserRole.SELLER) {
                    Log.d("SellViewModel", "⚠️ Usuario ya es vendedor")
                    _status.value = SellerCreationStatus.ALREADY_EXISTS
                    return@launch
                }

                // Llamar a la función del repositorio que hace la magia.
                Log.d("SellViewModel", "📤 Llamando convertToSeller con address='${_address.value}'")
                sellerRepository.convertToSeller(
                    user = user,
                    entrepreneurshipName = _entrepreneurshipName.value,
                    address = _address.value!!,
                    latitude = _latitude.value,
                    longitude = _longitude.value
                )
                    .onSuccess {
                        Log.d("SellViewModel", "✅ convertToSeller exitoso!")
                        // Guardar emprendimiento en DataStore
                        userPreferencesRepository.saveUserEntrepreneurship(_entrepreneurshipName.value)

                        // Actualizar estado local
                        _entrepreneurshipName.value = _entrepreneurshipName.value

                        _status.value = SellerCreationStatus.SUCCESS
                        Log.d("SellViewModel", "✅ Status set to SUCCESS, reseteando estado...")

                        resetConversionState()
                    }
                    .onFailure { error ->
                        Log.e("SellViewModel", "❌ Error en convertToSeller: ${error.message}", error)
                        throw error
                    }
            } catch (e: Exception) {
                Log.e("SellViewModel", "💥 EXCEPTION en convertUserToSeller: ${e.message}", e)
                e.printStackTrace()
                _status.value = SellerCreationStatus.ERROR
            }
        }
    }

    /**
     * Obtiene el vendedor asociado a un identificador específico.
     *
     * Esta función consulta el repositorio de vendedores y retorna un [Flow] que
     * emite el objeto [Seller] correspondiente al `sellerId` indicado. El uso de
     * [Flow] permite observar los cambios de forma reactiva, de modo que cualquier
     * actualización en los datos del vendedor se reflejará automáticamente en los
     * observadores.
     *
     * @param sellerId Identificador único del vendedor.
     * @return Un [Flow] que emite el vendedor correspondiente al ID proporcionado,
     *         o `null` si no se encuentra un vendedor con dicho identificador.
     */
    fun getSellerEmailById(sellerId: Int): Flow<Seller?> {
        return sellerRepository.getSellerById(sellerId)
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