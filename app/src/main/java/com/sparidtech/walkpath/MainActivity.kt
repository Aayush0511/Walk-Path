package com.sparidtech.walkpath

import android.content.Context
import android.graphics.*
import android.hardware.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.opencsv.CSVWriter
import java.io.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : AppCompatActivity(),SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var angleSensor: Sensor? = null


    private var running = false
    private var current_angle = 0F
    private var coords = mutableListOf(
        arrayOf(0F, 0F),
    )
    private var mCanvas: Canvas? = null
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
        coords.add(arrayOf(screenWidth/2F,screenHeight/2F))

        val mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        mCanvas!!.drawColor(Color.GRAY)
        mImageView.setImageBitmap(mBitmap)

        mButton.setOnClickListener {
            val clearPaint = Paint()
            clearPaint.color = Color.GRAY
            mCanvas!!.drawRect(0F, 0F, screenWidth*1F, screenHeight*1F, clearPaint)

            coords.clear()
            coords.add(arrayOf(screenWidth/2F,screenHeight/2F))
            enableSensors()

//            val mPaint = Paint()
//            mPaint.color = Color.RED
//            mPaint.style = Paint.Style.STROKE
//            mPaint.strokeWidth = 5F
//            mPaint.isAntiAlias = true
//
//            for (iter in 0 until coords.lastIndex) {
//                val mStartX = coords[iter][0]
//                val mStartY = coords[iter][1]
//                val mStopX = coords[iter + 1][0]
//                val mStopY = coords[iter + 1][1]
//                mCanvas!!.drawLine(mStartX, mStartY, mStopX, mStopY, mPaint)
//            }
//
//            mImageView.setImageBitmap(mBitmap)
        }
    }

    fun enableSensors(){
        running = true
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        angleSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
            Log.i("Sensor", "Not Supported")
        } else {
            sensorManager?.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            sensorManager?.registerListener(this, angleSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onResume() {
        super.onResume()
        running = true
        enableSensors()
    }

    override fun onPause() {
        super.onPause()
        writeDatatoCSV()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            if (event!!.sensor == stepSensor) {
                val index = coords.lastIndex
//
                val xx = coords[index][0] + (25 * cos(current_angle))
                val yy = coords[index][1] + (25 * sin(current_angle))

                val mPaint = Paint()
                mPaint.color = Color.RED
                mPaint.style = Paint.Style.STROKE
                mPaint.strokeWidth = 5F
                mPaint.isAntiAlias = true

                Log.i("Sensor", "X" + xx.toString() + " Y "+ yy.toString())

                mCanvas!!.drawLine(coords[index][0], coords[index][1], xx, yy, mPaint)
                coords.add(arrayOf(xx, yy))
            }else{
                current_angle = (event!!.values[0] * PI / 180).toFloat()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun writeDatatoCSV(){
        var writer: CSVWriter? = null
        try {
            writer = CSVWriter(FileWriter("/sdcard/Path.csv"))
            for (iter in 0 until coords.lastIndex) {
              val tempArr = arrayOf(""+coords[iter][0]+"",""+coords[iter][1]+"")
              writer.writeNext(tempArr)
            }
//            writer!!.writeNext(entries)
            writer!!.close()
        } catch (e: IOException) {
            //error
        }
    }
}