package com.undef.localhandsbrambillafunes.data.model

import androidx.room.Embedded
import com.undef.localhandsbrambillafunes.data.entity.Product

/**
 * Clase de datos que representa un producto junto con la ubicación de su vendedor.
 *
 * Esta clase se utiliza para combinar los datos de un `Product` con la latitud y longitud
 * del `Seller` que lo vende, simplificando el acceso a esta información en la UI.
 *
 * @property product El objeto `Product` completo, embebido en esta clase.
 * @property latitude La latitud de la ubicación del vendedor.
 * @property longitude La longitud de la ubicación del vendedor.
 */
data class ProductWithLocation(
    @Embedded val product: Product,
    val latitude: Double?,
    val longitude: Double?
)
