package com.grinleaf.tpexample01.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grinleaf.tpexample01.R
import com.grinleaf.tpexample01.activities.PlaceUrlActivity
import com.grinleaf.tpexample01.databinding.RecyclerItemListFragmentBinding
import com.grinleaf.tpexample01.model.Place

class PlaceListRecyclerAdapter(val context:Context, var documents:MutableList<Place>) : RecyclerView.Adapter<PlaceListRecyclerAdapter.VH>() {
    inner class VH(itemView:View) : RecyclerView.ViewHolder(itemView){
        val binding:RecyclerItemListFragmentBinding = RecyclerItemListFragmentBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView= LayoutInflater.from(context).inflate(R.layout.recycler_item_list_fragment, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place= documents[position]
        holder.binding.tvPlaceName.text= place.place_name
        holder.binding.tvAddress.text= if(place.road_address_name=="") place.address_name else place.road_address_name
        holder.binding.tvDistance.text= place.distance+"m"

        //각각의 장소 아이템뷰 클릭 시 장소에 대한 상세정보 화면으로 이동
        holder.itemView.setOnClickListener {
            val intent:Intent= Intent(context, PlaceUrlActivity::class.java)
            intent.putExtra("place_url",place.place_url)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = documents.size
}