package com.doublesymmetry.trackplayer.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.RatingCompat
import com.doublesymmetry.trackplayer.R
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper.Companion.instance
import androidx.media3.common.Rating
import androidx.media3.common.HeartRating
import androidx.media3.common.ThumbRating
import androidx.media3.common.StarRating
import androidx.media3.common.PercentageRating

/**
 * @author Milen Pivchev @mpivchev
 */
object BundleUtils {
    fun getUri(context: Context, data: Bundle?, key: String?): Uri? {
        if (!data!!.containsKey(key)) return null
        val obj = data[key]
        if (obj is String) {
            // Remote or Local Uri
            if (obj.trim { it <= ' ' }.isEmpty()) throw RuntimeException("$key: The URL cannot be empty")
            return Uri.parse(obj as String?)
        } else if (obj is Bundle) {
            // require/import
            val uri = obj.getString("uri")
            val helper = ResourceDrawableIdHelper.getInstance()
            val id = helper.getResourceDrawableId(context, uri)
            return if (id > 0) {
                // In production, we can obtain the resource uri
                val res = context.resources
                Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(res.getResourcePackageName(id))
                    .appendPath(res.getResourceTypeName(id))
                    .appendPath(res.getResourceEntryName(id))
                    .build()
            } else {
                // During development, the resources might come directly from the metro server
                Uri.parse(uri)
            }
        }
        return null
    }

    fun getRawResourceId(context: Context, data: Bundle, key: String?): Int {
        if (!data.containsKey(key)) return 0
        val obj = data[key] as? Bundle ?: return 0
        var name = obj.getString("uri")
        if (name.isNullOrEmpty()) return 0
        name = name.lowercase().replace("-", "_")
        return try {
            name.toInt()
        } catch (ex: NumberFormatException) {
            context.resources.getIdentifier(name, "raw", context.packageName)
        }
    }

    private fun getIcon(context: Context, options: Bundle, propertyName: String, defaultIcon: Int): Int {
        if (!options.containsKey(propertyName)) return defaultIcon

        val bundle = options.getBundle(propertyName) ?: return defaultIcon

        val helper = ResourceDrawableIdHelper.getInstance()
        val icon = helper.getResourceDrawableId(context, bundle.getString("uri"))
        return if (icon == 0) defaultIcon else icon
    }

    fun getCustomIcon(context: Context, options: Bundle, propertyName: String, defaultIcon: Int): Int {
        when (getIntOrNull(options, propertyName)) {
            0 -> return R.drawable.hearte_24px
            1 -> return R.drawable.heart_24px
            2 -> return R.drawable.baseline_repeat_24
            3 -> return R.drawable.baseline_repeat_one_24
            4 -> return R.drawable.shuffle_24px
            5 -> return R.drawable.ifl_24px
        }
        return getIcon(context, options, propertyName, defaultIcon)
    }

    fun getIconOrNull(context: Context, options: Bundle, propertyName: String): Int? {
        if (!options.containsKey(propertyName)) return null

        val bundle = options.getBundle(propertyName) ?: return null

        val helper = instance
        val icon = helper.getResourceDrawableId(context, bundle.getString("uri"))
        return if (icon == 0) null else icon
    }

    fun getRating(data: Bundle, key: String?, ratingType: Int): Rating? {
        return when (ratingType) {
            RatingCompat.RATING_HEART -> HeartRating(data.getBoolean(key, true))
            RatingCompat.RATING_THUMB_UP_DOWN -> ThumbRating(data.getBoolean(key, true))
            RatingCompat.RATING_PERCENTAGE -> PercentageRating(data.getFloat(key, 0f))
            RatingCompat.RATING_3_STARS, RatingCompat.RATING_4_STARS, RatingCompat.RATING_5_STARS -> StarRating(ratingType, data.getFloat(key, 0f))
            else -> null
        }
    }

    fun setRating(data: Bundle, key: String?, rating: Rating) {
        if (!rating.isRated) return
        when (rating) {
            is HeartRating -> data.putBoolean(key, rating.isHeart)
            is ThumbRating -> data.putBoolean(key, rating.isThumbsUp)
            is PercentageRating -> data.putDouble(key, rating.percent.toDouble())
            is StarRating -> data.putDouble(key, rating.starRating.toDouble())
        }
    }

    fun getInt(data: Bundle?, key: String?, defaultValue: Int): Int {
        val value = data!![key]
        return if (value is Number) {
            value.toInt()
        } else defaultValue
    }

    fun getIntOrNull(data: Bundle?, key: String?): Int? {
        val value = data!![key]
        return if (value is Number) {
            value.toInt()
        } else null
    }

    fun getDoubleOrNull(data: Bundle?, key: String?): Double? {
        val value = data!![key]
        return if (value is Number) {
            value.toDouble()
        } else null
    }
}