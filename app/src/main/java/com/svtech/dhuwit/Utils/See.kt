package com.svtech.dhuwit.Utils

import android.content.Context
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.Spinner

import com.svtech.dhuwit.modelOnline.ItemOption




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