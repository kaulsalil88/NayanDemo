package com.example.nayandemo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.nayandemo.R
import com.example.nayandemo.databinding.ActivityMainBinding
import viewmodels.MainViewModel
import java.util.*

class MainActivity : AppCompatActivity() {


    lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        viewModel.getRepositories()
        viewModel.result.observe(this, Observer {
            if (it != null && it.size > 0) {
                // val layoutManager = RecyclerView.LayoutManager(RecyclerView.LayoutManager.)
                val repoAdapter = RepositoryAdapter(it)
                mainBinding.rvRepos.adapter = repoAdapter

            }
        })
    }
}
