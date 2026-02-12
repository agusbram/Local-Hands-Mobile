package com.undef.localhandsbrambillafunes.data.repository

import android.util.Log
import com.undef.localhandsbrambillafunes.data.dao.SellerDao
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.dto.SellerPatchDTO
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio encargado de gestionar las operaciones relacionadas
 * con la entidad [Seller].
 *
 * Esta clase actúa como una capa intermedia entre:
 * - La fuente de datos remota ([ApiService])
 * - La base de datos local (Room mediante [SellerDao])
 *
 * Centraliza la lógica de sincronización, actualización,
 * conversión de usuarios y manejo de errores, desacoplando
 * a la capa de presentación de los detalles de implementación.
 *
 * Está anotada con [Singleton] para garantizar una única instancia
 * durante el ciclo de vida de la aplicación.
 *
 * @property apiService Servicio remoto que expone los endpoints de vendedores.
 * @property sellerDao DAO local para persistencia de vendedores.
 * @property userDao DAO local para operaciones sobre usuarios.
 */
@Singleton
class SellerRepository @Inject constructor(
    private val apiService: ApiService,
    private val sellerDao: SellerDao,
    private val userDao: UserDao
) {

    /**
     * Convierte un usuario existente en vendedor.
     *
     * El proceso incluye:
     * - Creación o actualización del vendedor en la API remota
     * - Persistencia del vendedor en la base de datos local
     * - Actualización del rol del usuario a [UserRole.SELLER]
     *
     * Se reutiliza el ID del usuario como ID del vendedor,
     * garantizando consistencia entre entidades.
     *
     * @param user Usuario a convertir en vendedor.
     * @param entrepreneurshipName Nombre del emprendimiento asociado.
     * @param address Dirección del emprendimiento.
     *
     * @return [Result] indicando éxito o fallo de la operación.
     */
    suspend fun convertToSeller(
        user: User,
        entrepreneurshipName: String,
        address: String
    ): Result<Unit> {
        Log.d("SellerRepository", "Iniciando conversión a vendedor para el usuario: ${user.email}")

        val newSellerData = Seller(
            id = user.id, // ✅ CRÍTICO: Usamos el ID del usuario
            name = user.name,
            lastname = user.lastName,
            email = user.email,
            phone = user.phone,
            address = address, // Usamos la nueva dirección
            entrepreneurship = entrepreneurshipName,
            photoUrl = user.photoUrl,
            latitude = 0.0, // Valor por defecto
            longitude = 0.0 // Valor por defecto
        )

        return try {
            // VERIFICAR SI YA EXISTE EL VENDEDOR EN LA API
            val existingSeller = try {
                apiService.getSellers().find { it.id == user.id }
            } catch (e: Exception) {
                null
            }

            val createdSellerFromApi = if (existingSeller != null) {
                // Si ya existe, actualizamos
                Log.d("SellerRepository", "Vendedor ya existe en API, actualizando...")
                apiService.patchSeller(
                    user.id,
                    SellerPatchDTO(
                        name = newSellerData.name,
                        lastname = newSellerData.lastname,
                        phone = newSellerData.phone,
                        address = newSellerData.address,
                        entrepreneurship = newSellerData.entrepreneurship,
                        photoUrl = newSellerData.photoUrl,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                )
                newSellerData // Usamos los datos locales
            } else {
                // Si no existe, lo creamos CON EL ID DEL USUARIO
                createSellerWithSpecificId(newSellerData)
            }

            // Insertar/actualizar en Room
            sellerDao.insertSeller(createdSellerFromApi)
            Log.d("SellerRepository", "Vendedor insertado en Room localmente.")

            // Actualizar rol del usuario
            val userWithNewRole = user.copy(role = UserRole.SELLER)
            userDao.updateUser(userWithNewRole)
            Log.d("SellerRepository", "Rol del usuario ${user.id} actualizado a SELLER en Room.")

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SellerRepository", "Fallo en convertToSeller: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Crea un vendedor en la API remota utilizando
     * un ID específico.
     *
     * Este método se apoya en el comportamiento del backend
     * para respetar el ID enviado en el cuerpo del POST.
     *
     * @param seller Vendedor a crear.
     * @return Vendedor creado por la API.
     */
    private suspend fun createSellerWithSpecificId(seller: Seller): Seller {
        // Llamar directamente al endpoint con el ID deseado
        // json-server respetará el ID que le envíes en el POST
        return apiService.createSeller(seller)
    }

    /**
     * Sincroniza los vendedores desde la API remota hacia la base local.
     *
     * Para cada vendedor obtenido:
     * - Se inserta si no existe localmente
     * - Se actualiza si ya existe
     *
     * Útil para sincronizaciones iniciales o refrescos completos.
     * 
     * @return Lista de vendedores sincronizados desde la API
     */
    suspend fun syncSellersWithApi(): List<Seller> {
        return try {
            Log.d("SellerRepository", "🔄 Iniciando sincronización de vendedores desde API...")
            val sellersFromApi = apiService.getSellers()
            Log.d("SellerRepository", "📡 Se obtuvieron ${sellersFromApi.size} vendedores de la API")

            var insertedCount = 0
            var updatedCount = 0
            
            // Para cada vendedor de la API, insertar o actualizar en Room
            for (apiSeller in sellersFromApi) {
                try {
                    // Verificar si ya existe en Room
                    val localSeller = sellerDao.getSellerByIdSuspend(apiSeller.id)
                    if (localSeller == null) {
                        // Insertar nuevo
                        sellerDao.insertSeller(apiSeller)
                        insertedCount++
                        Log.d("SellerRepository", "✅ Vendedor insertado: ${apiSeller.name} (ID: ${apiSeller.id})")
                    } else {
                        // Actualizar existente si hay cambios
                        sellerDao.updateSeller(apiSeller)
                        updatedCount++
                        Log.d("SellerRepository", "♻️ Vendedor actualizado: ${apiSeller.name} (ID: ${apiSeller.id})")
                    }
                } catch (e: Exception) {
                    Log.e("SellerRepository", "❌ Error procesando vendedor ${apiSeller.id}: ${e.message}", e)
                }
            }

            Log.d("SellerRepository", "✅ Sincronización completada: $insertedCount insertados, $updatedCount actualizados")
            sellersFromApi
        } catch (e: Exception) {
            Log.e("SellerRepository", "❌ Error sincronizando vendedores: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene un vendedor por su correo electrónico desde la API.
     *
     * Primero intenta una consulta directa por email.
     * Si no hay resultados, realiza una búsqueda manual
     * sobre la lista completa de vendedores.
     *
     * @param email Correo electrónico del vendedor.
     * @return Vendedor encontrado o `null` si no existe.
     */
    suspend fun getSellerByEmail(email: String): Seller? {
        return try {
            Log.d("SellerRepository", "Buscando seller por email: '$email'")

            // Usar query por email
            val sellers = apiService.getSellersByEmail(email)

            if (sellers.isNotEmpty()) {
                val seller = sellers.first()
                Log.d("SellerRepository", "✅ Seller encontrado por query email: ID=${seller.id}")
                return seller
            }

            // Si query por email no funciona, buscar en toda la lista
            Log.d("SellerRepository", "🔄 Query email vacía, buscando en lista completa...")
            val allSellers = apiService.getSellers()
            val foundSeller = allSellers.find { it.email.equals(email, ignoreCase = true) }

            if (foundSeller != null) {
                Log.d("SellerRepository", "✅ Seller encontrado en lista completa: ID=${foundSeller.id}")
            } else {
                Log.d("SellerRepository", "❌ Seller no encontrado para email: $email")
            }

            foundSeller

        } catch (e: Exception) {
            Log.e("SellerRepository", "❌ Error buscando seller por email", e)
            null
        }
    }

    /**
     * Obtiene un vendedor por ID desde la base de datos local
     * de forma reactiva.
     *
     * @param id Identificador del vendedor.
     * @return [Flow] que emite el vendedor o `null`.
     */
    fun getSellerById(id: Int): Flow<Seller?> {
        return sellerDao.getSellerById(id)
    }

    /**
     * Actualiza un vendedor únicamente en la base de datos local.
     *
     * @param seller Vendedor con los datos actualizados.
     */
    suspend fun updateSeller(seller: Seller) {
        sellerDao.updateSeller(seller)
    }

    /**
     * Actualiza un vendedor en la API remota.
     *
     * El flujo de actualización es:
     * 1. Verificación de existencia mediante GET
     * 2. Intento de actualización con PATCH
     * 3. Fallback a PUT si PATCH falla
     * 4. Persistencia local si la operación es exitosa
     *
     * @param seller Vendedor a actualizar.
     * @return [Result] indicando el resultado de la operación.
     */
    suspend fun updateSellerApi(seller: Seller): Result<Unit> {
        return try {
            Log.d("SellerRepository", "=== INICIO UPDATE SELLER API ===")
            Log.d("SellerRepository", "Intentando actualizar seller ID: ${seller.id}")

            // Verificar que GET por ID funciona
            try {
                Log.d("SellerRepository", "🔍 Probando GET /sellers/${seller.id}")
                val sellerFromApi = apiService.getSellerById(seller.id)
                Log.d("SellerRepository", "✅ GET exitoso. Seller encontrado: ${sellerFromApi.name}")

                // El seller existe, podemos proceder con PATCH/PUT
            } catch (e: Exception) {
                Log.e("SellerRepository", "❌ GET falló. El seller con ID=${seller.id} no existe en la API")
                Log.e("SellerRepository", "Error: ${e.message}")

                // Si GET falla, el seller no existe en la API
                return Result.failure(Exception("Seller no encontrado en la API. ID: ${seller.id}"))
            }

            // Intentar PATCH
            Log.d("SellerRepository", "📤 Intentando PATCH...")
            val sellerDto = SellerPatchDTO(
                name = seller.name,
                lastname = seller.lastname,
                phone = seller.phone,
                address = seller.address,
                entrepreneurship = seller.entrepreneurship,
                photoUrl = seller.photoUrl,
                latitude = seller.latitude,
                longitude = seller.longitude
            )

            val response = apiService.patchSeller(seller.id, sellerDto)

            Log.d("SellerRepository", "📊 Respuesta PATCH - Código: ${response.code()}")

            if (response.isSuccessful) {
                val updatedSeller = response.body()
                Log.d("SellerRepository", "✅ PATCH exitoso!")

                if (updatedSeller != null) {
                    // Actualizar en Room
                    sellerDao.updateSeller(updatedSeller)
                    Log.d("SellerRepository", "✅ Seller actualizado en Room")
                }

                return Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("SellerRepository", "❌ PATCH falló - Error: ${response.code()} - $errorBody")

                // Intentar PUT como alternativa
                Log.d("SellerRepository", "🔄 Intentando PUT como alternativa...")
                return tryPutUpdate(seller)
            }

        } catch (e: Exception) {
            Log.e("SellerRepository", "❌ Error general en updateSellerApi", e)
            Result.failure(Exception("Error actualizando seller: ${e.message}"))
        }
    }

    /**
     * Sincroniza la foto de perfil de un usuario con su entidad [Seller] asociada.
     *
     * Este método se utiliza cuando la foto de perfil del usuario cambia
     * (por ejemplo, al seleccionar una nueva imagen o eliminarla) y se
     * requiere reflejar ese cambio en el vendedor correspondiente.
     *
     * El flujo de la operación es el siguiente:
     * - Se obtiene el vendedor local asociado al [userId]
     * - Si el vendedor existe y el [photoUrl] no es nulo:
     *   - Se actualiza el campo `photoUrl` en la base de datos local (Room)
     *   - Se intenta sincronizar el cambio con la API remota
     *
     * Si el vendedor no existe localmente o la URL es nula,
     * la operación no realiza cambios y se considera exitosa.
     *
     * @param userId Identificador del usuario/vendedor.
     * @param photoUrl Ruta o URL de la nueva foto de perfil.
     *
     * @return [Result] indicando el éxito o fallo de la sincronización.
     */
    suspend fun syncUserPhotoToSeller(userId: Int, photoUrl: String?): Result<Unit> {
        return try {
            val seller = sellerDao.getSellerByIdSuspend(userId)
            if (seller != null && photoUrl != null) {
                val updatedSeller = seller.copy(photoUrl = photoUrl)
                sellerDao.updateSeller(updatedSeller)

                // También actualizar en API si es necesario
                updateSellerApi(updatedSeller)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SellerRepository", "Error syncing user photo to seller", e)
            Result.failure(e)
        }
    }

    /**
     * Intenta actualizar un vendedor en la API remota utilizando
     * una operación PUT como mecanismo de respaldo.
     *
     * Este método se utiliza cuando una actualización mediante PATCH
     * falla o no es soportada correctamente por el backend.
     *
     * El proceso consiste en:
     * - Construir un [SellerPatchDTO] con los datos actuales del vendedor
     * - Enviar una solicitud PUT al endpoint correspondiente
     * - Si la respuesta es exitosa:
     *   - Actualizar el vendedor en la base de datos local (Room)
     *
     * En caso de error:
     * - Se registra el detalle del fallo
     * - Se retorna un [Result.failure] con la información del error
     *
     * @param seller Vendedor con los datos a actualizar.
     *
     * @return [Result] indicando el resultado de la operación.
     */
    private suspend fun tryPutUpdate(seller: Seller): Result<Unit> {
        return try {
            val sellerDto = SellerPatchDTO(
                name = seller.name,
                lastname = seller.lastname,
                phone = seller.phone,
                address = seller.address,
                entrepreneurship = seller.entrepreneurship,
                photoUrl = seller.photoUrl, // Esto será null si se eliminó la foto
                latitude = seller.latitude,
                longitude = seller.longitude
            )

            val response = apiService.putSeller(seller.id, sellerDto)

            if (response.isSuccessful) {
                val updatedSeller = response.body()
                Log.d("SellerRepository", "✅ PUT exitoso!")

                if (updatedSeller != null) {
                    sellerDao.updateSeller(updatedSeller)
                }

                return Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("SellerRepository", "❌ PUT también falló - Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("SellerRepository", "❌ Error en PUT", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un vendedor por su identificador desde la base de datos local
     * de forma sincrónica (no reactiva).
     *
     * @param sellerId Identificador del vendedor.
     *
     * @return Instancia de [Seller] si existe, o `null` en caso contrario.
     */
    suspend fun getSellerByIdNonFlow(sellerId: Int): Seller? {
        return sellerDao.getSellerByIdNonFlow(sellerId)
    }
}