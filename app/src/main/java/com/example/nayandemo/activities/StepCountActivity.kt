package com.example.nayandemo.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.nayandemo.R
import com.example.nayandemo.databinding.ActivityMainBinding
import com.example.nayandemo.databinding.ActivityStepCountBinding

class StepCountActivity : AppCompatActivity() {


    lateinit var mainBinding: ActivityStepCountBinding
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_step_count)
    }
}