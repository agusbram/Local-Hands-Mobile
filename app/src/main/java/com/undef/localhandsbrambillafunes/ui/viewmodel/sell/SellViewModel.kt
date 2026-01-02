package com.undef.localhandsbrambillafunes.ui.viewmodel.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.repository.SellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Representa los posibles estados del proceso de creación o validación
 * del perfil de vendedor.
 *
 * Estos estados son observados por la UI para renderizar el contenido
 * correspondiente.
 */
enum class SellerCreationStatus {
    /** Estado inicial, sin acciones en curso. */
    IDLE,

    /** Indica que el proceso está en ejecución. */
    LOADING,

    /** El vendedor fue creado correctamente. */
    SUCCESS,

    /** El vendedor ya existía en el sistema. */
    ALREADY_EXISTS,

    /** Ocurrió un error durante el proceso. */
    ERROR
}

/**
 * ViewModel responsable de la lógica de negocio relacionada con el rol de vendedor.
 *
 * Centraliza la verificación de existencia de un vendedor y su creación en caso
 * de no existir, exponiendo un único flujo de estado observable por la UI.
 *
 * Sigue el patrón MVVM y utiliza corrutinas para ejecutar operaciones asíncronas
 * de manera segura dentro del ciclo de vida del ViewModel.
 *
 * @property sellerRepository Repositorio que gestiona el acceso a datos de vendedores.
 */
@HiltViewModel
class SellViewModel @Inject constructor(
    private val sellerRepository: SellerRepository
) : ViewModel() {

    /**
     * Estado interno mutable que representa el estado actual del proceso.
     */
    private val _status = MutableStateFlow<SellerCreationStatus>(SellerCreationStatus.IDLE)

    /**
     * Estado público e inmutable observado por la capa de UI.
     */
    val status: StateFlow<SellerCreationStatus> = _status

    /*
    * Expone el Flow de vendedores directamente desde el repositorio
    * Es necesario usar esta variable en caso que se quieran mostrar los vendedores en tiempo real
    * Es decir, cada vez que se actualicen los mismos con startPeriodicSync()
    * */
    val sellers: Flow<List<Seller>> = sellerRepository.getSellers()

    init {
        // Inicia el proceso de sincronización periódica.
        startPeriodicSync()
    }

    /**
     * Inicia una corrutina que se encarga de refrescar la lista de vendedores
     * a intervalos regulares.
     *
     * La corrutina se ejecutará mientras el ViewModel esté activo y se cancelará
     * automáticamente cuando el ViewModel sea destruido, evitando memory leaks.
     */
    private fun startPeriodicSync() {
        viewModelScope.launch {
            // Define el intervalo de refresco en milisegundos.
            // Por ejemplo, 30000L = 30 segundos.
            val refreshIntervalMs = 30000L

            // Bucle infinito que se ejecuta mientras la corrutina esté activa.
            while (true) {
                println("Sincronizando vendedores desde la API...")
                try {
                    // Llama al repositorio para actualizar los datos desde la API.
                    sellerRepository.refreshSellers()
                    println("Sincronización de vendedores completada.")
                } catch (e: Exception) {
                    // Si ocurre un error de red, lo capturamos para que el bucle no se rompa.
                    println("Error durante la sincronización periódica: ${e.message}")
                }
                // Pausa la corrutina durante el intervalo definido antes de la siguiente ejecución.
                delay(refreshIntervalMs)
            }
        }
    }

    /**
     * Verifica si un usuario ya posee un perfil de vendedor y lo crea en caso contrario.
     *
     * Esta función encapsula toda la lógica necesaria para:
     * 1. Consultar la existencia de un vendedor a partir del email.
     * 2. Crear un nuevo vendedor si no existe.
     * 3. Actualizar el estado del proceso para su consumo por la UI.
     *
     * Es la única función que debe ser invocada desde la interfaz de usuario
     * para iniciar el flujo de conversión a vendedor.
     *
     * @param email Dirección de correo electrónico del usuario.
     * @param name Nombre del vendedor.
     * @param lastname Apellido del vendedor.
     * @param phone Número de teléfono de contacto.
     * @param entrepreneurship Nombre del emprendimiento.
     * @param address Dirección física del emprendimiento o del vendedor.
     */
    fun becomeSeller(
        email: String,
        name: String,
        lastname: String,
        phone: String,
        entrepreneurship: String,
        address: String
    ) {
        viewModelScope.launch {
            _status.value = SellerCreationStatus.LOADING

            try {
                // 1. PRIMERO, buscamos si el vendedor ya existe por su email.
                val existingSeller = sellerRepository.getSellerByEmail(email)

                if (existingSeller != null) {
                    // 2. SI EXISTE: El trabajo está hecho. Ya es un vendedor.
                    println("Vendedor encontrado con email: $email, ID: ${existingSeller.id}")
                    _status.value = SellerCreationStatus.ALREADY_EXISTS

                } else {
                    // 3. SI NO EXISTE: Procedemos a crearlo.
                    println("Vendedor con email: $email no encontrado. Creando nuevo vendedor...")

                    val newSeller = Seller(
                        id = 0, // El ID lo calculará el repositorio, aquí no importa.
                        name = name,
                        lastname = lastname,
                        email = email,
                        phone = phone,
                        entrepreneurship = entrepreneurship,
                        address = address
                    )

                    // Llamamos a la función de creación que ya tiene la lógica del ID autoincremental.
                    val createdSeller = sellerRepository.createSeller(newSeller)

                    println("Nuevo vendedor creado con ID: ${createdSeller.id}")
                    _status.value = SellerCreationStatus.SUCCESS
                }

            } catch (e: Exception) {
                // Si algo falla en la red o en el proceso, se reporta un error.
                println("Error en el proceso de becomeSeller: ${e.message}")
                _status.value = SellerCreationStatus.ERROR
            }
        }
    }
}