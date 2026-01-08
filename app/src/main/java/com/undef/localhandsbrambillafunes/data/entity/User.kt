package com.undef.localhandsbrambillafunes.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entidad que representa un usuario en la base de datos
 *
 * @property id ID autoincremental (PK)
 * @property name Nombre del usuario
 * @property lastName Apellido del usuario
 * @property email Email único (indexado)
 * @property password Contraseña hasheada
 * @property isEmailVerified Estado de verificación de email
 * @property verificationCode Código temporal para verificación
 * @property createdAt Timestamp de creación
 */
@Entity(
    tableName = "UserEntity", // Nombre de la tabla en la base de datos
    indices = [Index(value = ["email"], unique = true)] // Índice único para email
)
data class User(
    @PrimaryKey(autoGenerate = true) // Clave primaria que se auto-genera
    val id: Int = 0, // Empieza en 1 automaticamente
    val name: String,
    val lastName: String,
    val email: String,
    @ColumnInfo(name = "password")
    val password: String, // IMPORTANTE: En producción, esto debería almacenarse hasheado
    val phone: String,
    val address: String,
    val role: UserRole,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false, // Por defecto no verificado
    val verificationCode: String? = null, // Código opcional para verificación TODO: tengo que hacer pruebas
    val createdAt: Long = System.currentTimeMillis() // Timestamp de creación
)