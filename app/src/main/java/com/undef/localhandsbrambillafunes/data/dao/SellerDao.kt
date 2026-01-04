package com.undef.localhandsbrambillafunes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.undef.localhandsbrambillafunes.data.entity.Seller
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad [Seller].
 *
 * Esta interfaz define todas las operaciones de acceso a datos
 * permitidas sobre la tabla asociada a la entidad `SellerEntity`,
 * utilizando Room como capa de persistencia local.
 *
 * Proporciona métodos para:
 * - Inserción y actualización de vendedores
 * - Consulta reactiva y no reactiva
 * - Eliminación masiva de registros
 *
 * Las operaciones suspendidas deben ejecutarse fuera del hilo principal,
 * garantizando un acceso seguro y eficiente a la base de datos.
 */
@Dao
interface SellerDao {

    /**
     * Inserta una lista de vendedores en la base de datos local.
     *
     * Si alguno de los vendedores ya existe (mismo identificador),
     * será reemplazado según la estrategia
     * [OnConflictStrategy.REPLACE].
     *
     * Operación suspendida que debe ejecutarse en un dispatcher
     * de I/O.
     *
     * @param sellers Lista de vendedores a insertar o actualizar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sellers: List<Seller>)

    /**
     * Inserta un único vendedor en la base de datos local.
     *
     * En caso de conflicto por clave primaria,
     * el registro existente será reemplazado.
     *
     * @param seller Vendedor a insertar o actualizar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: Seller)

    /**
     * Actualiza los datos de un vendedor existente.
     *
     * El vendedor será identificado por su clave primaria.
     * Si no existe un registro previo con ese ID,
     * la operación no tendrá efecto.
     *
     * @param seller Vendedor con los datos actualizados.
     */
    @Update
    suspend fun updateSeller(seller: Seller)

    /**
     * Obtiene todos los vendedores almacenados en la base de datos.
     *
     * Los resultados se exponen como un [Flow], permitiendo
     * a la capa de presentación observar los cambios de forma reactiva.
     * Cada modificación en la tabla emitirá automáticamente
     * una nueva lista de vendedores.
     *
     * @return Un [Flow] que emite la lista completa de vendedores.
     */
    @Query("SELECT * FROM SellerEntity")
    fun getAllSellers(): Flow<List<Seller>>


    /**
     * Obtiene un vendedor por su identificador de forma reactiva.
     *
     * El resultado se expone como un [Flow], lo que permite
     * observar cambios en el vendedor específico.
     *
     * @param id Identificador único del vendedor.
     * @return Un [Flow] que emite el vendedor encontrado,
     *         o `null` si no existe.
     */
    @Query("SELECT * FROM SellerEntity WHERE id = :id")
    fun getSellerById(id: Int): Flow<Seller?>

    /**
     * Obtiene un vendedor por su identificador de forma suspendida
     * (no reactiva).
     *
     * Este método es útil para operaciones puntuales donde no se
     * requiere observación continua de cambios.
     *
     * @param id Identificador único del vendedor.
     * @return El vendedor correspondiente, o `null` si no se encuentra.
     */
    @Query("SELECT * FROM SellerEntity WHERE id = :id")
    suspend fun getSellerByIdSuspend(id: Int): Seller?

    /**
     * Elimina todos los vendedores almacenados en la base de datos local.
     *
     * Esta operación es útil en escenarios como:
     * - Sincronización completa con un backend
     * - Limpieza de datos obsoletos
     * - Reinicialización de la información local
     *
     * Es una operación suspendida y debe ejecutarse
     * fuera del hilo principal.
     */
    @Query("DELETE FROM SellerEntity")
    suspend fun deleteAll()
}
