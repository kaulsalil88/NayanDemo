package com.example.nayandemo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.nayandemo.R
import com.example.nayandemo.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    lateinit var detailsBinding: ActivityDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details)
        detailsBinding.repo = intent.getParcelableExtra(KEY_REPO_DATA)

    }
}
