package com.undef.localhandsbrambillafunes.ui.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: UserRepository): ViewModel() {
    private val _sellers = MutableStateFlow<List<Seller>>(emptyList())
    val sellers: StateFlow<List<Seller>> = _sellers

    /**
     * Crea un nuevo vendedor (Seller) en el servidor y lo agrega a la lista local de vendedores.
     *
     * Esta función lanza una corrutina en el `viewModelScope` para:
     * 1. Enviar una solicitud POST a través del repositorio para crear el vendedor en el servidor.
     * 2. Actualizar el `StateFlow` interno (`_sellers`) agregando el nuevo vendedor a la lista actual.
     *
     * @param seller El objeto [Seller] que se desea registrar.
     *
     * @see repository.createSeller
     */
    fun createSeller(seller: Seller) {
        viewModelScope.launch {
            val newSeller = repository.createSeller(seller)
            _sellers.value = _sellers.value + newSeller
        }
    }

    /**
     * Descarga la lista completa de vendedores desde el servidor.
     *
     * Esta función realiza una solicitud de red utilizando el repositorio
     * y actualiza el `StateFlow` interno (`_sellers`) con los datos obtenidos.
     *
     * Se debe invocar, por ejemplo, al iniciar una pantalla que consuma la lista
     * de vendedores o cuando se requiere refrescar los datos.
     *
     * @see repository.getSellers
     */
    fun getSellers() {
        viewModelScope.launch {
            _sellers.value = repository.getSellers()
        }
    }

    /**
     * Obtiene un usuario por su ID.
     * @return Instancia de [User], se supone que siempre será distinto de null
     */
    suspend fun getUserById(): User {
        return repository.getUserById()
    }

    /**
     * Verifica si un usuario es un vendedor para poder validar si mostrar la confirmación de
     * convertirse en emprendedor o no
     *
     * @param email Correo electrónico del usuario
     * @param onResult Callback que se invoca con el resultado de la verificación
     *
     * @see repository.isUserSeller
     * @see onResult
     */
    fun checkIfUserIsSeller(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isSeller = repository.isUserSeller(email)
            onResult(isSeller)
        }
    }


}