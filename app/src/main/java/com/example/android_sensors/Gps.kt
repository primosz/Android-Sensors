package com.example.android_sensors

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*


class Gps : AppCompatActivity() {
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val locationInterval: Long = 2000
    private val locationFastestInterval: Long = 1000
    lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10
    private var outsideArea: Boolean = false

    lateinit var latTv: TextView
    lateinit var longTv: TextView
    lateinit var statusTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)

        mLocationRequest = LocationRequest()

        latTv = findViewById(R.id.txtLat);
        longTv = findViewById(R.id.txtLong);
        statusTv = findViewById(R.id.statusTv);
        statusTv.text = getString(R.string.inNe)
        statusTv.setTextColor(Color.GREEN)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

     override fun onResume(){
         super.onResume()
         if (checkPermissionForLocation(this)) {
             startLocationUpdates()
             Toast.makeText(this, getString(R.string.listening), Toast.LENGTH_SHORT).show()
         }
     }

    override fun onPause(){
        super.onPause()
        stoplocationUpdates()
        statusTv.text = getString(R.string.stopped)
        Toast.makeText(this, getString(R.string.stoppedListening), Toast.LENGTH_SHORT).show()
    }

    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.enableGpsQ))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.y)) { _, _ ->
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11)
            }
            .setNegativeButton(getString(R.string.n)) { dialog, _ ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    protected fun startLocationUpdates() {
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.interval = locationInterval
        mLocationRequest!!.fastestInterval = locationFastestInterval
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun onLocationChanged(location: Location) {
        mLastLocation = location
        latTv.text = "Latitude : ${mLastLocation.latitude}"
        longTv.text = "Longitude : ${mLastLocation.longitude}"
        checkIfOutOfArea()
    }

    private fun checkIfOutOfArea() {
        if (mLastLocation.latitude < 51.101839 || mLastLocation.latitude > 51.103616
            || mLastLocation.longitude < 17.011037 || mLastLocation.longitude > 17.014430){
            if(!outsideArea){
                Toast.makeText(this, getString(R.string.leftNe), Toast.LENGTH_SHORT).show()
                outsideArea = true
                statusTv.text = getString(R.string.outOfNe)
                statusTv.setTextColor(Color.RED)
            }
        }
        else {
            if(outsideArea) {
                Toast.makeText(this, getString(R.string.backInNe), Toast.LENGTH_SHORT).show()
                outsideArea = false
                statusTv.text = getString(R.string.inNe)
                statusTv.setTextColor(Color.GREEN)
            }
        }
    }

    private fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, getString(R.string.denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }

}
