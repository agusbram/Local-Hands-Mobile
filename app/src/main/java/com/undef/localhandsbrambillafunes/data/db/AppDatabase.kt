package com.undef.localhandsbrambillafunes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.dao.ProductDao
import com.undef.localhandsbrambillafunes.data.dao.SellerDao

// Importamos las Entidades
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.entity.Favorite
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.entity.Seller

/**
 * Base de datos principal de la aplicación (Room Database)
 *
 * @property entities Lista de entidades incluidas (User, Product)
 * @property version Versión actual del esquema
 *
 * @method userDao Proporciona acceso al DAO de usuarios
 * @method productDao Proporciona acceso al DAO de productos
 */
@Database(
    entities = [User::class, Product::class, Favorite::class, Seller::class], // Entidades
    version = 4, // Incrementar cuando se modifique el esquema
    exportSchema = true // Exportar el esquema
)
@TypeConverters(Converters::class) //Para cargar List<String> de Product
abstract class AppDatabase: RoomDatabase() {
    /**
     * Proporciona acceso al DAO de Usuarios
     * @return Instancia de [UserDao] para ejecutar operaciones CRUD sobre la BD.
     */
    abstract fun userDao(): UserDao

    /**
     * Proporciona acceso al DAO de productos.
     * @return Instancia de [ProductDao] para ejecutar operaciones CRUD sobre la BD.
     */
    abstract fun productDao(): ProductDao

    /**
     * Proporciona acceso al DAO de favoritos.
     *
     * @return Instancia de [FavoriteDao] para ejecutar operaciones CRUD sobre la base de datos.
     */
    abstract fun favoriteDao(): FavoriteDao

    /**
     * Proporciona acceso al DAO de vendedores.
     *
     * @return Instancia de [SellerDao] para ejecutar operaciones CRUD sobre la base de datos.
     */
    abstract fun sellerDao(): SellerDao
}