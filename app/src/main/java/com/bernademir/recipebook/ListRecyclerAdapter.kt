package com.bernademir.recipebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class ListRecyclerAdapter(val mealList: ArrayList<String>, val idList: ArrayList<Int>) : RecyclerView.Adapter<ListRecyclerAdapter.MealsViewHolder>() {
    class MealsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealsViewHolder {//rowlarin hangi tasarimla olusturulacagini belirler
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row, parent, false)
        return MealsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealsViewHolder, position: Int) {//rowlarin icerisine ne konacagini belirler
        holder.itemView.findViewById<TextView>(R.id.recyclerRowText).text = mealList[position]
        holder.itemView.setOnClickListener{
            val action = ListFragmentDirections.actionListFragmentToRecipeFragment("fromrecycler", idList[position])
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {//kac tane row olusturulacagini belirler
        return mealList.size
    }
}