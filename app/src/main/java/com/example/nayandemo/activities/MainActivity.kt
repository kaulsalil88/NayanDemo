package com.example.nayandemo.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.nayandemo.R
import com.example.nayandemo.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import viewmodels.GitApiStatus
import viewmodels.MainViewModel
import java.util.*
import java.util.concurrent.TimeUnit


const val KEY_REPO_DATA = "REPO_DATA"

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {


    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION: Int = 100
    private val PERMISSION_GOOGLE_SIGN_IN: Int = 101

    lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]

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
        mainBinding.btSecondApi.setOnClickListener {
            //Call second API
            viewModel.fetchStepCount()
        }

        mainBinding.bt3Api.setOnClickListener {
            //Call 3 rd API .
            updateStepCount()
        }
    }

    private fun testUser(viewModel: MainViewModel) {
        viewModel.loginUser()
        viewModel.loginResult.observe(this, Observer {
            showServerResponse(it.toString())
            Log.d("MainActivity", it.toString())
            viewModel.saveInPref(it.userid, it.token)
        })

    }

    //Google Fit Related Code.

    lateinit var fitnessOptions: FitnessOptions;
    private fun updateStepCount() {
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
            //accessGoogleFit();
            getTotalSteps()
            recordSteps()
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == PERMISSION_GOOGLE_SIGN_IN) {
                //accessGoogleFit()
                getTotalSteps()
                recordSteps()
            } else {
                permissionNotGranted(this, "User didnt grant GOOGLE ACCOUNT permission")
            }
        } else {
            permissionNotGranted(this, "User didnt grant GOOGLE ACCOUNT permission")
        }
    }

    private fun accessGoogleFit() {
        val cal: Calendar = Calendar.getInstance()
        cal.setTime(Date())
        val endTime: Long = cal.getTimeInMillis()
        cal.add(Calendar.YEAR, -1)
        val startTime: Long = cal.getTimeInMillis()
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
        val account = GoogleSignIn
            .getAccountForExtension(this, fitnessOptions)
        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                // Use response data here
                Log.d("MainActivity", "OnSuccess()")

            }
            .addOnFailureListener { e -> Log.d("MainActivity", "OnFailure()", e) }
    }

    private fun getTotalSteps() {
        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { result: DataSet ->
                val totalSteps =
                    if (result.isEmpty) 0 else result.dataPoints[0].getValue(Field.FIELD_STEPS)
                        .asInt()
                Log.e("MainActivity", "Step Count:" + totalSteps)
            }
            .addOnFailureListener { e: Exception ->
                Log.i(
                    "MainActivity", "There was a problem getting steps: " +
                            e.localizedMessage
                )
            }
    }

    private fun recordSteps() {
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
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


    override fun onStop() {
        super.onStop()
        //Unsubscribe from the Google Fitness subscription
    }


}



