package com.example.bookapp.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookapp.R
import com.example.bookapp.adapter.DashboardRecyclerAdapter
import com.example.bookapp.model.Book
import com.example.bookapp.util.connectionManager
import org.json.JSONException

class DashBoardfragment : Fragment() {


    lateinit var recyclerDashboard : RecyclerView

    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var recyclerAdapter: DashboardRecyclerAdapter

    lateinit var prgLayout : RelativeLayout

    lateinit var prgBar : ProgressBar

    val bookListInfo = arrayListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {



        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)

        prgLayout = view.findViewById(R.id.progressLayout)

        prgBar = view.findViewById(R.id.progressBar)

        prgLayout.visibility = View.VISIBLE



        layoutManager = LinearLayoutManager(activity)



        val queue = Volley.newRequestQueue(activity as Context)

        val url = "http://13.235.250.119/v1/book/fetch_books/"



        if(connectionManager().checkConnectivity(activity as Context)){
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET,url, null,
                Response.Listener {

                    println("Response is $it")
                    try {
                        prgLayout.visibility = View.GONE
                        val success = it.getBoolean("success")

                        if(success){
                            val data = it.getJSONArray("data")
                            for(i in 0 until data.length()){
                                val bookJsonObject = data.getJSONObject(i)
                                val bookObject = Book(
                                    bookJsonObject.getString("book_id"),
                                    bookJsonObject.getString("name"),
                                    bookJsonObject.getString("author"),
                                    bookJsonObject.getString("rating"),
                                    bookJsonObject.getString("price"),
                                    bookJsonObject.getString("image")
                                )

                                bookListInfo.add(bookObject)

                                recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookListInfo)

                                recyclerDashboard.adapter = recyclerAdapter

                                recyclerDashboard.layoutManager = layoutManager

                            }
                        }


                        else {
                            Toast.makeText(activity as Context, "Some Error Occurred!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    catch (e: JSONException){
                        Toast.makeText(activity as Context, "Some unexpected error occurred!", Toast.LENGTH_SHORT).show()
                    }

                } , Response.ErrorListener {


                    println("Error is $it")
                }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "9bf534118365f1"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        } else {
            val builder = AlertDialog.Builder(activity as Context)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
//            builder.setCancelable(false)
            builder.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()

            }


            builder.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            builder.create()
            builder.show()

        }




        return view
    }

}
