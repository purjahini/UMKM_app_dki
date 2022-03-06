@file:JvmName("MyUtil")
@file:JvmMultifileClass

package com.svtech.dhuwit.Utils


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pattra.pattracardsdk.Service.SyncService
import com.svtech.dhuwit.Activities.LoginActivity
import com.svtech.dhuwit.Activities.SplashScreenActivity
import kotlinx.android.synthetic.main.layout_toolbar_with_back.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.NumberFormat
import java.util.*


fun ImageViewToByteArray(imageView: ImageView): ByteArray {
    val bitmap = imageView.drawToBitmap()
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val bitmapdata = stream.toByteArray()
    return bitmapdata
}


fun numberToCurrency(number: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(number).toString()
        .let { it.substring(0, it.length - 3) }
}

/*Fungsi untuk mengconvert ke format uang*/
fun numberToCurrency(number: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(number).toString()
        .removeSuffix(",00").replace("Rp", "Rp. ")
}

fun numberToCurrency(number: Int): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(number).toString()
        .removeSuffix(",00").replace("Rp", "Rp. ")
}

fun calculateNoOfColumns(
    context: Context,
    columnWidthDp: Float
): Int { // For example columnWidthdp=180
    val displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / columnWidthDp + 0.5).toInt()
}

/*Fungsi untuk memilih gambar*/
fun pickImage(activity: Activity, imageView: ImageView , title: String):String {

    val folder = File(Environment.getExternalStorageDirectory(), "UmkmImage")
    if (!folder.exists()) folder.mkdir()
    var file = File(folder.absolutePath, title)
  var nameImage = ""
    Dexter.withContext(activity)
        .withPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                if (p0?.areAllPermissionsGranted()!!) {
                    ImagePicker.with(activity)
                        .galleryOnly()
                        .cropSquare()
                        .compress(1024)
                        .saveDir(file)
                        .maxResultSize(1080, 1080)
                        .start { resultCode, data ->


                            if (resultCode == Activity.RESULT_OK) {
                                Glide.with(activity).load(data?.data)
                                    .apply(RequestOptions.bitmapTransform(RoundedCorners(10F.toInt())))
                                    .into(imageView)
                            nameImage = File(data?.data?.path).name
                                See.log("nameImage : ${nameImage}")
                            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                                Toast.makeText(
                                    activity,
                                    ImagePicker.getError(data),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }

        }).check()
return nameImage
}

/*Fungsi untuk mengatur tombol kembali dan title di toolbar*/
fun setToolbar(
    activity: Activity,
    title: String
) {
    activity.btnBack.setOnClickListener {
        activity.onBackPressed()
    }
    activity.tvTitle.setText(title)
}

/*Fungsi untuk mengambil screen shoot*/
fun viewToImage(view: View): Bitmap? {
    val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(returnedBitmap)
    val bgDrawable = view.background
    if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
    view.draw(canvas)
    return returnedBitmap
}

fun Activity.hideSoftKeyboard() {
    currentFocus?.let {
        val inputMethodManager =
            ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

    }
    if (currentFocus is TextInputEditText) {
        (currentFocus as TextInputEditText).clearFocus()
    }
}

fun checkInput(input: Any): Boolean {
    var check = false
    if (input is TextInputLayout) {
        if (input.editText?.text.toString().trim().isEmpty()) {
            check = false
            input.isErrorEnabled = true
            input.error = "tidak boleh kosong!"
//            Snackbar.make(input.rootView,"${input.hint} tidak boleh kosong!",Snackbar.LENGTH_SHORT).show()
        } else {
            check = true
            input.isErrorEnabled = false
        }
    }
    return check
}

fun checkInputUsername(input: Any): Boolean {
    var check = false
    if (input is TextInputLayout) {

        if (input.editText?.text.toString().trim().isEmpty()) {
            check = false
            input.error = "Username tidak boleh kosong!"
        } else if (input.editText?.text.toString().length < 5) {
            check = false
            input.error = "Username minimal 5 karakter!"
        } else {
            check = true
        }
        input.isErrorEnabled = !check
    }
    return check
}

fun checkInputPassword(input: Any): Boolean {
    var check = false
    if (input is TextInputLayout) {
        if (input.editText?.text.toString().trim().isEmpty()) {
            check = false
            input.error = "Password tidak boleh kosong!"
        } else if (input.editText?.text.toString().length < 5) {
            check = false
            input.error = "Password minimal 5 karakter!"
        } else {
            check = true
        }
        input.isErrorEnabled = !check
    }
    return check
}

fun savePreferences(context: Context, namePreferences: String, value: Any): Boolean {
    val preferences = context.getSharedPreferences("prefs", 0)
    val editor = preferences.edit()
    when (value) {
        is Boolean -> {
            editor.putBoolean(namePreferences, value)
            editor.apply()
            return true
        }
        is String -> {
            editor.putString(namePreferences, value)
            editor.apply()
            return true
        }
        is Int -> {
            editor.putInt(namePreferences, value)
            editor.apply()
            return true
        }
        is Long -> {
            editor.putLong(namePreferences, value)
            editor.apply()
            return true
        }
        is Float -> {
            editor.putFloat(namePreferences, value)
            editor.apply()
            return true
        }
        else -> {
            return false
        }
    }
}
fun deletePreferences(context: Context, value: String):Boolean {
    val preferences = context.getSharedPreferences("prefs", 0)
    val editor = preferences.edit()
    editor.clear()
    editor.remove(value)
    editor.apply()

    return true

}

fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("prefs", 0)

}








