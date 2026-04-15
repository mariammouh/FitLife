package com.example.fitlife

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class ActivityAdapter(
    private val context: Context
) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    private val originalList = mutableListOf<ActivityModel>()
    private val filteredList = mutableListOf<ActivityModel>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtType: TextView = itemView.findViewById(R.id.txtType)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtDuration: TextView = itemView.findViewById(R.id.txtDuration)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = filteredList[position]

        holder.txtName.text = activity.name ?: "Unnamed"
        holder.txtType.text = activity.type ?: "General"
        holder.txtDate.text = activity.date ?: ""
        holder.txtDuration.text = "${activity.duration ?: "0"} min"

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityDetailActivity::class.java)
            intent.putExtra("activity_id", activity.id)
            intent.putExtra("name", activity.name)
            intent.putExtra("type", activity.type)
            intent.putExtra("duration", activity.duration)
            intent.putExtra("distance", activity.distance)
            intent.putExtra("calories", activity.calories)
            intent.putExtra("avg_heart", activity.avgHeart)
            intent.putExtra("max_heart", activity.maxHeart)
            intent.putExtra("sets", activity.sets)
            intent.putExtra("reps", activity.reps)
            intent.putExtra("weight", activity.weight)
            intent.putExtra("intensity", activity.intensity)
            intent.putExtra("mood_before", activity.moodBefore)
            intent.putExtra("mood_after", activity.moodAfter)
            intent.putExtra("location", activity.location)
            intent.putExtra("notes", activity.notes)
            intent.putExtra("date", activity.date)
            intent.putExtra("start", activity.start)
            intent.putExtra("end", activity.end)
            context.startActivity(intent)
        }

        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, EditActivityFormActivity::class.java)

            intent.putExtra("activity_id", activity.id)
            intent.putExtra("name", activity.name)
            intent.putExtra("type", activity.type)
            intent.putExtra("duration", activity.duration)
            intent.putExtra("distance", activity.distance)
            intent.putExtra("calories", activity.calories)
            intent.putExtra("avg_heart", activity.avgHeart)
            intent.putExtra("max_heart", activity.maxHeart)
            intent.putExtra("sets", activity.sets)
            intent.putExtra("reps", activity.reps)
            intent.putExtra("weight", activity.weight)
            intent.putExtra("intensity", activity.intensity)
            intent.putExtra("mood_before", activity.moodBefore)
            intent.putExtra("mood_after", activity.moodAfter)
            intent.putExtra("location", activity.location)
            intent.putExtra("notes", activity.notes)
            intent.putExtra("date", activity.date)
            intent.putExtra("start", activity.start)
            intent.putExtra("end", activity.end)

            context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            activity.id?.let { id -> deleteActivity(id) }
        }
    }

    fun updateData(newList: List<ActivityModel>) {
        originalList.clear()
        originalList.addAll(newList)

        filteredList.clear()
        filteredList.addAll(newList)

        notifyDataSetChanged()
    }

    fun filterList(query: String) {
        val search = query.trim().lowercase()

        filteredList.clear()

        if (search.isEmpty()) {
            filteredList.addAll(originalList)
        } else {
            filteredList.addAll(
                originalList.filter {
                    (it.type?.lowercase()?.contains(search) ?: false) ||
                            (it.date?.lowercase()?.contains(search) ?: false) ||
                            (it.name?.lowercase()?.contains(search) ?: false)
                }
            )
        }

        notifyDataSetChanged()
    }

    fun sortList(option: String) {
        when (option) {
            "Date" -> filteredList.sortByDescending { it.date }
            "Duration" -> filteredList.sortByDescending { it.duration?.toIntOrNull() ?: 0 }
            "Type" -> filteredList.sortBy { it.type?.lowercase() ?: "" }
        }
        notifyDataSetChanged()
    }

    private fun deleteActivity(id: String) {
        // Use the IP address consistent with the rest of the app
        val url = "http://192.168.0.102/fitlife/delete_activity.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            {
                Toast.makeText(context, "Activity deleted", Toast.LENGTH_SHORT).show()
                if (context is ActivitiesListActivity) {
                    context.recreate()
                }
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["activity_id"] = id
                return params
            }
        }

        Volley.newRequestQueue(context).add(request)
    }
}