package com.bernademir.recipebook

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

class ListFragment : Fragment() {

    var mealNameList = ArrayList<String>()
    var mealIdList = ArrayList<Int>()
    private lateinit var listAdapter : ListRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)

        listAdapter = ListRecyclerAdapter(mealNameList, mealIdList)
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.layoutManager = LinearLayoutManager(context)
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter = listAdapter


        sqlFetchData()
    }

    fun sqlFetchData(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Meals", Context.MODE_PRIVATE, null)

                val cursor = database.rawQuery("SELECT * FROM meals", null)
                val mealNameIndex = cursor.getColumnIndex("mealName")
                val idIndex = cursor.getColumnIndex("id")

                mealNameList.clear()
                mealIdList.clear()

                while(cursor.moveToNext()){
                    mealNameList.add(cursor.getString(mealNameIndex))
                    mealIdList.add(cursor.getInt(idIndex))
                }
                listAdapter.notifyDataSetChanged()
                cursor.close()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}