package com.example.breadcrumbsrework


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView


/*This fragment is responsible for displaying an image which will be used in a viewpager when the user views a cluster of images*/
class ImageForViewpager : Fragment() {

    private var imgPath : String? = null
    private lateinit var imgView : ImageView

    /*Get the path to the image that is being displayed as an argument*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imgPath = arguments?.getString("imgPath")
    }
    /*Use glide to load the image from the imgPath into the imageview*/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.image_for_viewpager, container, false)

        imgView = view.findViewById(R.id.viewpagerImage)

        GlideApp.with(view).load(imgPath).into(imgView)

        return view
    }


    fun getImagePath(): String?{
        return imgPath
    }




}
