package com.undef.localhandsbrambillafunes.data.dto

/**
 * Data Transfer Object (DTO) utilizado para la creación de un producto a través de la API.
 *
 * Este objeto encapsula la información necesaria para registrar un nuevo producto
 * en el sistema. No requiere que el identificador sea provisto por el cliente,
 * ya que dicho valor es generado automáticamente por el servidor.
 *
 * Se utiliza principalmente en operaciones de tipo *create* (POST),
 * actuando como contrato de datos entre la capa de presentación
 * y la capa de comunicación con la API.
 *
 * @property id Identificador del producto. Debe ser `null` al momento de la creación,
 *              ya que será asignado por el servidor.
 * @property name Nombre del producto.
 * @property description Descripción detallada del producto.
 * @property producer Nombre del productor, marca o emprendimiento asociado.
 * @property category Categoría a la que pertenece el producto.
 * @property ownerId Identificador del usuario propietario del producto.
 *                  Puede ser `null` si se infiere desde el contexto de autenticación.
 * @property images Lista de rutas o URLs de las imágenes asociadas al producto.
 * @property price Precio del producto.
 * @property location Ubicación geográfica o ciudad donde se ofrece el producto.
 */
data class ProductCreateDTO(
    val id: Int?,
    val name: String,
    val description: String,
    val producer: String,
    val category: String,
    val ownerId: Int?,
    val images: List<String>,
    val price: Double,
    val location: String
)
