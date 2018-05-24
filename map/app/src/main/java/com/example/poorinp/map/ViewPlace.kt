package com.example.poorinp.map

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.poorinp.map.common.common
import com.example.poorinp.map.model.PlaceDetail
import com.example.poorinp.map.remote.IGoogleAPIService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_place.*
import retrofit2.Call
import retrofit2.Response

class ViewPlace : AppCompatActivity() {

    internal lateinit var mServices: IGoogleAPIService

    var mPlace: PlaceDetail?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_place)

        mServices = common.googleApiServices

        place_name.text=""
        place_address.text=""
        place_open_hour.text=""

        btn_show_map.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.results!!.url))
            startActivity(mapIntent)
        }

        if (common.currentResult!!.photos != null && common.currentResult!!.photos!!.size > 0) {
            Picasso.with(this).load(getPhotoOfPlace(common.currentResult!!.photos!![0].photo_reference!!, 1000)).into(photos)
        }

        if (common.currentResult!!.rating != null) {
            rating_bar.rating = common.currentResult!!.rating.toFloat()
        }
        else { rating_bar.visibility = View.GONE }

        if (common.currentResult!!.opening_hour != null) {
            place_open_hour.text = "Open Now: " + common!!.currentResult!!.opening_hour!!.open_now
        }
        else { place_open_hour.visibility = View.GONE }

        mServices.getDetailPlace(getPlaceDetailUrl(common.currentResult!!.place_id!!))
                .enqueue(object : retrofit2.Callback<PlaceDetail> {
                    override fun onResponse(call: Call<PlaceDetail>?, response: Response<PlaceDetail>?) {
                        mPlace = response!!.body()
                        place_address.text = mPlace!!.results!!.formatted_address
                        place_name.text = mPlace!!.results!!.name

                    }

                    override fun onFailure(call: Call<PlaceDetail>?, t: Throwable?) {
                       Toast.makeText(baseContext, ""+t!!.message, Toast.LENGTH_SHORT).show()
                    }

                })
    }

    private fun getPlaceDetailUrl(place_id: String): String {

        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/details/json")
        url.append("?placeid=$place_id")
        url.append("&key=AIzaSyC_5UmFIdLZL6SJI2FI2b8LvbgpkCJgDD4")
        return url.toString()
    }

    private fun getPhotoOfPlace(photo_reference: String, maxWidth: Int): String{

        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        url.append("?maxwidth=$maxWidth")
        url.append("&photoreference=$photo_reference")
        url.append("&key=AIzaSyC_5UmFIdLZL6SJI2FI2b8LvbgpkCJgDD4")
        return url.toString()
    }
}
