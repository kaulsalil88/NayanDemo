package com.example.nayandemo.activities

import android.content.Intent
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

const val KEY_REPO_DATA = "REPO_DATA"

class MainActivity : AppCompatActivity() {


    lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        viewModel.getRepositories()
        viewModel.result.observe(this, Observer {
            if (it != null && it.size > 0) {
                val repoAdapter = RepositoryAdapter(it, RepositoryAdapter.RepoOnClickListener {
                    val intentForDetails = Intent(this@MainActivity, DetailsActivity::class.java)
                    intentForDetails.putExtra(KEY_REPO_DATA, it)
                    startActivity(intentForDetails)
                })
                mainBinding.rvRepos.adapter = repoAdapter

            }
        })
    }
}
