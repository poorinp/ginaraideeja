package com.example.poorinp.map

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.annotation.IntegerRes
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.example.poorinp.map.R.id.bottom_navigator
import com.example.poorinp.map.common.common
import com.example.poorinp.map.model.Myplace
import com.example.poorinp.map.remote.IGoogleAPIService
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap


    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private lateinit var mLastLocation: Location
    private var mMarker: Marker? = null

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object {
        private val MY_PERMISSION_CODE: Int = 1000
    }

    lateinit var mServices: IGoogleAPIService

    internal lateinit var currentPlace: Myplace

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mServices = common.googleApiServices

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermision()) {
                buildLocationRequest()
                buildLocationCallBack()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        }
        else {
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        bottom_navigator.setOnNavigationItemReselectedListener {
            item ->
            when (item.itemId) {
                R.id.action_restaurant -> nearByRes("Restaurant")
            }

        }

    }

    private fun nearByRes(place: String) {
        mMap.clear()
        val url = getUrl(latitude, longitude, place)

        mServices.getNearByRes(url).enqueue(object : retrofit2.Callback<Myplace> {
            override fun onResponse(call: Call<Myplace>?, response: Response<Myplace>?) {
                currentPlace = response!!.body()!!

                if(response!!.isSuccessful) {
                    for (i in 0 until response!!.body()!!.results!!.size) {
                        val markerOptions = MarkerOptions()
                        val googlePlace = response.body()!!.results!![i]
                        val lat = googlePlace.geometry!!.location!!.lat
                        val lng = googlePlace.geometry!!.location!!.lng
                        val placeName = googlePlace.name
                        val latLng = LatLng(lat, lng)

                        markerOptions.position(latLng)
                        markerOptions.title(placeName)
                        if (place.equals("Restaurant"))
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))
                        else markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                        markerOptions.snippet(i.toString())

                        mMap!!.addMarker(markerOptions)
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(17f))


                    }

                }
            }

            override fun onFailure(call: Call<Myplace>?, t: Throwable?) {
                Toast.makeText(baseContext, ""+t!!.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getUrl(latitude: Double, longitude: Double, place: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=300")
        googlePlaceUrl.append("&type=$place")
        googlePlaceUrl.append("&key=AIzaSyC_5UmFIdLZL6SJI2FI2b8LvbgpkCJgDD4")
        Log.d("URL_DEBUG", googlePlaceUrl.toString())
        return  googlePlaceUrl.toString()


    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    private fun buildLocationCallBack() {

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation = p0!!.locations.get(p0!!.locations.size-1)

                if(mMarker != null) {
                    mMarker!!.remove()
                }

                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude

                val latLng = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions().position(latLng).title("Your Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                mMarker = mMap!!.addMarker(markerOptions)

                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    private fun buildLocationRequest() {

        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f

    }

    private fun checkLocationPermision(): Boolean {

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_CODE)}
            else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_CODE)
            }
            return false
        }
        else return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)  {
                        if (checkLocationPermision()) {

                            buildLocationRequest()
                            buildLocationCallBack()

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

                            mMap!!.isMyLocationEnabled = true
                        }
                    }
                    else {
                        Toast.makeText(this, "Permission_Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap!!.isMyLocationEnabled=true
            }
        }
        else
            mMap!!.isMyLocationEnabled=true

        mMap!!.setOnMarkerClickListener { marker ->
            common.currentResult = currentPlace!!.results!![Integer.parseInt(marker.snippet)]
            startActivity(Intent(this@MapsActivity, ViewPlace::class.java))
            true
        }

    }
}
