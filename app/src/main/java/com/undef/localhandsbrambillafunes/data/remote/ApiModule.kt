package com.undef.localhandsbrambillafunes.data.remote
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Módulo de Hilt que provee las dependencias relacionadas con Retrofit y la API remota.
 *
 * Este módulo está instalado en el componente Singleton, por lo que todas las dependencias
 * proporcionadas serán de ámbito global (singleton) durante todo el ciclo de vida de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    // Dirección base del servidor de backend. 10.0.2.2 redirige al localhost del host en el emulador Android.
    private const val BASE_URL = "http://10.0.2.2:3000/"

    /**
     * Proporciona una instancia singleton de Retrofit configurada con la base URL
     * y un convertidor Gson para la serialización y deserialización de datos JSON.
     *
     * @return Una instancia de [Retrofit] lista para realizar peticiones HTTP.
     */
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * Proporciona una implementación de la interfaz [ApiService] usando Retrofit.
     *
     * Esta interfaz define los endpoints que se comunicarán con el backend.
     *
     * @param retrofit La instancia de Retrofit ya configurada.
     * @return Una implementación de [ApiService] lista para inyección.
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
