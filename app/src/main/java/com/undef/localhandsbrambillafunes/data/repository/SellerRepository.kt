package com.undef.localhandsbrambillafunes.data.repository

import com.undef.localhandsbrambillafunes.data.dao.SellerDao
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio encargado de gestionar las operaciones relacionadas con la entidad [Seller].
 *
 * Esta clase actúa como una capa intermedia entre la fuente de datos remota
 * ([ApiService]) y el resto de la aplicación, encapsulando la lógica de acceso
 * a datos y manejo de errores.
 *
 * Está anotada con [Singleton] para garantizar una única instancia durante
 * el ciclo de vida de la aplicación.
 *
 * @property apiService Servicio remoto que expone los endpoints relacionados con vendedores.
 */
@Singleton
class SellerRepository @Inject constructor(
    private val apiService: ApiService,
    private val sellerDao: SellerDao
) {
    /**
     * Esta es la ÚNICA función que la UI usará para obtener los vendedores.
     * Devuelve un Flow directamente desde Room (Fuente de Verdad Única).
     */
    fun getSellers(): Flow<List<Seller>> {
        return sellerDao.getAllSellers()
    }

    /**
     * Actualiza la base de datos local con los datos de la API.
     * Esta función se llamará al iniciar la app o a intervalos regulares.
     */
    suspend fun refreshSellers() {
        try {
            // 1. Llama a la API para obtener los datos más recientes.
            val sellersFromApi = apiService.getSellers()
            // 2. Borra los datos viejos de la base de datos local.
            sellerDao.deleteAll()
            // 3. Inserta los nuevos datos en la base de datos local.
            sellerDao.insertAll(sellersFromApi)
        } catch (e: Exception) {
            // Aca se pueden manejar errores de red
            // Si la llamada falla, la app seguirá mostrando los datos viejos de Room.
            println("Error al refrescar vendedores: ${e.message}")
        }
    }

    /**
     * Obtiene un vendedor a partir de su dirección de correo electrónico.
     *
     * La API devuelve una lista de vendedores que coinciden con el email proporcionado.
     * Este método retorna el primer elemento de la lista si existe, o `null` en caso
     * contrario.
     *
     * En caso de producirse una excepción durante la llamada a la API,
     * el error es capturado y se retorna `null`.
     *
     * @param email Dirección de correo electrónico del vendedor a buscar.
     * @return El primer [Seller] encontrado con el email indicado, o `null` si no existe
     * o si ocurre un error.
     */
    suspend fun getSellerByEmail(email: String): Seller? {
        return try {
            // Devuelve una lista
            val sellers = apiService.getSellersByEmail(email)
            // Si la lista no está vacía, devuelve el primer vendedor. Si está vacía, devuelve null.
            sellers.firstOrNull()
        } catch (e: Exception) {
            println("Error fetching seller by email: ${e.message}")
            null
        }
    }

    /**
     * Crea un nuevo vendedor en el sistema asignándole un ID numérico autoincremental.
     *
     * Dado que el backend (json-server) no genera automáticamente IDs,
     * este método:
     * 1. Obtiene la lista completa de vendedores existentes.
     * 2. Calcula el siguiente ID disponible (máximo actual + 1).
     * 3. Crea una copia del objeto [Seller] con el ID asignado.
     * 4. Envía el vendedor al servicio remoto para su persistencia.
     *
     * @param seller Objeto [Seller] a crear (sin ID o con ID no válido).
     * @return El [Seller] creado, tal como lo devuelve la API.
     */
    suspend fun createSeller(seller: Seller): Seller {
        // 1. Obtener la lista actual de todos los vendedores para calcular el siguiente ID.
        val allSellers = apiService.getSellers()

        // 2. Calcular el siguiente ID.
        // Si la lista está vacía, el primer ID será 1.
        // Si no, encuentra el ID máximo actual y le suma 1.
        val nextId = (allSellers.maxOfOrNull { it.id } ?: 0) + 1

        // 3. Crea una copia del objeto vendedor pero con el nuevo ID calculado.
        val sellerWithId = seller.copy(id = nextId)

        // 4. Envía el nuevo vendedor con el ID ya asignado a json-server.
        // json-server respetará el ID que le estás enviando.
        return apiService.createSeller(sellerWithId)
    }
}