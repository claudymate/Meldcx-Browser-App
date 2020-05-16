package com.claudemate.meldcxbrowser.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.claudemate.meldcxbrowser.R
import com.claudemate.meldcxbrowser.room.History


class HistoryAdapter internal constructor(
    context: Context,
    private val listener: OnHistoryActionItemListener
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var list = emptyList<History>()

    // Holds the views
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textUrl = itemView.findViewById<TextView>(R.id.textUrl)
        val textDateTime = itemView.findViewById<TextView>(R.id.textDateTime)
        val imgIcon = itemView.findViewById<ImageView>(R.id.imgIcon)
        val imgRemove = itemView.findViewById<ImageView>(R.id.imgRemove)
        val root = itemView
    }

    // This overridden function is used to create an instance for View Holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = inflater.inflate(R.layout.row_history, parent, false)
        return HistoryViewHolder(itemView)
    }

    // This overridden function is used ti set the values on the UI
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val current = list[position]
        holder.textUrl.text = current.url
        holder.textDateTime.text = current.dateTime
        val bitmap = BitmapFactory.decodeByteArray(current.icon, 0, current.icon.size)
        holder.imgIcon.setImageBitmap(bitmap)
        holder.imgRemove.setOnClickListener {
            listener.onDelete(current)
        }
        holder.root.setOnClickListener {
            listener.onItemSelected(current)
        }
    }

    /**
     * Update the list
     */
    internal fun setHistoryList(list: List<History>) {
        this.list = list
        notifyDataSetChanged()
    }

    internal interface OnHistoryActionItemListener {
        /**
         * This callback is triggered when an item has been removed
         */
        fun onDelete(history: History)

        /**
         * This callback is triggered when the user selected an item
         */
        fun onItemSelected(history: History)
    }

    /**
     * Get the count of items in the list
     */
    override fun getItemCount() = list.size
}