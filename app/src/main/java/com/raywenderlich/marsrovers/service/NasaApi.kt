package com.raywenderlich.marsrovers.service

import com.raywenderlich.marsrovers.models.PhotoList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface NasaApi {
    @GET("mars-photos/api/v1/rovers/{rover}/photos?sol=1000&api_key=D0gjKFSNtBcW9kgIf6AJ1m5hTXQJ6LsIkGhpFZ27")
    fun getPhotos(@Path("rover") rover: String) : Call<PhotoList>
}
