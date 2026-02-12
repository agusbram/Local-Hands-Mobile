package com.undef.localhandsbrambillafunes.data.repository

import android.util.Log
import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.dao.ProductDao
import com.undef.localhandsbrambillafunes.data.dto.ProductCreateDTO
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.entity.Favorite
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.remote.ApiService
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Tests para ProductRepository - Funcionalidad crítica de gestión de productos
 * 
 * Valida:
 * - Sincronización de productos con API
 * - CRUD de productos (Create, Read, Update, Delete)
 * - Búsqueda y filtrado de productos
 * - Gestión de favoritos
 */
class ProductRepositoryTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var productDao: ProductDao
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var apiService: ApiService
    private lateinit var sellerRepository: SellerRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        productDao = mockk()
        favoriteDao = mockk()
        apiService = mockk()
        sellerRepository = mockk()
        productRepository = ProductRepository(productDao, favoriteDao, apiService, sellerRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        clearAllMocks()
    }

    // Test de Sincronización con API
    @Test
    fun syncProductsWithApi_success() = runTest {
        // Given
        val productsFromApi = listOf(
            createTestProduct(1, "Product 1"),
            createTestProduct(2, "Product 2"),
            createTestProduct(3, "Product 3")
        )

        coEvery { apiService.getProducts() } returns productsFromApi
        coEvery { productDao.addProduct(any()) } returns 1L

        // When
        productRepository.syncProductsWithApi()

        // Then
        coVerify { apiService.getProducts() }
        coVerify(exactly = 3) { productDao.addProduct(any()) }
    }

    @Test
    fun syncProductsWithApi_handlesDuplicatesWithUpdate() = runTest {
        // Given
        val productsFromApi = listOf(createTestProduct(1, "Product 1"))

        coEvery { apiService.getProducts() } returns productsFromApi
        coEvery { productDao.addProduct(any()) } throws Exception("Duplicate")
        coEvery { productDao.updateProduct(any()) } just Runs

        // When
        productRepository.syncProductsWithApi()

        // Then
        coVerify { apiService.getProducts() }
        coVerify { productDao.addProduct(any()) }
        coVerify { productDao.updateProduct(any()) }
    }

    // Tests de Agregar Producto
    @Test
    fun addProductWithSync_withApiSuccess_returnsProduct() = runTest {
        // Given
        val product = createTestProduct(0, "New Product", ownerId = 1)
        val seller = Seller(
            id = 1,
            name = "Test",
            lastname = "Seller",
            email = "seller@example.com",
            phone = "1234567890",
            address = "Test Address",
            entrepreneurship = "Test Entrepreneurship",
            photoUrl = null
        )
        val createdProduct = product.copy(id = 100)

        coEvery { sellerRepository.getSellerByIdNonFlow(1) } returns seller
        coEvery { apiService.getProducts() } returns listOf()
        coEvery { apiService.addProductDTO(any()) } returns createdProduct
        coEvery { productDao.addProduct(createdProduct) } returns 1L

        // When
        val result = productRepository.addProductWithSync(product)

        // Then
        assertNotNull("Debe retornar un producto", result)
        assertEquals("ID debe ser asignado", 100, result.id)
        coVerify { sellerRepository.getSellerByIdNonFlow(1) }
        coVerify { apiService.addProductDTO(any()) }
        coVerify { productDao.addProduct(createdProduct) }
    }

    @Test
    fun addProductWithSync_withApiFail_savesLocally() = runTest {
        // Given
        val product = createTestProduct(0, "New Product", ownerId = 1)

        coEvery { sellerRepository.getSellerByIdNonFlow(1) } returns null
        coEvery { apiService.getProducts() } throws Exception("Network error")
        coEvery { productDao.addProduct(any()) } returns 1L

        // When
        val result = productRepository.addProductWithSync(product)

        // Then
        assertNotNull("Debe retornar un producto", result)
        assertTrue("Debe asignar un ID temporal", result.id > 0)
        coVerify { productDao.addProduct(any()) }
    }

    // Tests de Actualizar Producto
    @Test
    fun updateProductWithSync_withApiSuccess_returnsTrue() = runTest {
        // Given
        val product = createTestProduct(1, "Updated Product")

        coEvery { apiService.updateProduct(product.id, product) } returns product
        coEvery { productDao.updateProduct(product) } just Runs

        // When
        val result = productRepository.updateProductWithSync(product)

        // Then
        assertTrue("Debe retornar true con API exitosa", result)
        coVerify { apiService.updateProduct(product.id, product) }
        coVerify { productDao.updateProduct(product) }
    }

    @Test
    fun updateProductWithSync_withApiFail_updatesLocallyReturnsFalse() = runTest {
        // Given
        val product = createTestProduct(1, "Updated Product")

        coEvery { apiService.updateProduct(product.id, product) } throws Exception("Network error")
        coEvery { productDao.updateProduct(product) } just Runs

        // When
        val result = productRepository.updateProductWithSync(product)

        // Then
        assertFalse("Debe retornar false con API fallida", result)
        coVerify { productDao.updateProduct(product) }
    }

    // Tests de Eliminar Producto
    @Test
    fun deleteProductWithSync_withApiSuccess_returnsTrue() = runTest {
        // Given
        val product = createTestProduct(1, "Product to Delete")
        val response = mockk<Response<Unit>>()

        coEvery { apiService.deleteProduct(product.id) } returns response
        coEvery { productDao.deleteProduct(product) } just Runs

        // When
        val result = productRepository.deleteProductWithSync(product)

        // Then
        assertTrue("Debe retornar true con API exitosa", result)
        coVerify { apiService.deleteProduct(product.id) }
        coVerify { productDao.deleteProduct(product) }
    }

    @Test
    fun deleteProductWithSync_withApiFail_deletesLocallyReturnsFalse() = runTest {
        // Given
        val product = createTestProduct(1, "Product to Delete")

        coEvery { apiService.deleteProduct(product.id) } throws Exception("Network error")
        coEvery { productDao.deleteProduct(product) } just Runs

        // When
        val result = productRepository.deleteProductWithSync(product)

        // Then
        assertFalse("Debe retornar false con API fallida", result)
        coVerify { productDao.deleteProduct(product) }
    }

    // Tests de Lectura de Productos
    @Test
    fun getAllProducts_returnsFlow() = runTest {
        // Given
        val products = listOf(
            createTestProduct(1, "Product 1"),
            createTestProduct(2, "Product 2")
        )
        val flowProducts = flowOf(products)

        every { productDao.getAllProducts() } returns flowProducts

        // When
        val result = productRepository.getAllProducts().first()

        // Then
        assertEquals("Debe retornar lista de productos", products, result)
        assertEquals("Debe retornar 2 productos", 2, result.size)
    }

    @Test
    fun getProductById_returnsProduct() = runTest {
        // Given
        val productId = 1
        val product = createTestProduct(productId, "Test Product")
        val flowProduct = flowOf(product)

        every { productDao.getProductById(productId) } returns flowProduct

        // When
        val result = productRepository.getProductById(productId).first()

        // Then
        assertNotNull("Debe retornar el producto", result)
        assertEquals("Debe retornar el producto correcto", product, result)
    }

    @Test
    fun getProductsByCategory_returnsFilteredProducts() = runTest {
        // Given
        val category = "Electronics"
        val products = listOf(
            createTestProduct(1, "Product 1", category = category),
            createTestProduct(2, "Product 2", category = category)
        )
        val flowProducts = flowOf(products)

        every { productDao.getProductsByCategory(category) } returns flowProducts

        // When
        val result = productRepository.getProductsByCategory(category).first()

        // Then
        assertEquals("Debe retornar productos de la categoría", products, result)
        assertEquals("Debe retornar 2 productos", 2, result.size)
    }

    @Test
    fun searchProducts_returnsMatchingProducts() = runTest {
        // Given
        val query = "Test"
        val products = listOf(
            createTestProduct(1, "Test Product 1"),
            createTestProduct(2, "Test Product 2")
        )
        val flowProducts = flowOf(products)

        every { productDao.searchProducts(query) } returns flowProducts

        // When
        val result = productRepository.searchProducts(query).first()

        // Then
        assertEquals("Debe retornar productos que coincidan con la búsqueda", products, result)
        assertEquals("Debe retornar 2 productos", 2, result.size)
    }

    @Test
    fun getProductsByOwner_returnsOwnerProducts() = runTest {
        // Given
        val ownerId = 1
        val products = listOf(
            createTestProduct(1, "Product 1", ownerId = ownerId),
            createTestProduct(2, "Product 2", ownerId = ownerId)
        )
        val flowProducts = flowOf(products)

        every { productDao.getProductsByOwner(ownerId) } returns flowProducts

        // When
        val result = productRepository.getProductsByOwner(ownerId).first()

        // Then
        assertEquals("Debe retornar productos del propietario", products, result)
        assertEquals("Debe retornar 2 productos", 2, result.size)
    }

    @Test
    fun getAllCategories_returnsUniqueCategories() = runTest {
        // Given
        val categories = listOf("Electronics", "Food", "Clothing")
        val flowCategories = flowOf(categories)

        every { productDao.getAllCategories() } returns flowCategories

        // When
        val result = productRepository.getAllCategories().first()

        // Then
        assertEquals("Debe retornar lista de categorías", categories, result)
        assertEquals("Debe retornar 3 categorías", 3, result.size)
    }

    // Tests de Favoritos
    @Test
    fun getFavoritesForUser_returnsFavoriteProducts() = runTest {
        // Given
        val userId = 1
        val products = listOf(
            createTestProduct(1, "Favorite 1"),
            createTestProduct(2, "Favorite 2")
        )
        val flowProducts = flowOf(products)

        every { favoriteDao.getFavoritesForUser(userId) } returns flowProducts

        // When
        val result = productRepository.getFavoritesForUser(userId).first()

        // Then
        assertEquals("Debe retornar productos favoritos", products, result)
        assertEquals("Debe retornar 2 favoritos", 2, result.size)
    }

    @Test
    fun addFavorite_success() = runTest {
        // Given
        val userId = 1
        val productId = 100

        coEvery { favoriteDao.addFavorite(any()) } just Runs

        // When
        productRepository.addFavorite(userId, productId)

        // Then
        coVerify { 
            favoriteDao.addFavorite(match { 
                it.userId == userId && it.productId == productId 
            }) 
        }
    }

    @Test
    fun removeFavorite_success() = runTest {
        // Given
        val userId = 1
        val productId = 100

        coEvery { favoriteDao.removeFavByUserAndProduct(userId, productId) } just Runs

        // When
        productRepository.removeFavorite(userId, productId)

        // Then
        coVerify { favoriteDao.removeFavByUserAndProduct(userId, productId) }
    }

    @Test
    fun updateProductsProducerByOwner_updatesAllOwnerProducts() = runTest {
        // Given
        val ownerId = 1
        val newProducer = "New Entrepreneurship"
        val products = listOf(
            createTestProduct(1, "Product 1", ownerId = ownerId, producer = "Old Producer"),
            createTestProduct(2, "Product 2", ownerId = ownerId, producer = "Old Producer")
        )
        val flowProducts = flowOf(products)

        every { productDao.getProductsByOwner(ownerId) } returns flowProducts
        coEvery { apiService.updateProduct(any(), any()) } returns mockk()
        coEvery { productDao.updateProduct(any()) } just Runs

        // When
        productRepository.updateProductsProducerByOwner(ownerId, newProducer)

        // Then
        coVerify(exactly = 2) { apiService.updateProduct(any(), any()) }
        coVerify(exactly = 2) { productDao.updateProduct(match { it.producer == newProducer }) }
    }

    // Helper function to create test products
    private fun createTestProduct(
        id: Int,
        name: String,
        category: String = "Test Category",
        ownerId: Int? = null,
        producer: String = "Test Producer"
    ): Product {
        return Product(
            id = id,
            name = name,
            description = "Test Description",
            producer = producer,
            category = category,
            ownerId = ownerId,
            images = listOf(),
            price = 100.0,
            location = "Test Location"
        )
    }
}
