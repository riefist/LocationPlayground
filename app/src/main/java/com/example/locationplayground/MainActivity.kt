package com.example.locationplayground

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager : LocationManager
    var lastLocation : Location? = null
    val geocoder = Geocoder(this, Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        locationManager = LocationManager(this)

        button.setOnClickListener {
            getAddress()
        }



    }

    fun getAddress(){
        GlobalScope.launch(Dispatchers.Main){
            val address = fetchAddress()
            toast(address[0].getAddressLine(0))
        }
    }

    suspend fun fetchAddress() : List<Address> {
        return GlobalScope.async(Dispatchers.IO) {
            geocoder.getFromLocation(lastLocation!!.latitude, lastLocation!!.longitude, 1)
        }.await()
    }

    override fun onResume() {
        super.onResume()
        locationManager.listenLocationUpdate {
            lastLocation = it
            Log.d("MainActivity", "lat: ${it.latitude} lng: ${it.longitude}")
            updateUi(it)
        }
    }

    fun updateUi(location: Location){
        txtLat.text = "Latitude : ${location.latitude}"
        txtLng.text = "Longitude : ${location.longitude}"
    }

    override fun onPause() {
        super.onPause()
        locationManager.stopLocationUpdate()
    }

    private fun checkPermission(){
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    toast("granted")

                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    toast("denied")
                    response?.let {
                        if (it.isPermanentlyDenied){
                            alert("This app needs permission to use this feature. You can grant them in app settings.", "Need Permission") {
                                yesButton {  }
                            }.show()
                        }
                    }
                }

            }).check()
    }
}
