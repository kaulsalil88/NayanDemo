package com.example.nayandemo.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.nayandemo.R
import com.example.nayandemo.databinding.ActivityMainBinding
import viewmodels.GitApiStatus
import viewmodels.MainViewModel


const val KEY_REPO_DATA = "REPO_DATA"

class MainActivity : AppCompatActivity() {


    lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        mainBinding.rvRepos.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.default_padding).toInt()
            )
        )
        testUser(viewModel)
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
    }

    private fun testUser(viewModel: MainViewModel) {
        viewModel.loginUser()
        viewModel.loginResult.observe(this, Observer {
            showServerResponse(it.toString())
            Log.d("MainActivity", it.toString())
        })

    }

    private fun interActWithGit(viewModel: MainViewModel) {
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

    private fun showServerResponse(response: String) {
        Toast.makeText(mainBinding.root.context, response, Toast.LENGTH_LONG).show()
    }


}


class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceHeight
            }
            left = spaceHeight
            right = spaceHeight
            bottom = spaceHeight
        }
    }
}
