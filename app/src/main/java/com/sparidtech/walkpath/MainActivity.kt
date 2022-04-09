package com.sparidtech.walkpath

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mImageView = findViewById<ImageView>(R.id.image_view_1)
        val mButton = findViewById<Button>(R.id.button_1)
        val buttonZoomIn: Button = findViewById(R.id.zoomInButton)
        val buttonZoomOut: Button = findViewById(R.id.zoomOutButton)

        buttonZoomIn.setOnClickListener() {
            val animZoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            mImageView.startAnimation(animZoomIn)
        }

        buttonZoomOut.setOnClickListener() {
            val animZoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            mImageView.startAnimation(animZoomOut)
        }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val coords = mutableListOf(arrayOf(0F, 0F), arrayOf(20F, 50F), arrayOf(40F, 120F), arrayOf(60F, 140F), arrayOf(80F, 200F), arrayOf(100F, 160F))

        val mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(mBitmap)
        mCanvas.drawColor(Color.GRAY)
        mImageView.setImageBitmap(mBitmap)

        mButton.setOnClickListener {
            val mPaint = Paint()
            mPaint.color = Color.RED
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 5F
            mPaint.isAntiAlias = true

            for (iter in 0 until coords.lastIndex) {
                val mStartX = coords[iter][0]
                val mStartY = coords[iter][1]
                val mStopX = coords[iter+1][0]
                val mStopY = coords[iter+1][1]
                mCanvas.drawLine(mStartX, mStartY, mStopX, mStopY, mPaint)
            }

            mImageView.setImageBitmap(mBitmap)
        }
    }
}