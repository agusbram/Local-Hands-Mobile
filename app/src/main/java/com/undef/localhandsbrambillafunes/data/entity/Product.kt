package com.undef.localhandsbrambillafunes.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.undef.localhandsbrambillafunes.data.db.Converters

/**
 * Modelo de datos que representa un producto almacenado en la base de datos local.
 *
 * Esta entidad está mapeada a la tabla **ProductEntity** mediante Room.
 *
 * Contiene toda la información relevante para describir un producto, incluyendo su nombre,
 * descripción, productor, categoría, imágenes, precio y ubicación geográfica.
 *
 * ## Restricciones:
 * - **images**: debe contener entre 1 y 10 URLs válidas.
 * - **id**: se genera automáticamente como clave primaria.
 *
 * @property id Identificador único del producto. Se genera automáticamente.
 * @property name Nombre del producto.
 * @property description Descripción detallada del producto.
 * @property producer Nombre del vendedor o productor del producto.
 * @property category Categoría a la que pertenece el producto (ej. "Electrónica", "Ropa", etc.).
 * @property images Lista de URLs que apuntan a imágenes del producto. Debe contener entre 1 y 10 elementos.
 * @property price Precio del producto expresado en moneda local.
 * @property location Ciudad o lugar donde se encuentra disponible el producto.
 */
@Entity(tableName = "ProductEntity")
@TypeConverters(Converters::class)
data class Product(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val description: String,
    val producer: String,
    val category: String,
    val ownerId: Int?, // null = producto público, no es de ningún emprendedor aún
    val images: List<String>, // Mínimo 1 imagen, máximo 10
    val price: Double,
    val location: String
)