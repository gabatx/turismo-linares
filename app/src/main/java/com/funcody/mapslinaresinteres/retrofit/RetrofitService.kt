package com.funcody.mapslinaresinteres.retrofit

import com.funcody.mapslinaresinteres.models.Lugar
import retrofit2.Response
import retrofit2.http.GET

interface RetrofitService {
    // RECIBIR TODOS LOS LUGARES DE INTERES
    @GET("getJson.php")
    suspend fun getLugares(): Response<ArrayList<Lugar>>
}