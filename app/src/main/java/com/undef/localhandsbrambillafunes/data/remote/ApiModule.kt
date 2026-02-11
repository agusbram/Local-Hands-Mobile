package com.undef.localhandsbrambillafunes.data.remote
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias de Hilt encargado de proveer
 * las configuraciones y dependencias necesarias para la comunicación
 * con la API remota mediante Retrofit.
 *
 * Responsabilidades principales:
 * - Configurar y proveer una instancia personalizada de [Gson].
 * - Proveer una instancia singleton de [Retrofit] con conversión JSON.
 * - Exponer la implementación de [ApiService] para el consumo de la API.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    /**
     * URL base del servidor backend.
     *
     * La dirección `10.0.2.2` permite al emulador Android acceder
     * al `localhost` de la máquina anfitriona.
     */
    private const val BASE_URL = "http://10.0.2.2:3000/"

    /**
     * Se registra un [IntTypeAdapter] para manejar correctamente la
     * serialización y deserialización de valores enteros, tanto primitivos
     * como sus wrappers, evitando errores ante valores inesperados desde la API.
     *
     * @return Instancia configurada de [Gson].
     */
    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(Int::class.java, IntTypeAdapter())
            .registerTypeAdapter(Int::class.javaPrimitiveType, IntTypeAdapter())
            .create()


    /**
     * Proporciona una instancia singleton de Retrofit configurada con la base URL
     * y un convertidor Gson para la serialización y deserialización de datos JSON.
     *
     * @return Una instancia de [Retrofit] lista para realizar peticiones HTTP.
     */
    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
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
