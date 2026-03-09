package com.example.myapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class SensorUtils(
    private val context: Context,
    private val onRotateDetected: (() -> Unit),
    private var onSensorValuesChange: ((Float, Float, Float) -> Unit)

) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // orientation states: 0: initial, 1: Portrait, 2: Landscape
    private var lastOrientation: Int = 0

    fun start() {
        sensorManager.registerListener(
            this,
            rotationSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            onSensorValuesChange(0f, 0f, 0f)

            val currentOrientation = if (abs(roll) < 45) {
                1 // Portrait
            } else {
                2 // Landscape
            }

            // Only trigger the notification if the orientation has changed from one state to another
            if (currentOrientation != lastOrientation) {
                // This check prevents triggering a notification on the very first sensor reading
                if (lastOrientation != 0) {
                    onRotateDetected()
                }
                lastOrientation = currentOrientation
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}