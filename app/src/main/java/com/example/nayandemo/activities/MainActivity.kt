package com.example.nayandemo.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.nayandemo.R
import com.example.nayandemo.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataUpdateRequest

import com.google.android.gms.tasks.Task
import viewmodels.GitApiStatus
import viewmodels.MainViewModel
import java.util.*
import java.util.concurrent.TimeUnit


const val KEY_REPO_DATA = "REPO_DATA"
const val PREF_NAME = "myPref"
const val PREF_KEY_STEP_COUNT = "step_count"
const val PREF_KEY_USER_ID = "userid"
const val PREF_KEY_TOKEN = "token"

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback,
    SensorEventListener {


    private var isSensorPresent: Boolean = false
    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION: Int = 100
    private val PERMISSION_GOOGLE_SIGN_IN: Int = 101
    private lateinit var fitnessOptions: FitnessOptions;
    private lateinit var viewModel: MainViewModel
    lateinit var mainBinding: ActivityMainBinding

    //Sensor related variables
    private lateinit var sensor: Sensor;
    private lateinit var sensorManager: SensorManager

    private var stepCountFromSensor: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        //testUser(viewModel)
        viewModel.status.observe(this, Observer {
            when (it) {
                GitApiStatus.ERROR -> {
                    mainBinding.pb.visibility = View.GONE
                }
                GitApiStatus.LOADING -> {
                    mainBinding.pb.visibility = View.VISIBLE
                }
                GitApiStatus.DONE -> {
                    mainBinding.pb.visibility = View.GONE
                }
            }
        })

        //Logic to determin which elements to show on the screen .
        mainBinding.pb.visibility = View.GONE
        toggleLoginScreenElementsVisibility(isLoggedIn())
        mainBinding.btLogin.setOnClickListener { login() }
        mainBinding.stepBtRecord.setOnClickListener { startSensor() }
        mainBinding.stepBtUpdateServer.setOnClickListener {
            viewModel.updateStepCount(
                mainBinding.stepEtCount.text.toString().toInt() + stepCountFromSensor!!
            )
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            isSensorPresent = true
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        } else {
            isSensorPresent = false
        }
        setUpObservers()

    }

    private fun setUpObservers() {

        viewModel.loginResult.observe(this, Observer {
            showServerResponse(it.toString())
            Log.d("MainActivity", it.toString())
            viewModel.saveInPref(it.userid, it.token)
            toggleLoginScreenElementsVisibility(isLoggedIn())
            checkActivityRecognitionAndGoogleSignIn()
        })

        viewModel.stepCount.observe(this, Observer {
            showServerResponse(it.toString())
        })

    }

    override fun onResume() {
        super.onResume()
        startSensor()
        mainBinding.stepEtCount.text =
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).getInt(PREF_KEY_STEP_COUNT, 0).toString()

    }

    private fun startSensor() {
        if (isLoggedIn() && isSensorPresent) {
            if (isSensorPresent) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        stepCountFromSensor = p0?.values?.get(0)?.toInt()
        mainBinding.stepEtCount.text =
            (mainBinding.stepEtCount.text.toString().toInt() + stepCountFromSensor!!).toString()

    }

    //Google Fit Related Code.


    private fun checkActivityRecognitionAndGoogleSignIn() {
        //Check for the ACTIVITY_RECOGNITION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION
            );

        } else {
            startSensor()
            googleSignInAndAccessGoogleFitnessData()
        }


    }

    fun googleSignInAndAccessGoogleFitnessData() {
        //The ACTIVITY_RECOGNITION has been granted we need to check the Google account permission
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                PERMISSION_GOOGLE_SIGN_IN, // e.g. 1
                account,
                fitnessOptions
            )
        } else {
            getTotalSteps()
            recordSteps()
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == PERMISSION_GOOGLE_SIGN_IN) {
                startSensor()
                getTotalSteps()
                recordSteps()
            } else {
                permissionNotGranted(this, "User didnt grant GOOGLE ACCOUNT permission")
            }
        } else {
            permissionNotGranted(this, "User didnt grant GOOGLE ACCOUNT permission")
        }
    }

    //This function will fetch the current no of steps from the Google Fitness API .
    private fun getTotalSteps() {
        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { result: DataSet ->
                val totalSteps =
                    if (result.isEmpty) 0 else result.dataPoints[0].getValue(Field.FIELD_STEPS)
                        .asInt()
                mainBinding.stepEtCount.text = totalSteps.toString()
                //Save Step Count to shared Prefs so on app restart we can display the same
                getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                    .putInt(PREF_KEY_STEP_COUNT, totalSteps)
                    .apply()
            }
            .addOnFailureListener { e: Exception ->
                Log.i(
                    "MainActivity", "There was a problem getting steps: " +
                            e.localizedMessage
                )
            }
    }


    //This function turns on the sensor in the background which will increase the step count
    private fun recordSteps() {
        val recordingClient = Fitness.getRecordingClient(
            this,
            GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        )
            .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { unused: Void? ->
                Log.i(
                    "MainActivity",
                    "Successfully subscribed!"
                )
            }
            .addOnFailureListener { e: java.lang.Exception? ->
                e?.printStackTrace()
                Log.i(
                    "MainActivity",
                    "There was a problem subscribing."
                )
            }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            googleSignInAndAccessGoogleFitnessData()
        } else {
            permissionNotGranted(this, "User didnt grant ACTIVITY_RECOGNITION permission")
        }
    }

    private fun showServerResponse(response: String) {
        Toast.makeText(mainBinding.root.context, response, Toast.LENGTH_LONG).show()
    }

    private fun permissionNotGranted(activity: MainActivity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
        finish()

    }

    //Function to toggle the visibility
    fun toggleLoginScreenElementsVisibility(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            mainBinding.tvEmail.visibility = View.GONE
            mainBinding.tvPassword.visibility = View.GONE
            mainBinding.etEmail.visibility = View.GONE
            mainBinding.etPassword.visibility = View.GONE
            mainBinding.btLogin.visibility = View.GONE
            //Elements for step
            mainBinding.stepBtRecord.visibility = View.VISIBLE
            mainBinding.stepEtLabel.visibility = View.VISIBLE
            mainBinding.stepEtCount.visibility = View.VISIBLE
            mainBinding.stepSensorCount.visibility = View.VISIBLE
            mainBinding.stepBtUpdateServer.visibility = View.VISIBLE
        } else {
            mainBinding.tvEmail.visibility = View.VISIBLE
            mainBinding.tvEmail.visibility = View.VISIBLE
            mainBinding.tvPassword.visibility = View.VISIBLE
            mainBinding.etEmail.visibility = View.VISIBLE
            mainBinding.etPassword.visibility = View.VISIBLE
            mainBinding.btLogin.visibility = View.VISIBLE
            //Elements for step
            mainBinding.stepBtRecord.visibility = View.GONE
            mainBinding.stepEtLabel.visibility = View.GONE
            mainBinding.stepEtCount.visibility = View.GONE
            mainBinding.stepSensorCount.visibility = View.GONE
            mainBinding.stepBtUpdateServer.visibility = View.GONE

        }


    }


    private fun isLoggedIn(): Boolean {
        return !TextUtils.isEmpty(
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(
                PREF_KEY_TOKEN,
                ""
            )
        )
    }

    fun login() {
        if (!TextUtils.isEmpty(mainBinding.etEmail.text) && !TextUtils.isEmpty(mainBinding.etPassword.text)) {
            viewModel.loginUser(
                mainBinding.etEmail.text.toString(),
                mainBinding.etPassword.text.toString()
            )

        } else {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_LONG).show()
        }

    }


    private fun unsubscribeFromGoogleFitness() {
        if (this::fitnessOptions.isInitialized) {
            Fitness.getRecordingClient(
                this,
                GoogleSignIn.getAccountForExtension(this, fitnessOptions)
            )
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener { unused: Void? ->
                    Log.i(
                        "MainActivity",
                        "Successfully unsubscribed."
                    )
                }
                .addOnFailureListener { e: java.lang.Exception? ->
                    // Subscription not removed
                    Log.i("MainActivity", "Failed to unsubscribe.")
                }
        }

    }

    private fun unsubscribeFromSensor() {
        if (isSensorPresent && this::sensor.isInitialized && this::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
    }


    override fun onPause() {
        super.onPause()
        unsubscribeFromGoogleFitness()
        unsubscribeFromSensor()

    }

    override fun onStop() {
        super.onStop()
        //Unsubscribe from the Google Fitness subscription
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
            .putInt(PREF_KEY_STEP_COUNT, mainBinding.stepEtCount.text.toString().toInt())

        unsubscribeFromGoogleFitness()
        unsubscribeFromSensor()
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    fun updateGoogleFitWithLatestStepCount(){
        val cal: Calendar = Calendar.getInstance()
        val now = Date()
        cal.setTime(now)
        cal.add(Calendar.MINUTE, 0)
        val endTime: Long = cal.getTimeInMillis()
        cal.add(Calendar.MINUTE, -50)
        val startTime: Long = cal.getTimeInMillis()

// Create a data source

// Create a data source
        val dataSource: DataSource = DataSource.Builder()
            .setAppPackageName(this)
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setStreamName("MainActivity" + " - step count")
            .setType(DataSource.TYPE_RAW)
            .build()


// Create a data set


// Create a data set
        val stepCountDelta = 1000
// For each data point, specify a start time, end time, and the data value -- in this case,
// the number of new steps.
// For each data point, specify a start time, end time, and the data value -- in this case,
// the number of new steps.
        val dataPoint: DataPoint = DataPoint.builder(dataSource)
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .setField(Field.FIELD_STEPS, stepCountDelta)
            .build()

        val dataSet = DataSet.builder(dataSource)
            .add(dataPoint)
            .build()

        val request = DataUpdateRequest.Builder()
            .setDataSet(dataSet)
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        val response: Task<Void> = Fitness
            .getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .updateData(request)


    }


}



