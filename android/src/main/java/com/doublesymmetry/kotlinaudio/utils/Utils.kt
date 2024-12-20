package com.doublesymmetry.kotlinaudio.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

fun isUriLocalFile(uri: Uri?): Boolean {
    if (uri == null) return false
    val scheme = uri.scheme
    val host = uri.host
    if ((scheme == "http" || scheme == "https") &&
                    (host == "localhost" || host == "127.0.0.1" || host == "[::1]")
    ) {
        return false
    }
    return scheme == null ||
            scheme == ContentResolver.SCHEME_FILE ||
            scheme == ContentResolver.SCHEME_ANDROID_RESOURCE ||
            scheme == ContentResolver.SCHEME_CONTENT ||
            scheme == "res" ||
            host == null
}
