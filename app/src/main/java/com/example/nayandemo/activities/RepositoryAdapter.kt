package com.example.nayandemo.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import api.RepositoryDataClass
import com.example.nayandemo.databinding.ItemRepoBinding

class RepositoryAdapter(val repositories: List<RepositoryDataClass>) :
    RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {


    class RepositoryViewHolder(private var binding: ItemRepoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBindViewHolder(repositoryDataClass: RepositoryDataClass) {
            binding.repository = repositoryDataClass
            binding.executePendingBindings()
        }

        companion object {

            fun from(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
                return RepositoryViewHolder(ItemRepoBinding.inflate(LayoutInflater.from(parent.context)))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        return RepositoryViewHolder.from(parent, viewType)
    }

    override fun getItemCount(): Int {
        return repositories.size
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        holder.onBindViewHolder(repositories.get(position))
    }
}