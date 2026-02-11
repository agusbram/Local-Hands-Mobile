package com.undef.localhandsbrambillafunes

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.undef.localhandsbrambillafunes.data.db.AppDatabase
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
                
                // 1. Sincronizar vendedores desde la API
                try {
                    Log.d("LocalHandsApplication", "Sincronizando vendedores...")
                    val syncedSellers = sellerRepository.syncSellersWithApi()
                    Log.d("LocalHandsApplication", "Vendedores sincronizados: ${syncedSellers.size} vendedores")
                    
                    // 2. Crear usuarios automáticamente a partir de los vendedores sincronizados
                    Log.d("LocalHandsApplication", "Creando usuarios a partir de vendedores...")
                    userRepository.createUsersFromSellers(syncedSellers)
                    Log.d("LocalHandsApplication", "Usuarios creados correctamente")
                    
                } catch (e: Exception) {
                    Log.e("LocalHandsApplication", "Error sincronizando vendedores o creando usuarios: ${e.message}", e)
                }
                
                // 3. Sincronizar productos desde la API
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