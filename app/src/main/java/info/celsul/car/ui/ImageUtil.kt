package info.celsul.car.ui

import android.widget.ImageView
import com.squareup.picasso.Picasso
import info.celsul.car.R
import java.io.File

fun ImageView.loadUrl(imageUrl: String) {
    Picasso.get()
        .load(imageUrl)
        .placeholder(R.drawable.ic_download)
        .error(R.drawable.ic_error)
        .transform(CircleTransform())
        .into(this)
}

fun ImageView.setImageFromPath(file: File){
    Picasso.get().load(file).placeholder(R.drawable.ic_download).error(R.drawable.ic_error).transform(CircleTransform()).into(this)
}