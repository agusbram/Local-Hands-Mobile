package com.undef.localhandsbrambillafunes.ui.viewmodel.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🧠 ViewModel — Encargado de gestionar y exponer datos a la capa de UI.
 *
 * ## Función principal:
 * Mantiene y proporciona los datos necesarios para la interfaz de usuario, incluso durante
 * cambios de configuración como rotaciones de pantalla.
 *
 * ## ¿Para qué sirve?
 * - 🔄 Recupera datos desde el `Repository` y los expone a la UI mediante `State`, `LiveData` o `StateFlow`.
 * - 🎯 Contiene la lógica de presentación (formateo, validación, control de estado).
 * - 🚫 No contiene lógica de negocio ni de acceso directo a la base de datos.
 * - 📦 Actúa como una capa intermedia que separa la UI de la lógica de datos, promoviendo una arquitectura limpia y mantenible.
 *
 * ## Beneficios:
 * - Mejora la organización del código.
 * - Facilita la reutilización y testeo.
 * - Hace que la UI sea más declarativa y reactiva.
 */
@HiltViewModel
class ProductViewModel @Inject constructor(private val repository: ProductRepository) : ViewModel() {
    // Todos los productos disponibles
    /*val products: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //Inicializamos la BD con los productos migrados
    init {
        viewModelScope.launch {
            // Verificamos si la BD ya tiene productos
            val hasProducts = repository.getAllProducts().first().isNotEmpty() //First se utiliza porque tenemos un Flow en la lista de productos que traemos
            if(!hasProducts) {
                //Si no tiene productos, se insertan los productos migrados
                val migratedProducts = ProductProviderMigration.getAllAsEntities()
                repository.insertAll(migratedProducts)
            }
        }
    }*/

    // Estado interno y externo que expone la lista de productos
    private val _products = MutableStateFlow<List<Product>>(emptyList())

    /**
     * Estado observable de productos que expone una lista de productos a la UI.
     *
     * Se actualiza cuando se cargan productos desde la API.
     */
    val products: StateFlow<List<Product>> = _products

    /**
     * Carga todos los productos desde la API remota y actualiza el estado interno.
     *
     * Esta función lanza una corrutina en el `viewModelScope` para evitar
     * bloquear el hilo principal. Los productos obtenidos se almacenan en `_products`.
     *
     * Ideal para pantallas que muestran todos los productos públicos o disponibles.
     */
    fun loadAllProducts() {
        viewModelScope.launch {
            _products.value = repository.getProducts()
        }
    }

    /**
     * Carga los productos asociados a un dueño específico utilizando su `ownerId`.
     *
     * Utilizado para mostrar únicamente los productos creados por un emprendedor o usuario.
     * Los datos se obtienen desde la API remota y se actualiza el flujo interno `_products`.
     *
     * @param ownerId El ID del dueño (emprendedor) cuyos productos se desean obtener.
     */
    fun loadProductsByOwner(ownerId: Int?) {
        viewModelScope.launch {
            _products.value = repository.getProductsByOwnerId(ownerId)
        }
    }

    /**
     * Agrega un nuevo producto mediante la API remota y actualiza la lista del emprendedor.
     *
     * Esta función realiza un `POST` a través del repositorio y luego recarga los
     * productos del mismo `ownerId` para reflejar los cambios inmediatamente en la UI.
     *
     * @param product El nuevo producto a ser creado en el servidor.
     */
    fun addProductByApi(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product)
            loadProductsByOwner(product.ownerId)
        }
    }

    /**
     * Inserta un nuevo producto en la base de datos.
     *
     * Este método lanza una corrutina en el `viewModelScope` y delega la operación al repositorio.
     * Se utiliza al crear un nuevo producto desde la interfaz de usuario.
     *
     * @param product Instancia del producto a agregar.
     */
    fun addProduct(product: Product) = viewModelScope.launch {
        repository.insertProduct(product)
    }

    /**
     * Actualiza un producto existente en la base de datos.
     *
     * Ideal para operaciones de edición donde el usuario modifica un producto previamente creado.
     * La actualización se realiza de forma asincrónica dentro del `viewModelScope`.
     *
     * @param product Producto con los datos actualizados.
     */
    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    /**
     * Elimina un producto de la base de datos.
     *
     * Este método remueve permanentemente el producto proporcionado.
     * Se recomienda usarlo con confirmación del usuario, especialmente si el producto está publicado.
     *
     * @param product Producto que se desea eliminar.
     */
    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.deleteProduct(product)
    }

    fun getProductById(productId: Int): StateFlow<Product?> =
        repository.getProductById(productId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Inserta una lista de productos en la base de datos, reemplazando los existentes si hay conflicto.
     *
     * Esta función es útil para sincronizar múltiples productos desde una fuente externa
     * (como una API REST) o restaurar datos locales.
     *
     * @param products Lista de productos a insertar o actualizar.
     */
    fun insertAll(products: List<Product>) = viewModelScope.launch {
        repository.insertAll(products)
    }

    /**
     * Agrega un producto a la lista de favoritos del usuario actualmente autenticado.
     *
     * Este método utiliza el `userId` asociado al `ViewModel` para crear la relación
     * en la tabla de favoritos y lo ejecuta de forma asincrónica.
     *
     * @param productId ID del producto a marcar como favorito.
     */
    fun addFavorite(productId: Int, userId: Int) = viewModelScope.launch {
        repository.addFavorite(userId, productId)
    }

    /**
     * Elimina un producto de la lista de favoritos del usuario actual.
     *
     * La eliminación se basa en el `userId` y `productId` para identificar la relación
     * y ejecuta la operación de forma segura dentro del `viewModelScope`.
     *
     * @param productId ID del producto que se desea eliminar de favoritos.
     */
    fun removeFavorite(productId: Int, userId: Int) = viewModelScope.launch {
        repository.removeFavorite(userId, productId)
    }

    /**
     * Obtiene los productos que el usuario con el ID especificado está vendiendo.
     *
     * Esta función recupera una lista reactiva de productos pertenecientes al usuario
     * y la expone como un [StateFlow] que se inicializa de manera perezosa.
     *
     * @param ownerId ID del usuario del cual se desean obtener los productos.
     * @return Un [StateFlow] que contiene una lista de productos publicados por el usuario.
     */
        fun getMyProducts(ownerId: Int): StateFlow<List<Product>> =
            repository.getProductsByOwner(ownerId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Obtiene la lista de productos marcados como favoritos por el usuario.
     *
     * Esta función recupera una lista reactiva de productos que el usuario ha marcado como favoritos
     * y la expone como un [StateFlow], también inicializado de manera perezosa.
     *
     * @param userId ID del usuario del cual se desean obtener los productos favoritos.
     * @return Un [StateFlow] que contiene una lista de productos favoritos del usuario.
     */
    fun getFavorites(userId: Int): StateFlow<List<Product>> =
        repository.getFavoritesForUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}