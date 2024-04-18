package com.svtech.mandiri.Utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.Spinner

import com.svtech.mandiri.modelOnline.ItemOption



private lateinit var alertDialog: AlertDialog
class See {


    companion object {

        fun getIndex(list: ArrayList<ItemOption>, spinner: Spinner, searchId: String?): Int {
            for (i in 0 until spinner.count) {
                if (list[i].optId.equals(searchId)) {
                    return i
                }

            }
            return 0
        }

        fun log(message: String) {

            try {
                Log.d(Cons.TAG, message)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun showPopUp(context: Context, title: String, message: String, targetActivity: Class<*> ) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle(title)
            alertDialogBuilder.setMessage(message)

            // Tombol OK
            alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                // Intent untuk memulai aktivitas baru
                val intent = Intent(context, targetActivity)
                context.startActivity(intent)
                dialog.dismiss()
            }

            // Tombol Cancel
            alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        fun dismissPopUpOnOutsideTouch() {
            if (::alertDialog.isInitialized && alertDialog.isShowing) {
                alertDialog.setOnCancelListener(DialogInterface.OnCancelListener { })
            }
        }

        fun log(key: String, message: String) {
            try {
                Log.d(Cons.TAG, key + " -> " + message)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }

        fun logE(message: String) {
            try {
                Log.e(Cons.TAG, message)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }


        fun logE(key: String, message: String) {
            try {
                Log.e(Cons.TAG, key + " -> " + message)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }


        fun toast(context: Context, message: String) {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
        fun EditText.itText(): String {
            return this.text.toString().trim()
        }

        @JvmStatic
        fun Companion(s: String) {

        }
    }


}