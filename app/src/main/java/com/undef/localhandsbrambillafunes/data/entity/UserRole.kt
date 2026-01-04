package com.undef.localhandsbrambillafunes.data.entity

/**
 * Enumeración que define los roles posibles de un usuario
 * dentro del sistema.
 *
 * Esta enumeración se utiliza para:
 * - Diferenciar el comportamiento y permisos según el tipo de usuario
 * - Controlar flujos de navegación y lógica de negocio
 * - Facilitar la validación de acciones permitidas en la aplicación
 *
 * Valores disponibles:
 * - [CLIENT]: Usuario final que consume productos o servicios.
 * - [SELLER]: Usuario que ofrece productos o servicios a través de la plataforma.
 */
enum class UserRole {
    CLIENT,
    SELLER
}