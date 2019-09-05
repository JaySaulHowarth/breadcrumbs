package com.example.breadcrumbsrework

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/*Adapter for the viewpager, creates the fragment that displays the image and returns in to the viewpager*/
class MyViewPagerAdapter(fm: FragmentManager?, private val imgPathList: ArrayList<String>?) : FragmentStatePagerAdapter(fm){

    override fun getItem(itemNum: Int): Fragment {
        val imgPath = imgPathList?.get(itemNum)
        var imgFrag = ImageForViewpager()
        var args = Bundle()
        args.putString("imgPath", imgPath)
        imgFrag.arguments = args
        return imgFrag
    }

    override fun getCount(): Int {
        val count = imgPathList?.size
        if(count != null){
            return count
        }else{
            return 0
        }
    }

}