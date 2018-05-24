package com.example.poorinp.map.common

import com.example.poorinp.map.model.Results
import com.example.poorinp.map.remote.IGoogleAPIService
import com.example.poorinp.map.remote.RetrofitClient

object common {
    private val GOOGLE_API="https://maps.googleapis.com/"

    var currentResult:Results?= null

    val googleApiServices: IGoogleAPIService get() = RetrofitClient.getClient(GOOGLE_API).create(IGoogleAPIService::class.java)
}