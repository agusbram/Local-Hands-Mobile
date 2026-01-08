package com.undef.localhandsbrambillafunes.di

import android.content.Context
import androidx.room.Room
import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.dao.ProductDao
import com.undef.localhandsbrambillafunes.data.dao.SellerDao
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * Módulo Dagger Hilt que provee las dependencias relacionadas con la base de datos Room.
 *
 * Este módulo es responsable de:
 * - Crear y configurar la instancia única de la base de datos de la aplicación
 * - Proporcionar los DAOs (Data Access Objects) necesarios
 *
 * Las dependencias están configuradas para tener alcance de aplicación (Singleton).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provee una instancia única de [AppDatabase] configurada con Room.
     *
     * @param context Contexto de aplicación inyectado por Hilt
     * @return Instancia configurada de la base de datos Room
     *
     * Configuración actual:
     * - Nombre de la base de datos: "app_database"
     * - Sin migraciones explícitas (en producción deberían manejarse)
     * - Sin callback de creación (opcional para operaciones post-creación)
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration(true) // Habilitar migraciones destructivas
            //.addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Opción profesional
            //.addCallback()  // Callbacks para operaciones post-creación/popen
            .build()
    }

    /**
     * Provee el DAO para operaciones de usuario.
     *
     * @param database Instancia de la base de datos inyectada
     * @return Implementación concreta de [UserDao]
     *
     * Notas:
     * - No requiere alcance Singleton ya que Room maneja internamente
     *   las instancias del DAO de manera eficiente
     * - El DAO es generado automáticamente por Room en tiempo de compilación
     */
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    /**
     * Proporciona una instancia de [ProductDao] a partir de una instancia de [AppDatabase].
     *
     * Esta función es anotada con `@Provides`, lo que indica que es un proveedor en un módulo de Dagger/Hilt.
     * Se utiliza para inyectar la dependencia de `ProductDao` donde sea necesario dentro del ciclo de vida
     * de la aplicación.
     *
     * @param database Instancia de [AppDatabase] desde la cual se obtiene el DAO.
     * @return Instancia de [ProductDao] proporcionada por la base de datos.
     */
    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    /**
     * Proporciona una instancia de [FavoriteDao] a partir de la instancia de [AppDatabase].
     *
     * Esta función está anotada con `@Provides`, lo que indica que forma parte de un módulo de Dagger/Hilt.
     * Se utiliza para inyectar la dependencia de `FavoriteDao`, la cual permite acceder a las operaciones
     * relacionadas con la entidad de favoritos en la base de datos.
     *
     * @param database Instancia de [AppDatabase] que contiene el método de acceso al DAO.
     * @return Instancia de [FavoriteDao] proporcionada por la base de datos.
     */
    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    /**
     * Proporciona una instancia de [SellerDao] a partir de la instancia de [AppDatabase].
     *
     * Esta función está anotada con `@Provides`, lo que indica que forma parte de un módulo de Dagger/Hilt.
     * Se utiliza para inyectar la dependencia de `SellerDao`, la cual permite acceder a las operaciones
     * relacionadas con la entidad de emprendedores en la base de datos.
     *
     * @param database Instancia de [AppDatabase] que contiene el método de acceso al DAO.
     * @return Instancia de [SellerDao] proporcionada por la base de datos.
     */
    @Provides
    fun provideSellerDao(database: AppDatabase): SellerDao = database.sellerDao()
}