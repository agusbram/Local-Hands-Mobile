package com.undef.localhandsbrambillafunes.ui.viewmodel.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.exception.NotAuthenticatedException
import com.undef.localhandsbrambillafunes.data.repository.AuthRepository
import com.undef.localhandsbrambillafunes.data.repository.SellerRepository
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsable de gestionar el estado de sesión del usuario autenticado.
 *
 * Esta clase extiende de [AndroidViewModel] para acceder al contexto de la aplicación cuando sea necesario.
 * Utiliza un [MutableStateFlow] para mantener el identificador del usuario autenticado y
 * exponerlo de forma reactiva a la interfaz de usuario u otras capas.
 *
 * Puede integrarse con un [UserRepository] si se desea validar o recuperar información adicional
 * del usuario autenticado.
 *
 * @param application Instancia de la aplicación requerida por [AndroidViewModel].
 * @param userRepository Repositorio opcional para validar información del usuario.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val sellerRepository: SellerRepository
) : ViewModel() {
    // Caché para guardar los datos del vendedor una vez obtenidos
    private var currentSeller: Seller? = null

    /**
     * Obtiene el perfil completo del vendedor actual y lo guarda en caché.
     * Si ya está en caché, lo devuelve directamente para evitar llamadas innecesarias a la API.
     * @return El objeto Seller del usuario actual, o null si no se encuentra.
     */
    private suspend fun fetchAndCacheCurrentSeller(): Seller? {
        // Si ya lo tenemos en memoria, lo devolvemos directamente
        if (currentSeller != null) {
            return currentSeller
        }

        // Si no, obtenemos el email del usuario logueado
        val userEmail = authRepository.getCurrentUserEmail() ?: return null

        // Usamos el repositorio para buscar al vendedor por su email a través de la API
        currentSeller = sellerRepository.getSellerByEmail(userEmail)
        return currentSeller
    }

    /**
     * Obtenemos el id del usuario autenticado de la sesión actual
     * */
    suspend fun getCurrentUserId(): Int?  {
        return  authRepository.getCurrentUserId()
            ?: throw NotAuthenticatedException("User not logged in")
    }

    /**
     * Obtiene el ID del vendedor autenticado.
     */
    suspend fun getCurrentSellerId(): Int?  {
        // Puedes mantener tu lógica original si es más directa
        // O puedes obtenerlo del perfil del vendedor
        return fetchAndCacheCurrentSeller()?.id
    }

    /**
     * Obtiene el email del vendedor autenticado.
     */
    suspend fun getCurrentSellerEmail(): String? {
        return fetchAndCacheCurrentSeller()?.email
    }

    /**
     * Obtiene el nombre del vendedor autenticado.
     */
    suspend fun getCurrentSellerName(): String? {
        return fetchAndCacheCurrentSeller()?.name
    }

    /**
     * Obtiene el apellido del vendedor autenticado.
     */
    suspend fun getCurrentSellerLastName(): String? {
        return fetchAndCacheCurrentSeller()?.lastname
    }

    /**
     * Obtiene el nombre del emprendimiento del vendedor autenticado.
     */
    suspend fun getCurrentSellerEntrepreneurship(): String? {
        return fetchAndCacheCurrentSeller()?.entrepreneurship
    }

    /**
     * Obtiene el teléfono del vendedor autenticado.
     */
    suspend fun getCurrentSellerPhone(): String? {
        return fetchAndCacheCurrentSeller()?.phone
    }

    /**
     * Obtiene la dirección del vendedor autenticado.
     */
    suspend fun getCurrentSellerAddress(): String? {
        return fetchAndCacheCurrentSeller()?.address
    }
}