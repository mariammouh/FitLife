package com.example.fitlife

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray

class ActivitiesListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var spinnerSort: Spinner
    private lateinit var txtEmpty: TextView

    private val list = mutableListOf<ActivityModel>()
    private lateinit var adapter: ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recycler = findViewById(R.id.recyclerActivities)
        etSearch = findViewById(R.id.etSearch)
        spinnerSort = findViewById(R.id.spinnerSort)
        txtEmpty = findViewById(R.id.txtEmpty)

        recycler.layoutManager = LinearLayoutManager(this)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = ActivityAdapter(this)
        recycler.adapter = adapter

        setupSearch()
        setupSort()

        loadActivities()
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

        val sortAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_dark,
            sortOptions
        )
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)
        spinnerSort.adapter = sortAdapter
        spinnerSort.setSelection(0)

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapter.sortList(sortOptions[position])
                updateEmptyState()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadActivities() {
        val url = "http://10.0.2.2/fitlife/get_user_activities.php"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val json = JSONArray(response)
                    list.clear()

                    for (i in 0 until json.length()) {
                        val obj = json.getJSONObject(i)

                        list.add(
                            ActivityModel(
                                obj.getString("activity_id"),
                                obj.getString("activity_name"),
                                obj.getString("activity_type"),
                                obj.getString("duration_minutes"),
                                obj.getString("distance_km"),
                                obj.getString("calories_burned"),
                                obj.getString("avg_heart_rate"),
                                obj.getString("max_heart_rate"),
                                obj.getString("sets"),
                                obj.getString("reps_per_set"),
                                obj.getString("weight_used_kg"),
                                obj.getString("intensity"),
                                obj.getString("mood_before"),
                                obj.getString("mood_after"),
                                obj.getString("notes"),
                                obj.getString("location"),
                                obj.getString("activity_date"),
                                obj.getString("start_time"),
                                obj.getString("end_time")
                            )
                        )
                    }

                    adapter.updateData(list)

                    val currentQuery = etSearch.text?.toString()?.trim() ?: ""
                    if (currentQuery.isNotEmpty()) {
                        adapter.filterList(currentQuery)
                    }

                    updateEmptyState()

                } catch (e: Exception) {
                    Toast.makeText(this, "Erreur JSON : ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Erreur : ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun updateEmptyState() {
        txtEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }
}