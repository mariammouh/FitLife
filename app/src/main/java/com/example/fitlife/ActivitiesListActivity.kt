package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivitiesListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var spinnerSort: Spinner
    private lateinit var txtEmpty: TextView
    private lateinit var fabAdd: FloatingActionButton

    private var userId: Int = -1
    private lateinit var adapter: ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        userId = intent.getIntExtra("USER_ID", -1)
        Log.d("ACTIVITIES_DEBUG", "ActivitiesList opened with USER_ID: $userId")

        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        recycler = findViewById(R.id.recyclerActivities)
        etSearch = findViewById(R.id.etSearch)
        spinnerSort = findViewById(R.id.spinnerSort)
        txtEmpty = findViewById(R.id.txtEmpty)
        fabAdd = findViewById(R.id.fabAddActivity)

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ActivityAdapter(this)
        recycler.adapter = adapter

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        setupSearch()
        setupSort()
    }

    override fun onResume() {
        super.onResume()
        loadActivities()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filterList(s.toString())
                updateEmptyState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSort() {
        val sortOptions = listOf("Date", "Duration", "Type")
        val sortAdapter = ArrayAdapter(this, R.layout.spinner_item_dark, sortOptions)
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)
        spinnerSort.adapter = sortAdapter
        
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapter.sortList(sortOptions[position])
                updateEmptyState()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadActivities() {
        RetrofitClient.instance.getActivities(userId).enqueue(object : Callback<ActivityResponse> {
            override fun onResponse(call: Call<ActivityResponse>, response: Response<ActivityResponse>) {
                if (response.isSuccessful) {
                    val activities = response.body()?.activities ?: emptyList()
                    adapter.updateData(activities.toMutableList())
                    updateEmptyState()
                } else {
                    Log.e("ACTIVITIES_DEBUG", "Server Error: ${response.code()}")
                    Toast.makeText(this@ActivitiesListActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ActivityResponse>, t: Throwable) {
                Log.e("ACTIVITIES_DEBUG", "Fail: ${t.message}")
                Toast.makeText(this@ActivitiesListActivity, "Connection Error: Check server/IP", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateEmptyState() {
        txtEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }
}