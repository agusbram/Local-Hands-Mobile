package com.undef.localhandsbrambillafunes.utils

import kotlin.math.*

/**
 * Utilidades para cálculos de ubicación y proximidad geográfica.
 */
object LocationUtils {
    
    // Radio de la Tierra en kilómetros
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine.
     * 
     * Esta fórmula es precisa para calcular la distancia de gran círculo entre dos puntos
     * en una esfera dados sus longitudes y latitudes.
     *
     * @param lat1 Latitud del primer punto (en grados)
     * @param lon1 Longitud del primer punto (en grados)
     * @param lat2 Latitud del segundo punto (en grados)
     * @param lon2 Longitud del segundo punto (en grados)
     * @return Distancia en kilómetros entre los dos puntos
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // Convertir grados a radianes
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_KM * c
    }

    /**
     * Convierte grados a radianes.
     *
     * @return Ángulo en radianes
     */
    private fun Double.toRadians(): Double = this * PI / 180.0

    /**
     * Formatea una distancia en kilómetros a un string legible.
     * Si es menor a 1 km, muestra en metros.
     *
     * @param distanceKm Distancia en kilómetros
     * @return String formateado (ej: "2.5 km" o "500 m")
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m"
            else -> String.format("%.1f km", distanceKm)
        }
    }
}
