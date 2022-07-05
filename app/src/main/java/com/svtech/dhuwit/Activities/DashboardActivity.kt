package com.svtech.dhuwit.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.AdapterSlider
import com.svtech.dhuwit.Adapter.RclvItemMenu
import com.svtech.dhuwit.Models.Menu
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.util.*

class DashboardActivity : AppCompatActivity() {
    //...

    lateinit var vpSlider: ViewPager
    lateinit var timer: Timer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        vpSlider = findViewById(R.id.vp_slider)

        val arrSlider = ArrayList<Int>()
        arrSlider.add(R.drawable.slider1)
        arrSlider.add(R.drawable.slider2)
        arrSlider.add(R.drawable.slider3)

        val adapterSlider = AdapterSlider(arrSlider, this)
        vpSlider.adapter = adapterSlider


        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                vpSlider.post(Runnable { vpSlider.setCurrentItem((vpSlider.getCurrentItem() + 1) % arrSlider.size) })
            }
        }
        timer = Timer()
        timer.schedule(timerTask, 3000, 3000)




        /*Setting menu*/
        val username = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER,"")
        val user = SugarRecord.find(User::class.java,"USERNAME = ?",username).firstOrNull()
        if(user != null){
            initMenu(user)
        }

        /*Setting data profile*/
        initProfile()

        btnEditProfile.setOnClickListener{
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

    }

    private fun initMenu(user: User) {
        var listMenu : MutableList<Menu>
        var colSpan : Int
        if (user.role.equals(User.userSysAdmin)){
            colSpan = 4
            listMenu = mutableListOf(
                Menu(R.drawable.icon1,"Penjualan"),
                Menu(R.drawable.icon2,"Keranjang"),
                Menu(R.drawable.icon3,"Kategori"),
                Menu(R.drawable.icon4,"Produk"),
                Menu(R.drawable.icon5,"Pegawai"),
                Menu(R.drawable.ic_chart, "Neraca"),
                Menu(R.drawable.ic_account,"TOPUP"),
                Menu(R.drawable.icon6,"Laporan")
            )
            imgEditProfile.setImageDrawable(getDrawable(R.drawable.ic_user))
        }else{
            colSpan = 4
            listMenu = mutableListOf(
                Menu(R.drawable.icon1,"Penjualan"),
                Menu(R.drawable.icon2,"Keranjang"),
                Menu(R.drawable.icon3,"Kategori"),
                Menu(R.drawable.icon4,"Produk")
            )
            imgEditProfile.setImageDrawable(getDrawable(R.drawable.ic_user))
        }

        rclvPenjualan.apply {
            adapter = RclvItemMenu(this@DashboardActivity,listMenu)
            layoutManager = GridLayoutManager(this@DashboardActivity, colSpan)
            setHasFixedSize(true)
        }
    }


    fun initProfile(){
        val profile  = SugarRecord.listAll(Profile::class.java).firstOrNull()
        if(profile != null){
            if(profile.logoToko != null){
                Glide.with(this).load(profile.logoToko).fitCenter().into(imgLogo)
            }
            tvNamaCafe.text = profile.namaToko
            tvAlamatCafe.text = profile.alamatToko
        }
    }

    override fun onResume() {
        super.onResume()
        initProfile()
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()

    }


}