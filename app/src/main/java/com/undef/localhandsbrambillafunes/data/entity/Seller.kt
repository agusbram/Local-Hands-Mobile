package com.undef.localhandsbrambillafunes.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidad que representa a un vendedor dentro del sistema.
 *
 * Esta clase se utiliza como modelo de datos persistente en la base de datos local
 * mediante Room. Cada instancia de [Seller] corresponde a un registro de la tabla
 * "SellerEntity".
 *
 * Existe una relación uno a uno entre [User] y [Seller]. El identificador del vendedor
 * coincide con el identificador del usuario al que pertenece, funcionando
 * simultáneamente como clave primaria y clave foránea.
 *
 * La integridad referencial es garantizada por Room mediante una restricción
 * de clave foránea. Al eliminar un [User], el [Seller] asociado se elimina
 * automáticamente gracias a la política de eliminación en cascada.
 *
 * @property id Identificador único del vendedor.
 * Corresponde al identificador del usuario asociado ([User.id]).
 * Se define como clave primaria y clave foránea, y **no se genera automáticamente**.
 *
 * @property name Nombre del vendedor.
 *
 * @property lastname Apellido del vendedor.
 *
 * @property email Dirección de correo electrónico del vendedor.
 *
 * @property phone Número de teléfono de contacto del vendedor.
 *
 * @property entrepreneurship Nombre del emprendimiento asociado al vendedor.
 *
 * @property address Dirección física del vendedor o del emprendimiento.
 */
@Entity(
    tableName = "SellerEntity",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Seller(
    @PrimaryKey
    val id: Int,
    val name: String,
    val lastname: String,
    val email: String,
    val phone: String,
    val photoUrl: String? = null,
    val entrepreneurship: String,
    val address: String
)