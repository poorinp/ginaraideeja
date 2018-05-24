package com.example.poorinp.map.remote

import com.example.poorinp.map.model.Myplace
import com.example.poorinp.map.model.PlaceDetail
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun getNearByRes(@Url url: String) : Call<Myplace>


    @GET
    fun getDetailPlace(@Url url: String) : Call<PlaceDetail>
}