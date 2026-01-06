package com.undef.localhandsbrambillafunes.data.dto

/**
 * Data Transfer Object (DTO) utilizado para la actualización parcial
 * de un vendedor (operación PATCH).
 *
 * Esta clase representa únicamente los campos que pueden ser
 * modificados desde la capa de presentación o red, evitando
 * exponer la entidad completa [Seller].
 *
 * Se utiliza principalmente para:
 * - Enviar datos de actualización al backend
 * - Mapear información editable en formularios
 * - Mantener separación entre el modelo de dominio y el modelo de transporte
 *
 * @property name Nombre del vendedor.
 * @property lastname Apellido del vendedor.
 * @property phone Número de teléfono de contacto.
 * @property address Dirección física del vendedor.
 * @property entrepreneurship Nombre del emprendimiento asociado.
 * @property photoUrl Url de la foto de perfil
 */
data class SellerPatchDTO(
    val name: String,
    val lastname: String,
    val phone: String,
    val address: String,
    val entrepreneurship: String,
    val photoUrl: String? = null
)