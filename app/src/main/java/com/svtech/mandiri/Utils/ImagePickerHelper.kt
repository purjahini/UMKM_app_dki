package com.svtech.mandiri.Utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ImagePickerHelper(private val activity: Activity, private val callback: ImagePickerCallback) {
    private var imageUri: Uri? = null

    private val galleryLauncher: ActivityResultLauncher<Intent> =
        (activity as? AppCompatActivity)?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val uri = data?.data
                uri?.let { callback.onImagePicked(it) }
            }
        } ?: throw IllegalStateException("Activity must be an instance of AppCompatActivity")

    private val cameraLauncher: ActivityResultLauncher<Uri> =
        (activity as? AppCompatActivity)?.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUri?.let { callback.onImagePicked(it) }
            }
        } ?: throw IllegalStateException("Activity must be an instance of AppCompatActivity")

    fun showImagePickerDialog() {
        val options = arrayOf("Pilih dari Galeri", "Ambil Foto")
        AlertDialog.Builder(activity)
            .setTitle("Pilih Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (checkPermission(permission)) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        } else {
            requestPermission(permission, REQUEST_GALLERY_PERMISSION)
        }
    }

    private fun takePhotoFromCamera() {
        if (checkPermission(Manifest.permission.CAMERA)) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "New Picture")
                put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            }
            imageUri = activity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            cameraLauncher.launch(imageUri!!)
        } else {
            requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showPermissionExplanationDialog(permission, requestCode)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        }
    }

    private fun showPermissionExplanationDialog(permission: String, requestCode: Int) {
        AlertDialog.Builder(activity)
            .setTitle("Izin Diperlukan")
            .setMessage("Aplikasi memerlukan izin ini untuk berfungsi dengan baik. Silakan izinkan akses.")
            .setPositiveButton("Izinkan") { _, _ ->
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    companion object {
        private const val REQUEST_GALLERY_PERMISSION = 101
        private const val REQUEST_CAMERA_PERMISSION = 102
    }
}

interface ImagePickerCallback {
    fun onImagePicked(imageUri: Uri)
}
