package com.undef.localhandsbrambillafunes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.undef.localhandsbrambillafunes.data.entity.Seller
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad [Seller].
 *
 * Esta interfaz define las operaciones de acceso a datos permitidas
 * sobre la tabla "SellerEntity" utilizando Room como capa de persistencia.
 *
 * Provee métodos para insertar, consultar y eliminar vendedores,
 * garantizando una interacción segura y eficiente con la base de datos local.
 */
@Dao
interface SellerDao {

    /**
     * Inserta una lista de vendedores en la base de datos local.
     *
     * Si un registro con el mismo identificador ya existe,
     * será reemplazado según la estrategia [OnConflictStrategy.REPLACE].
     *
     * Este método es una operación suspendida y debe ejecutarse
     * fuera del hilo principal.
     *
     * @param sellers Lista de vendedores a insertar o actualizar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sellers: List<Seller>)

    /**
     * Obtiene todos los vendedores almacenados en la base de datos.
     *
     * Los datos se exponen como un [Flow], permitiendo a la capa de UI
     * observar cambios de manera reactiva. Cada modificación en la tabla
     * "SellerEntity" emitirá automáticamente una nueva lista de vendedores.
     *
     * @return Un [Flow] que emite la lista completa de vendedores.
     */
    @Query("SELECT * FROM SellerEntity")
    fun getAllSellers(): Flow<List<Seller>>

    /**
     * Elimina todos los registros de vendedores de la base de datos local.
     *
     * Este método resulta útil para sincronizaciones completas,
     * limpieza de datos obsoletos o reinicialización de la información
     * proveniente de una fuente remota.
     *
     * Es una operación suspendida y debe ejecutarse fuera del hilo principal.
     */
    @Query("DELETE FROM SellerEntity")
    suspend fun deleteAll()
}
