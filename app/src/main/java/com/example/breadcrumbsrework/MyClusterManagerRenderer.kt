package com.example.breadcrumbsrework


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

/*This class is responsible for rendering the pins on the map, clustering them together and adding images to each pin*/
class MyClusterManagerRenderer(context: Context, map: GoogleMap?, clusterManager: ClusterManager<ImagePin>?) :
    DefaultClusterRenderer<ImagePin>(context, map, clusterManager){

    private var iconGenerator = IconGenerator(context.applicationContext)
    private var clusterIconGenerator = IconGenerator(context.applicationContext)
    private var clusterImageView = ImageView(context.applicationContext)
    private var context = context
    private var dimension = context.resources.getDimensionPixelSize(R.dimen.image_pin_size)

    init{
        iconGenerator.setBackground(ColorDrawable(Color.TRANSPARENT))
        clusterImageView.layoutParams = ViewGroup.LayoutParams(dimension, dimension)
        clusterIconGenerator.setContentView(clusterImageView)
    }

    /*Handles the pre-rendering of single items*/
    override fun onBeforeClusterItemRendered(item: ImagePin?, markerOptions: MarkerOptions?) {

        /*Null safety: ensure item exists before accessing it*/
        if(item?.getImageView() != null) {
            /*Check if the imageview has an image already if not then load image into imageview*/
            if(item.getImageView().drawable == null) {
                /*Explicitly set imageView size to ensure consistency (different size images are all the same size)*/
                item.getImageView().layoutParams = ViewGroup.LayoutParams(dimension, dimension)
                GlideApp.with(context)
                    .load(item.getImagePath())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .circleCrop()
                    .thumbnail(0.3f)
                    .override(dimension, dimension)
                    .into(item.getImageView())

                /*Create an icon using the imageview*/
                iconGenerator.setContentView(item.getImageView())
                val icon = iconGenerator.makeIcon()
                /*Set the marker icon to the one created*/
                markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))
            }else {
                iconGenerator.setContentView(item.getImageView())
                val icon = iconGenerator.makeIcon()
                markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))
            }
        }
    }


    /*Called before the clusters are rendered to the map, we create a blank icon as a placeholder*/
    override fun onBeforeClusterRendered(cluster: Cluster<ImagePin>, markerOptions: MarkerOptions?) {

        cluster.items.toMutableList().get(0).getClusterImageView().layoutParams = ViewGroup.LayoutParams(dimension, dimension)
        GlideApp.with(context)
            .load(cluster.items.toMutableList().get(0).getImagePath())
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .centerCrop()
            .thumbnail(0.5f)
            .override(dimension,dimension)
            .into(cluster.items.toMutableList().get(0).getClusterImageView())

        clusterIconGenerator.setContentView(cluster.items.toMutableList().get(0).getClusterImageView())
        val icon = clusterIconGenerator.makeIcon()
        markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    /* Rendering steps are repeated upon rendered for imageviews that dont contain an image
    * Because Glide is loading so many images often some will fail to load for unknown reasons
    * repeating the steps increases the success chance of rendering
    * REMINDER: Ask for specific feedback about this issue as I am unsure on why exactly this occurs*/
    override fun onClusterRendered(cluster: Cluster<ImagePin>?, marker: Marker?) {
        /*Null safety: Ensure the cluster actually has a non-null item*/
        if(cluster?.items?.toMutableList()?.get(0) != null) {
            /*We take a list of the items in the cluster and always access the first item to ensure the
            * image displayed by the cluster remains the same on each map update (zoom in/out)
            * If the item does not have an image in its clusterview then load one*/
            if(cluster.items.toMutableList().get(0).getClusterImageView().drawable == null) {
                cluster.items.toMutableList().get(0).getClusterImageView().layoutParams = ViewGroup.LayoutParams(dimension, dimension)
                GlideApp.with(context)
                    .load(cluster.items?.toMutableList()?.get(0)?.getImagePath())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .thumbnail(0.5f)
                    .override(dimension, dimension)
                    .into(cluster.items.toMutableList().get(0).getClusterImageView())

                clusterIconGenerator.setContentView(cluster.items.toMutableList().get(0).getClusterImageView())
                val icon = clusterIconGenerator.makeIcon()
                marker?.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
            }else{
                clusterIconGenerator.setContentView(cluster.items.toMutableList().get(0).getClusterImageView())
                val icon = clusterIconGenerator.makeIcon()
                marker?.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
            }
        }
    }

    override fun onClusterItemRendered(item: ImagePin?, marker: Marker?) {
        if(item != null){
            if(item.getImageView().drawable == null){
                /*Explicitly set imageView size to ensure consistency (different size images are all the same size)*/
                item.getImageView().layoutParams = ViewGroup.LayoutParams(dimension, dimension)
                GlideApp.with(context)
                    .load(item.getImagePath())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .circleCrop()
                    .thumbnail(0.3f)
                    .override(dimension, dimension)
                    .into(item.getImageView())
                /*Create an icon using the imageview*/
                iconGenerator.setContentView(item.getImageView())
                val icon = iconGenerator.makeIcon()
                marker?.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
            }else{
                iconGenerator.setContentView(item.getImageView())
                val icon = iconGenerator.makeIcon()
                marker?.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
            }
        }
    }

    /*Determines when items should be rendered as a cluster or a single item*/
    override fun shouldRenderAsCluster(cluster: Cluster<ImagePin>?): Boolean {
        if(cluster != null) {
            return cluster.size > 3
        }
        return false
    }

}