package com.undef.localhandsbrambillafunes.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.dao.ProductDao
import com.undef.localhandsbrambillafunes.data.dao.SellerDao
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.db.AppDatabase
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        callback: AppDatabaseCallback
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            .addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    class AppDatabaseCallback @Inject constructor(
        private val userDaoProvider: Provider<UserDao>,
        private val productDaoProvider: Provider<ProductDao>,
        private val sellerDaoProvider: Provider<SellerDao> // Inyectamos el SellerDao
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val userDao = userDaoProvider.get()
                val productDao = productDaoProvider.get()
                val sellerDao = sellerDaoProvider.get() // Lo obtenemos aquí
                populateDatabase(userDao, productDao, sellerDao)
            }
        }

        private suspend fun populateDatabase(userDao: UserDao, productDao: ProductDao, sellerDao: SellerDao) {
            // --- CREACIÓN DE USUARIOS Y VENDEDORES ---

            // Vendedor 1
            val seller1User = User(
                name = "Carlos",
                lastName = "Gomez",
                email = "carlos.gomez@example.com",
                password = "password123",
                phone = "1122334455",
                address = "Av. Siempre Viva 742",
                role = UserRole.SELLER,
                photoUrl = "https://picsum.photos/id/1025/200/200"
            )
            val seller1Id = userDao.insertUser(seller1User)

            // Creamos la entidad Seller correspondiente
            sellerDao.insertSeller(
                Seller(
                    id = seller1Id.toInt(),
                    name = seller1User.name,
                    lastname = seller1User.lastName,
                    email = seller1User.email,
                    phone = seller1User.phone,
                    photoUrl = seller1User.photoUrl,
                    entrepreneurship = "El Artesano del Cuero",
                    address = seller1User.address
                )
            )

            // Vendedor 2
            val seller2User = User(
                name = "Ana",
                lastName = "Martinez",
                email = "ana.martinez@example.com",
                password = "password123",
                phone = "5566778899",
                address = "Calle Falsa 123",
                role = UserRole.SELLER,
                photoUrl = "https://picsum.photos/id/1084/200/200"
            )
            val seller2Id = userDao.insertUser(seller2User)

            sellerDao.insertSeller(
                Seller(
                    id = seller2Id.toInt(),
                    name = seller2User.name,
                    lastname = seller2User.lastName,
                    email = seller2User.email,
                    phone = seller2User.phone,
                    photoUrl = seller2User.photoUrl,
                    entrepreneurship = "Huerta Orgánica 'La Raíz'",
                    address = seller2User.address
                )
            )

            // Cliente
            userDao.insertUser(
                User(
                    name = "Laura",
                    lastName = "Perez",
                    email = "laura.perez@example.com",
                    password = "password123",
                    phone = "9988776655",
                    address = "Boulevard de los Sueños Rotos 45",
                    role = UserRole.CLIENT
                )
            )

            // --- CREACIÓN DE PRODUCTOS ---
            productDao.addProduct(
                Product(
                    name = "Billetera de Cuero Genuino",
                    description = "Hecha a mano con cuero de vaca de alta calidad. Diseño clásico y duradero.",
                    producer = "El Artesano del Cuero",
                    category = "Artesanías",
                    ownerId = seller1Id.toInt(),
                    images = listOf("https://picsum.photos/id/24/400/300", "https://picsum.photos/id/40/400/300"),
                    price = 4500.0,
                    location = "Córdoba"
                )
            )

            productDao.addProduct(
                Product(
                    name = "Cinturón de Cuero Trenzado",
                    description = "Cinturón unisex, ideal para jeans o pantalones de vestir. Hebilla de acero inoxidable.",
                    producer = "El Artesano del Cuero",
                    category = "Textiles",
                    ownerId = seller1Id.toInt(),
                    images = listOf("https://picsum.photos/id/145/400/300"),
                    price = 3800.0,
                    location = "Córdoba"
                )
            )

            productDao.addProduct(
                Product(
                    name = "Tomates Orgánicos",
                    description = "Canasta de 1kg de tomates frescos, cultivados sin pesticidas. Sabor 100% natural.",
                    producer = "Huerta Orgánica 'La Raíz'",
                    category = "Alimentos",
                    ownerId = seller2Id.toInt(),
                    images = listOf("https://picsum.photos/id/1080/400/300", "https://picsum.photos/id/188/400/300"),
                    price = 800.0,
                    location = "Rosario"
                )
            )

            productDao.addProduct(
                Product(
                    name = "Miel Pura de Abeja",
                    description = "Frasco de 500g de miel pura, cosechada de nuestras propias colmenas.",
                    producer = "Huerta Orgánica 'La Raíz'",
                    category = "Alimentos",
                    ownerId = seller2Id.toInt(),
                    images = listOf("https://picsum.photos/id/135/400/300"),
                    price = 1200.0,
                    location = "Rosario"
                )
            )
        }
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideSellerDao(db: AppDatabase): SellerDao = db.sellerDao()
}
