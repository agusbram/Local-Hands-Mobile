package com.undef.localhandsbrambillafunes

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.undef.localhandsbrambillafunes.data.db.AppDatabase
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
import com.undef.localhandsbrambillafunes.data.repository.SellerRepository
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LocalHandsApplication : Application() {
    
    @Inject
    lateinit var userRepository: UserRepository
    
    @Inject
    lateinit var sellerRepository: SellerRepository
    
    @Inject
    lateinit var productRepository: ProductRepository
    
    override fun onCreate() {
        super.onCreate()
        Log.d("LocalHandsApplication", "LocalHandsApplication.onCreate() - Inicializando aplicación")
        
        // Sincronizar datos desde la API cuando la app inicia
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("LocalHandsApplication", "Iniciando sincronización de datos desde la API...")
                
                // 1. Obtener vendedores desde la API (sin guardarlos aún)
                var syncedSellers = emptyList<Seller>()
                try {
                    Log.d("LocalHandsApplication", "Obteniendo vendedores desde API...")
                    syncedSellers = sellerRepository.syncSellersWithApi()
                    Log.d("LocalHandsApplication", "Vendedores obtenidos: ${syncedSellers.size} vendedores")
                } catch (e: Exception) {
                    Log.e("LocalHandsApplication", "Error obteniendo vendedores: ${e.message}", e)
                }
                
                // 2. Crear usuarios automáticamente a partir de los vendedores sincronizados
                // IMPORTANTE: Los usuarios DEBEN crearse PRIMERO porque los vendedores tienen
                // una clave foránea hacia la tabla de usuarios
                try {
                    if (syncedSellers.isNotEmpty()) {
                        Log.d("LocalHandsApplication", "Creando usuarios a partir de vendedores...")
                        userRepository.createUsersFromSellers(syncedSellers)
                        Log.d("LocalHandsApplication", "Usuarios creados correctamente")
                    }
                } catch (e: Exception) {
                    Log.e("LocalHandsApplication", "Error creando usuarios: ${e.message}", e)
                }
                
                // 3. Guardar vendedores en la base de datos local
                // Ahora es seguro guardarlos porque los usuarios ya existen
                try {
                    if (syncedSellers.isNotEmpty()) {
                        Log.d("LocalHandsApplication", "Guardando vendedores en base de datos...")
                        sellerRepository.saveSellers(syncedSellers)
                        Log.d("LocalHandsApplication", "Vendedores guardados correctamente")
                    }
                } catch (e: Exception) {
                    Log.e("LocalHandsApplication", "Error guardando vendedores: ${e.message}", e)
                }
                
                // 4. Sincronizar productos desde la API
                try {
                    Log.d("LocalHandsApplication", "Sincronizando productos...")
                    productRepository.syncProductsWithApi()
                    Log.d("LocalHandsApplication", "Productos sincronizados correctamente")
                } catch (e: Exception) {
                    Log.e("LocalHandsApplication", "Error sincronizando productos: ${e.message}", e)
                }
                
                Log.d("LocalHandsApplication", "Sincronización inicial completada")
                
            } catch (e: Exception) {
                Log.e("LocalHandsApplication", "Error crítico durante la sincronización inicial: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
}