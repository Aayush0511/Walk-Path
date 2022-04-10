package com.sparidtech.walkpath

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.*
import android.net.wifi.aware.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : AppCompatActivity(),SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var angleSensor: Sensor? = null

    val serviceName="walk-path"

    private var running = false
    private var current_angle = 0F
    private var coords = mutableListOf(
        arrayOf(0F, 0F),
    )
    private var mCanvas: Canvas? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private var handler = Handler()

    @RequiresApi(Build.VERSION_CODES.O)
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
//        var isSubscribed = false

//        var wifiAwareSession:WifiAwareSession
        val attachCallback = object:AttachCallback(){
            override fun onAttachFailed() {
                super.onAttachFailed()
                Toast.makeText(this@MainActivity,"Failed to Attach",Toast.LENGTH_SHORT).show()
            }

            override fun onAttached(session: WifiAwareSession?) {
                super.onAttached(session)
                Toast.makeText(this@MainActivity,"Succesfull to Attach",Toast.LENGTH_SHORT).show()
//                if (session != null) {
//                    wifiAwareSession=session
//                }

                //first discover a session
                val configSub: SubscribeConfig = SubscribeConfig.Builder()
                    .setServiceName(serviceName)
                    .build()
                session!!.subscribe(configSub, object : DiscoverySessionCallback() {
                    var sessionDiscovered: SubscribeDiscoverySession? =null
                    override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                        sessionDiscovered=session
                        Toast.makeText(this@MainActivity,"On Subscribed",Toast.LENGTH_SHORT).show()
                    }

                    override fun onServiceDiscovered(
                        peerHandle: PeerHandle,
                        serviceSpecificInfo: ByteArray,
                        matchFilter: List<ByteArray>
                    ) {
                        Toast.makeText(this@MainActivity,"Sending message",Toast.LENGTH_SHORT).show()

                        if(sessionDiscovered!=null){
                            var message = coords.toString().toByteArray()
                            sessionDiscovered!!.sendMessage(peerHandle,0,message)
                        }
                    }
                }, null)

                //publishing a session too
                val config: PublishConfig = PublishConfig.Builder()
                    .setServiceName(serviceName)
                    .build()
               session!!.publish(config,object : DiscoverySessionCallback() {
                   override fun onPublishStarted(session: PublishDiscoverySession) {
                       Toast.makeText(this@MainActivity,"On Published",Toast.LENGTH_SHORT).show()


                   }
                   override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                       Toast.makeText(this@MainActivity,message.toString(),Toast.LENGTH_SHORT).show()

                   }
               },handler)
            }
        }

        if(this.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE) ){
            Toast.makeText(this,"packet manager is avaialber",Toast.LENGTH_SHORT).show()


            val wifiAwareManager = this.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
            val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
            val myReceiver = object : BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    Toast.makeText(context,"in Reciever",Toast.LENGTH_SHORT).show()

                    // discard current sessions
                    if (wifiAwareManager?.isAvailable == true) {
                       Toast.makeText(context,"is Availabel mangaer",Toast.LENGTH_SHORT).show()

                        wifiAwareManager.attach(attachCallback,handler)
                    } else {
                        Toast.makeText(context,"Isnt Availabel mangaer",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            this.registerReceiver(myReceiver, filter)
        }


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
        Log.i("CSV","File written succsddfully !")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            if (event!!.sensor == stepSensor) {
                val index = coords.lastIndex
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

    fun writeDatatoCSV() {
        val filename = "Path.csv";
        val filepath = "Walk-Path";

        val myExternalFile = File(getExternalFilesDir(filepath), filename)
        // Create an object of FileOutputStream for writing data to myFile.txt
        var fos: FileOutputStream? = null
        try {
            // Instantiate the FileOutputStream object and pass myExternalFile in constructor
            fos = FileOutputStream(myExternalFile)
            // Write to the file
            for (iter in 0 until coords.lastIndex) {
              val tempString:String =  ""+coords[iter][0]+","+coords[iter][0]+"\n"
                fos.write(tempString.toByteArray())
            }

            // Close the stream
            fos.close()
            Log.i("CSV","Success")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.i("CSV","Not Success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("CSV","Fail")
        }
        Toast.makeText(this@MainActivity, "Information saved to SD card.", Toast.LENGTH_SHORT).show()
    }
}