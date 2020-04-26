package com.example.android_sensors


import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils


class BallGame : AppCompatActivity(), SensorEventListener {


    private var xPos: Float = 0.0f
    private var xAccel: Float = 0.0f
    private var xVel: Float = 0.0f
    private var yPos: Float = 0.0f
    private var yAccel: Float = 0.0f
    private var yVel: Float = 0.0f
    private var xMax: Float = 0.0f
    private var yMax: Float = 0.0f

    internal lateinit var ball: Bitmap
    private lateinit var sensorManager: SensorManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation =  (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        val ballView = BallView(this)
        setContentView(ballView)

        var size = Point()
        var screenDisplay = windowManager.defaultDisplay
        screenDisplay.getSize(size)
        xMax = size.x.toFloat() - 100
        yMax = size.y.toFloat() - 220
        xPos = size.x.toFloat()/2
        yPos = size.y.toFloat()/2

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }


    override fun onStart() {
        super.onStart()

        sensorManager!!.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onStop() {
        sensorManager.unregisterListener(this)
        super.onStop()
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            xAccel = sensorEvent.values[0]
            yAccel = -sensorEvent.values[1]
            updateBall()
        }
    }

    private fun updateBall() {
        val frameTime = 0.666f
        xVel += xAccel * frameTime
        yVel += yAccel * frameTime
        val xS = xVel / 2 * frameTime
        val yS = yVel / 2 * frameTime
        xPos -= xS
        yPos -= yS
        if (xPos > xMax) {
           // xPos = xMax
            gameOver()
        } else if (xPos < 0) {
            //xPos = 0f
            gameOver()
        }
        if (yPos > yMax) {
           // yPos = yMax
            gameOver()
        } else if (yPos < 0) {
            //yPos = 0f
            gameOver()
        }
    }

    private fun gameOver() {
        sensorManager.unregisterListener(this)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game over!")
        builder.setMessage("Do you want to try again?")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            this.recreate()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            NavUtils.navigateUpFromSameTask(this)
        }
        builder.show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, i: Int) {}

    inner class BallView(context: Context?) : View(context) {
         override fun onDraw(canvas: Canvas) {
            canvas.drawBitmap(ball, xPos, yPos, null)
            invalidate()
        }

        init {
            val ballSrc = BitmapFactory.decodeResource(resources, R.drawable.iz_ball)
            val dstWidth = 100
            val dstHeight = 100
            ball = Bitmap.createScaledBitmap(ballSrc, dstWidth, dstHeight, true)
        }
    }
}


