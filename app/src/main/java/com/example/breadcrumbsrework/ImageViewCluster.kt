package com.example.breadcrumbsrework

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.maps.android.clustering.Cluster
import java.io.File


/*This fragment is responsible for loading a list of images into a viewpager for the user to "swipe" through*/
class ImageViewCluster : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var exitButton : ImageButton
    private lateinit var shareButton : ImageButton
    private lateinit var pageNum : TextView
    private lateinit var imgDate: TextView
    private var imgDateList: ArrayList<String>? = null
    private  var imgPathList : ArrayList<String>? = null
    private lateinit var viewPager : ViewPager
    private lateinit var viewPagerAdapter : MyViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imgPathList = arguments?.getStringArrayList("imgPathList")
        imgDateList = arguments?.getStringArrayList("imgDateList")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.image_view_cluster, container, false)
        /*Set the viewpager and link it with the custom adapter*/
        viewPager = view.findViewById(R.id.image_viewpager)
        viewPagerAdapter = MyViewPagerAdapter(fragmentManager, imgPathList)
        viewPager.adapter = viewPagerAdapter
        /*Set the text view*/
        pageNum = view.findViewById(R.id.pagerNum)
        imgDate = view.findViewById(R.id.date_text_view)

        /*Initial values to be displayed as the page number*/
        val totalPages = viewPager.adapter?.count
        var currentPagerIndex = 0

        /*If there is only a single image then dont display the viewPager location to the user*/
        if(totalPages == 1){
            pageNum.text = ""
        }else {
            pageNum.text = "1 / $totalPages"
        }
        imgDate.text = imgDateList?.get(0)

        /*Update the text that tells the user what image they are currently viewing every time the page is changed*/
        viewPager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                currentPagerIndex = position
                val currentPage = (position+1).toString()
                val text = "$currentPage / $totalPages"
                pageNum.text = text
                imgDate.text = imgDateList?.get(position)
            }
        })

        /*If back arrow is pressed them just got back to previous fragment*/
        exitButton = view.findViewById(R.id.close_dialog)
        exitButton.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        /*When the share button is pressed create a share intent for an image
        * This will allow the image to be shared with any app the user has installed that accepts image intents*/
        shareButton = view.findViewById(R.id.share_intent)
        shareButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_SEND)
            val iPath = imgPathList?.get(currentPagerIndex)
            if(iPath != null) {
                println(iPath)
                val imgFile = File(iPath)
                val imageUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", imgFile)

                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_STREAM, imageUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context?.startActivity(Intent.createChooser(intent, "Send the image to another app."))
            }
        }

        /*Set the title of the app to let the user know where they are currently within the application */
        activity?.setTitle(R.string.title_fragment_viewpager)

        return view
    }


    override fun onDetach() {
        super.onDetach()
        activity?.setTitle(R.string.title_activity_maps)
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onExitButtonClicked()
    }

}
