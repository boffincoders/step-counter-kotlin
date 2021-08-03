package com.boffin.stepcounter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator


class MainActivity : AppCompatActivity(), SensorEventListener, View.OnClickListener {
    private var stepsTextView: TextView? = null
    private var pendingStepsTextView: TextView? = null
    private var sleepTextView: TextView? = null
    private var heartRateTextView: TextView? = null
    private var stepsProgressBar: CircularProgressIndicator? = null
    private var caloriesProgressBar: CircularProgressIndicator? = null
    private var sensorManager: SensorManager? = null;
    private var running = false
    private var stepsTraget = 5000
    private var previousTotalSteps = 0f
    private var totalSteps = 0f
    private var sensorAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    private var stepSensor: Sensor? = null
    private var heartRateSensor: Sensor? = null
    private var burnedcalleries=0f
    private var burnedcalleriesTarget=500f
    private var pendingSteps=0


    var stepCounterTrigger = object : TriggerEventListener() {
        override fun onTrigger(event: TriggerEvent?) {
            totalSteps = event!!.values[0]
            // Current steps are calculated by taking the difference of total steps
            // and previous steps
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            // It will show the current steps to the user
            stepsTextView?.text = ("$currentSteps steps")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        loadData()
        // resetSteps()
        stepsTextView = findViewById(R.id.steps_text)
        sleepTextView = findViewById(R.id.sleepTextView)
        pendingStepsTextView=findViewById(R.id.pendingStepsText)
        stepsProgressBar = findViewById(R.id.stepsProgressBar)
        caloriesProgressBar=findViewById(R.id.caloriesProgressBar)
        heartRateTextView=findViewById(R.id.heartRateText)
        stepsTextView?.setOnClickListener(this)
        stepsProgressBar!!.maxProgress = stepsTraget.toDouble()
        caloriesProgressBar!!.maxProgress=burnedcalleriesTarget.toDouble()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        pendingStepsTextView!!.text=("$stepsTraget steps away to reach daily goal" )
    }

    override fun onResume() {
        super.onResume()

        if (!running) {
            running = true
            stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            heartRateSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE)

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    101
                )

            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (stepSensor != null)
                    registerSensor(stepSensor)else
                    Toast.makeText(this, "Step counter sensor not found", Toast.LENGTH_SHORT).show()
                if (heartRateSensor != null)
                    registerSensor(heartRateSensor)
                else
                    Toast.makeText(this, "Heart rate sensor not found", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun registerSensor(sensor: Sensor?) {
        sensorManager?.registerListener(
            this,
            sensor,
            sensorAccuracy
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (running) {
            when (event!!.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {

                    sensorManager?.requestTriggerSensor(stepCounterTrigger, stepSensor)
                    print(event!!.sensor.type)
                    totalSteps = event!!.values[0]
                    if (previousTotalSteps < 1) {
                        previousTotalSteps = totalSteps
                        saveData()

                    }
                    // Current steps are calculated by taking the difference of total steps
                    // and previous steps
                    var currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                    //val currentStep =tTotal.toInt() - previousTotalSteps.toInt()
                    // It will show the current steps to the user
                  burnedcalleries=  currentSteps/ 100f
                    stepsTextView?.text = ("$currentSteps steps")

                    pendingSteps=stepsTraget-currentSteps

                    pendingStepsTextView!!.text=("$pendingSteps steps away to reach daily goal" )

                    caloriesProgressBar!!.setCurrentProgress(burnedcalleries.toDouble())
                    stepsProgressBar!!.setCurrentProgress(currentSteps.toDouble())

                    // sleepTextView?.text=tTotal.toString()
                    //to handle listen step counter sensor just once when on onResume fire


                }

                Sensor.TYPE_HEART_RATE -> {
                    val hRate = event!!.values


                    heartRateTextView!!.text = (" Heart rate ${hRate} BPM")
                }
            }


        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        print(accuracy);
    }

    fun resetSteps() {

        previousTotalSteps = totalSteps
        stepsTextView?.text = "0 steps";
        saveData();


    }

    private fun saveData() {

        // Shared Preferences will allow us to save
        // and retrieve data in the form of key,value pair.
        // In this function we will save data
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {

        // In this function we will retrieve data
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)

        // Log.d is used for debugging purposes

        previousTotalSteps = savedNumber
    }

    override fun onClick(v: View?) {
        resetSteps()
    }


}