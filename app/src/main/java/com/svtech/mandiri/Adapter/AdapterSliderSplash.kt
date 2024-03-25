package com.svtech.mandiri.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.svtech.mandiri.R
import com.svtech.mandiri.modelOnline.SliderItem

class AdapterSliderSplash(private val sliderItems: List<SliderItem>, private val context: Context) :
    PagerAdapter() {

    override fun getCount(): Int {
        return sliderItems.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_slider_splash, container, false)

        // Find your ImageView and TextView in your custom slider item layout
        val imageView: ImageView = view.findViewById(R.id.slider_image)
        val textView: TextView = view.findViewById(R.id.slider_text)

        // Set image and text
        imageView.setImageResource(sliderItems[position].imageResId)
        textView.text = sliderItems[position].text

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}