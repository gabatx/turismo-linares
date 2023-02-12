package com.funcody.mapslinaresinteres.retrofit

class Repositorio {
    private val retrofitService = RetrofitHelper.getRetrofit()
    suspend fun getLugares() = retrofitService.getLugares()
}