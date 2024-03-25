import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import java.util.ArrayList

class AdapterSlider(var data: ArrayList<String>, var context: Activity?) : PagerAdapter() {
    lateinit var layoutInflater: LayoutInflater

    override fun getCount(): Int {
        return data.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    @NonNull
    override fun instantiateItem(@NonNull container: ViewGroup, position: Int): Any {
        layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.item_slider, container, false)

        //Init
        val imageView: ImageView
        imageView = view.findViewById(R.id.image)

        // Load image using Glide or Picasso from URL
        Glide.with(context!!)
            .load(MyConstant.url_image+data[position])
            .into(imageView)

        container.addView(view, 0)

        return view
    }

    override fun destroyItem(@NonNull container: ViewGroup, position: Int, @NonNull `object`: Any) {
        container.removeView(`object` as View)
    }
}
