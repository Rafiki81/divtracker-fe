package com.rafiki81.divtracker.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // URLs según entorno (ver TESTING_GUIDE.md o ANDROID_AUTH_GUIDE.md)
    
    // Para emulador Android (localhost se mapea a 10.0.2.2)
    private const val BASE_URL_LOCAL_EMULATOR = "http://10.0.2.2:8080/"
    
    // Para dispositivo físico conectado a la misma red Wi-Fi
    // Reemplaza con la IP de tu máquina: ifconfig | grep "inet "
    private const val BASE_URL_LOCAL_DEVICE = "http://192.168.1.XXX:8080/"
    
    // Servidor de producción en AWS
    private const val BASE_URL_PRODUCTION = "http://divtracker-prod.eba-rghuxgtw.eu-west-1.elasticbeanstalk.com/"
    
    // 🔧 Cambia esto según tu entorno actual. 
    // Por defecto usaremos Producción para evitar problemas si no tienes el backend local corriendo.
    private const val BASE_URL = BASE_URL_PRODUCTION 
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = AuthInterceptor()
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val watchlistApiService: WatchlistApiService by lazy {
        retrofit.create(WatchlistApiService::class.java)
    }
    
    val deviceApiService: DeviceApiService by lazy {
        retrofit.create(DeviceApiService::class.java)
    }
}
