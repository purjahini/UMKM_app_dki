package com.svtech.mandiri.Utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset

/**
 * Created by adriyoutomo on 23/07/2019.
 */
object FileUtils {

    @Throws(IOException::class)
    fun loadJSONFromAsset(context: Context, jsonFileName: String): String {
        (context.assets).open(jsonFileName).let {
            val buffer = ByteArray(it.available())
            it.read(buffer)
            it.close()
            return String(buffer, Charset.forName("UTF-8"))
        }
    }

    fun loadImageFromURL(url: String?): Drawable? {
        try {
            val stream = URL(url).content as InputStream
            return Drawable.createFromStream(stream, "")
        } catch (e: Exception) {
            return null
        }
    }

    fun getScaledBitmap(b: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap {
        val m = Matrix()
        m.setRectToRect(
            RectF(0f, 0f, b.width.toFloat(), b.height.toFloat()),
            RectF(0f, 0f, reqWidth.toFloat(), reqHeight.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
    }

    fun File.grantedUri(context: Context): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.applicationContext.packageName}.provider",
                this
            )
        } else {
            Uri.fromFile(this)
        }
    }

    /**
     * Finds real path of the file which is addressed by the uri.
     *
     * @param context a context
     * @param uri content uri to find real path
     *
     * @return real path of the file addressing by the uri.
     */
    fun pathFromUri(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
            DocumentsContract.isDocumentUri(context, uri)
        ) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                getFileName(context, uri)?.let { fileName ->
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/Download/" + fileName
                }

                var documentId = DocumentsContract.getDocumentId(uri)
                if (documentId.startsWith("raw:")) {
                    documentId = documentId.replace("raw:", "")
                    if (File(documentId).exists()) {
                        return documentId
                    }
                }

                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    documentId.toLong()
                )
                return getDataColumn(
                    context,
                    contentUri,
                    null,
                    null
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)
        return null
    }

    // returns whether the Uri authority is ExternalStorageProvider.
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    // returns whether the Uri authority is DownloadsProvider.
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    // returns whether the Uri authority is MediaProvider.
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    // returns whether the Uri authority is Google Photos.
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getFileName(
        context: Context,
        uri: Uri
    ): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getPath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }
}