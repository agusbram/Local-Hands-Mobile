package com.undef.localhandsbrambillafunes.data.remote

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * Adaptador personalizado de Gson para la serialización y deserialización
 * de valores enteros (`Int`).
 *
 * Este `TypeAdapter` permite manejar de forma segura respuestas inconsistentes
 * provenientes del backend, donde un mismo campo puede llegar como:
 * - un número (`NUMBER`)
 * - una cadena numérica (`STRING`)
 * - un valor nulo (`NULL`)
 *
 * Su objetivo principal es evitar excepciones durante el parseo de JSON
 * y garantizar la estabilidad de la aplicación ante datos mal formateados
 * o inesperados.
 */
class IntTypeAdapter : TypeAdapter<Int>() {
    /**
     * Serializa un valor [Int] hacia JSON.
     *
     * Si el valor es `null`, se escribe explícitamente un valor nulo en el JSON.
     * En caso contrario, se escribe el valor numérico correspondiente.
     *
     * @param out Escritor JSON utilizado por Gson.
     * @param value Valor entero a serializar.
     * @throws IOException En caso de error de escritura.
     */
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Int?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    /**
     * Deserializa un valor JSON hacia un [Int].
     *
     * Comportamiento según el tipo de token recibido:
     * - `NULL`: consume el valor y retorna `null`.
     * - `NUMBER`: convierte directamente el número a `Int`.
     * - `STRING`: intenta convertir la cadena a `Int`; si falla, retorna `null`.
     * - Cualquier otro tipo: se ignora el valor y se retorna `null`.
     *
     * Este enfoque tolerante previene fallos de ejecución cuando
     * el backend no respeta estrictamente el contrato de tipos.
     *
     * @param in Lector JSON utilizado por Gson.
     * @return Valor entero deserializado o `null` si no es válido.
     * @throws IOException En caso de error de lectura.
     */
    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Int? {
        return when (`in`.peek()) {
            JsonToken.NULL -> {
                `in`.nextNull()
                null
            }
            JsonToken.NUMBER -> `in`.nextInt()
            JsonToken.STRING -> {
                try {
                    `in`.nextString().toIntOrNull()
                } catch (e: NumberFormatException) {
                    null
                }
            }
            else -> {
                `in`.skipValue()
                null
            }
        }
    }
}