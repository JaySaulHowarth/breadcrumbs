package com.example.breadcrumbsrework

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.media.ExifInterface
import android.media.ExifInterface.TAG_DATETIME
import android.media.ExifInterface.TAG_DATETIME_ORIGINAL
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.view.View
import android.view.Window
import android.widget.ImageButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterManager
/*This it the main activity for the application, it also contains the functionality for creating the map*/
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var clusterManager: ClusterManager<ImagePin>
    private lateinit var clusterRenderer: MyClusterManagerRenderer
    private lateinit var darkButton: ImageButton
    private var darkmode = false
    private var permissionsList = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    /*List of permission codes*/
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val READ_STORAGE_PERMISSION_REQUEST_CODE = 2
        private const val WRITE_STORAGE_PERMISSION_REQUEST_CODE = 3
        private const val ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setSupportActionBar(findViewById(R.id.app_toolbar))
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        darkButton = findViewById(R.id.dark_mode)
    }


    /*Called when the map is displayed*/
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.mapstyle_default))
        map.uiSettings.isZoomControlsEnabled = true

        /*Button that toggles darkmode and default mode. Darkmode uses true black to save battery on OLED screens and reduce eye strain*/
        darkButton.setOnClickListener{
            if(darkmode){
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.mapstyle_default))
                darkButton.setImageResource(R.drawable.ic_dark_mode_off)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorAccent, this.theme)))
                darkmode = false
            }else{
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.mapstyle_dark))
                darkButton.setImageResource(R.drawable.ic_dark_mode_on)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorPrimaryDark, this.theme)))
                darkmode = true
            }
        }
        setUpMap()
    }
    /*Check permissions and ask for them if needed, if permissions are already granted then call permission sensitive functionality
    * If permissions are not granted then they will be requested and the onRequestPermissionResult will call the permission sensitive functions if they are granted*/
    private fun setUpMap() {
        if (checkPermissions()) {
            locateUserOnMap()
            plotImagesOnMap()
        }
    }

    /*Check which permissions have been granted, add those which are not granted to a required permissions list and then request them from the user*/
    private fun checkPermissions(): Boolean {
        val requiredPermissions = mutableListOf<String>()
        for (p in permissionsList) {
            val permGranted = checkSelfPermission(p)
            if (permGranted != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(p)
            }
        }
        if (requiredPermissions.isNotEmpty()) {
            requestPermissions(requiredPermissions.toTypedArray(), ASK_MULTIPLE_PERMISSION_REQUEST_CODE)
            return false
        }
        return true
    }

    /*Once the user has responded to the permission popup, if they accept the permissions then call permission sensitive functionality*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ASK_MULTIPLE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                plotImagesOnMap()
                locateUserOnMap()
            }
        }
    }

    /*Get the users location and move the map camera to their location
    * This is done as the user will want to start looking at the map from a familar locaiton*/
    private fun locateUserOnMap() {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            /*Here a blue dot is placed on the map that shows the users location
                * the map is also centered on their location*/
            map.isMyLocationEnabled = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    /*Check the users external storage (SD card) for any images. For each image found get the filepath, LatLng and original date*/
    private fun plotImagesOnMap() {
        /*Using mediastore and a cursor to collect the paths of all images on the device*/
        val columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
        val order = MediaStore.Images.Media._ID
        val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, order)
        val imagePathList = mutableListOf<String>()
        var imagePath: String
        if(cursor != null && cursor.moveToFirst()){
            while(cursor.moveToNext()){
                imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                imagePathList.add(imagePath)
            }
        }else{
            return
        }
        cursor.close()

        /*Here the cluster manager is initialised, this will be responsible for implementing advanced marker features
        * Such as having images for markers and clustering them based on zoom level*/
        clusterManager = ClusterManager(applicationContext, map)

        /*When a cluster is clicked replace map fragment with the ImageViewCluster fragment*/
        clusterManager.setOnClusterClickListener {
            val clusterImgPaths = ArrayList<String>()
            val clusterImgDates = ArrayList<String>()
            val args = Bundle()
            val imageViewCluster = ImageViewCluster()
            for(i in it.items.toMutableList()){
                clusterImgPaths.add(i.getImagePath())
                clusterImgDates.add(i.getDate())
            }
            args.putStringArrayList("imgPathList", clusterImgPaths)
            args.putStringArrayList("imgDateList", clusterImgDates)
            imageViewCluster.arguments = args
            val manager : FragmentManager = supportFragmentManager
            manager.beginTransaction().replace(R.id.map, imageViewCluster).addToBackStack(null).commit()
            true
        }
        /*When a single item is clicked re-use the viewpager fragment but only add a single item to it
        * (this reduces a lot of redundant code)*/
        clusterManager.setOnClusterItemClickListener {
            val clusterImgPaths = ArrayList<String>()
            val clusterImgDates = ArrayList<String>()
            val args = Bundle()
            val imageViewCluster = ImageViewCluster()
            clusterImgPaths.add(it.getImagePath())
            clusterImgDates.add(it.getDate())

            args.putStringArrayList("imgPathList", clusterImgPaths)
            args.putStringArrayList("imgDateList", clusterImgDates)
            imageViewCluster.arguments = args
            val manager : FragmentManager = supportFragmentManager
            manager.beginTransaction().replace(R.id.map, imageViewCluster).addToBackStack(null).commit()
            true
        }
        /*Linking the cluster manager with my custom renderer and linking the manager with my map*/
        clusterRenderer = MyClusterManagerRenderer(applicationContext, map, clusterManager)
        clusterManager.renderer = clusterRenderer
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)

        /*For each image found in storage use exifinterface to get metadata such as location and the date the image was captured*/
        var exif: ExifInterface
        for(i in 0 until imagePathList.size){
            exif = ExifInterface(imagePathList[i])
            val location = FloatArray(2)
            exif.getLatLong(location)
            val date = exif.getAttribute(TAG_DATETIME)
            /*By default if an image hasn't been geotagged its tag is 0.00,0.00
            * this places images in the atlantic ocean, I'm making an assumption here that the chances of a user
            * taking a photo at exactly 0.00,0.00 are very slim so I ignore any images with this geotag
            * this prevents images that have not been taken by the user from being plotted to the map*/
            if (location[0] != 0.00f && location [1] != 0.00f && date != null) {
                /*Creating an imagePin for each image found and putting it in the cluster manager*/
                val imagePin = ImagePin(applicationContext, location[0].toDouble(), location[1].toDouble(), imagePathList[i], date)
                clusterManager.addItem(imagePin)
            }
        }
        clusterManager.cluster()
    }



}
