package com.sparidtech.walkpath

import android.Manifest
import android.R.string
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.lang.Error


class MainActivity : AppCompatActivity(),SensorEventListener {

    private var sensorManager :SensorManager?=null
    private var stepSensor:Sensor?=null
    private var angleSensor:Sensor?=null


    private var running = false
    private var total_steps = 0F
    private var prev_steps = 0F
    private var current_angle = 0F
    private var coords = mutableListOf(arrayOf(0F, 0F), arrayOf(20F, 50F), arrayOf(40F, 120F), arrayOf(60F, 140F), arrayOf(80F, 200F), arrayOf(100F, 160F))
    private var mCanvas:Canvas?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


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

        val mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        mCanvas!!.drawColor(Color.GRAY)
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
                mCanvas!!.drawLine(mStartX, mStartY, mStopX, mStopY, mPaint)
            }

            mImageView.setImageBitmap(mBitmap)
        }
    }

    override fun onResume() {
        //Log.i("Sensor","is on sensor changed function")
//        Toast.makeText(this,"No sensor detected on this device",Toast.LENGTH_SHORT).show()
        super.onResume()
        running = true
        stepSensor=sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        angleSensor=sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        if(stepSensor==null){
            Toast.makeText(this,"No sensor detected on this device",Toast.LENGTH_SHORT).show()
            Log.i("Sensor","Not Supported")
        }else{
            sensorManager?.registerListener(this,stepSensor,SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
            sensorManager?.registerListener(this,angleSensor,SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    override fun onSensorChanged(event: SensorEvent?) {

            if(running){
                if(event!!.sensor==stepSensor){
                    if(prev_steps==0F){
                        prev_steps=event!!.values[0]
                    }else{
                        total_steps=event!!.values[0]-prev_steps

                        val index = coords.lastIndex
                        Log.i("Sensor","Rotation Vector X"+Math.cos(current_angle.toDouble()).toString())
                        Log.i("Sensor","Rotation Vector Y"+Math.sin(current_angle.toDouble()).toString())
                        val xx = coords[index][0]+(6.5 * total_steps * Math.cos(current_angle.toDouble())).toFloat()
                        val yy = coords[index][1]+(6.5 * total_steps * Math.sin(current_angle.toDouble())).toFloat()

                        val mPaint = Paint()
                        mPaint.color = Color.RED
                        mPaint.style = Paint.Style.STROKE
                        mPaint.strokeWidth = 5F
                        mPaint.isAntiAlias = true

                        Log.i("Sensor","Rotation Vector X"+xx.toString())
                        Log.i("Sensor","Rotation Vector Y"+yy.toString())

                        mCanvas!!.drawLine(coords[index][0], coords[index][1], xx, yy, mPaint)
                        coords.add(arrayOf(xx, yy))

//                        Log.i("Sensor","Rotation Vector X"+xx.toString())
//                        Log.i("Sensor","Rotation Vector Y"+yy.toString())

                        prev_steps=total_steps
                    }
//                    Log.i("Sensor","Pedometer "+total_steps.toString())
                }else if (event.sensor==angleSensor){
//                    current_angle = event!!.values[0] * Math.PI.toFloat() / 180

                    current_angle = event!!.values[0]
//                   Log.i("Sensor","RV "+current_angle.toString())
                }
            }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}