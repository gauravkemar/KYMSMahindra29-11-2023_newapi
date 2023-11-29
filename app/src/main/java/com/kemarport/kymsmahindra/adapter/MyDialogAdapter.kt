package com.kemarport.kymsmahindra.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kymsmahindra.databinding.DialogItemBinding

class MyDialogAdapter(val items: List<Int>, val listener: (Int) -> Unit) : RecyclerView.Adapter<MyDialogAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(val dialogItemBinding: DialogItemBinding) : RecyclerView.ViewHolder(dialogItemBinding.root) {
         fun bind(item: Int) {
             dialogItemBinding.tvPeriod.text = "Period $item"
             itemView.setOnClickListener {
                 listener.invoke(item)
             }
         }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(DialogItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

}
