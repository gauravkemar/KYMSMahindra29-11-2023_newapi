package com.kemarport.kymsmahindra.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kymsmahindra.model.newapi.prdout.PrdOutListResponse
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.activity.newactivity.ParkReparkActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrdOutAdapter: RecyclerView.Adapter<PrdOutAdapter.ViewHolder>() {

    private var prdOutList = mutableListOf<PrdOutListResponse>()
    private var context: Context?=null

    fun setDockLevelerList(
        prdOutList: ArrayList<PrdOutListResponse>,
        context: Context,
    ) {
        this.prdOutList = prdOutList
        this.context=context
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.production_out_list_item, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var prdMod:PrdOutListResponse=prdOutList.get(position)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

        try {
            val date: Date = inputFormat.parse(prdMod.prodOutAt ?: "")
            val formattedDate: String = outputFormat.format(date)
            holder.tvDate.text = formattedDate ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
        holder.tvVin.text = prdMod.vin ?: ""

        holder.tvModelCode.text = prdMod.modelCode ?: ""
        holder.tvModelDescription.text = prdMod.modelDescription ?: ""
        holder.tvColor.text = prdMod.colorDescription ?: ""

        holder.itemView.setOnClickListener {
            var intent= Intent(context,ParkReparkActivity::class.java)
            intent.putExtra("vin",prdMod.vin)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        if(prdOutList.size==0){
            //Toast.makeText(context,"List is empty", Toast.LENGTH_LONG).show()
        }else{
            return prdOutList.size
        }
        return prdOutList.size
    }
    class ViewHolder (ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvVin: TextView = itemView.findViewById(R.id.tvVin)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvModelCode: TextView = itemView.findViewById(R.id.tvModelCode)
        val tvModelDescription: TextView = itemView.findViewById(R.id.tvModelDescription)
        val tvColor: TextView = itemView.findViewById(R.id.tvColor)
    }
}