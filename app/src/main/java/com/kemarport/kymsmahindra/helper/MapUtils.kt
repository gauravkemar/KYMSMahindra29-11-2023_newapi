package com.kemarport.kymsmahindra.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kemarport.kymsmahindra.R


class MapUtils(private val context: Context) {

    fun addRotatedMarker(googleMap: GoogleMap, position: LatLng, bearing: Float) {
        val rotatedBitmapDrawable = rotateDrawable(bearing)
        val markerOptions = MarkerOptions()
            .position(position)
            .icon(BitmapDescriptorFactory.fromBitmap(rotatedBitmapDrawable.bitmap))
        googleMap.addMarker(markerOptions)
    }

    private fun rotateDrawable(angle: Float): BitmapDrawable {
        val arrowBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.navigation_arrow)
        val canvasBitmap = arrowBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvasBitmap.eraseColor(0x00000000)
        val canvas = Canvas(canvasBitmap)
        val rotateMatrix = Matrix()
        rotateMatrix.setRotate(angle, canvas.width.toFloat() / 2, canvas.height.toFloat() / 2)
        canvas.drawBitmap(arrowBitmap, rotateMatrix, null)
        return BitmapDrawable(context.resources, canvasBitmap)
    }
}