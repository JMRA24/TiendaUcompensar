package com.project.store.utils

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.loadProductImage(imageUrl: String?) {
    when {
        imageUrl.isNullOrEmpty() -> {
            setImageResource(android.R.drawable.ic_menu_gallery)
        }
        imageUrl.startsWith("data:image") -> {
            val base64 = imageUrl.substringAfter("base64,")
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            setImageBitmap(bitmap)
        }
        else -> {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(this)
        }
    }
}
