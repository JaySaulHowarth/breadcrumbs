package com.example.breadcrumbsrework

import android.content.Context
import android.widget.ImageView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class ImagePin constructor(context: Context?, lat: Double, lng: Double, imagePath: String, date: String): ClusterItem {

    /*Map position of the cluster item*/
    private var mPosition = LatLng(lat, lng)
    /*Info window text (currently unused)*/
    private lateinit var mTitle : String
    private lateinit var mSnippet : String
    private var date = date
    /*Directory path to the image that is being used as the cluster item icon*/
    private var imagePath = imagePath

    /*The view that is populated with an image when this imagepin appears as a cluster item*/
    private var imageView = ImageView(context)
    /*The view that is populated with an image when this imagepin is first in a cluster*/
    private var clusterImageView = ImageView(context)

    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String {
        return "Test"
    }

    override fun getSnippet(): String {
        return "Test snippet"
    }

    fun getImagePath(): String{
        return imagePath
    }

    fun getImageView(): ImageView{
        return imageView
    }

    fun getClusterImageView(): ImageView{
        return clusterImageView
    }

    fun getDate(): String{
        return date
    }

}