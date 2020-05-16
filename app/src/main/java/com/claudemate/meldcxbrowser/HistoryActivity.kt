package com.claudemate.meldcxbrowser

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.claudemate.meldcxbrowser.room.History
import com.claudemate.meldcxbrowser.ui.HistoryAdapter
import com.claudemate.meldcxbrowser.viewModel.HistoryViewModel
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HistoryActivity : AppCompatActivity() {

    public lateinit var viewModel: HistoryViewModel
    public lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Set up back button menu
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true);

        // Set up view model
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        // Start observing the data on history_table
        viewModel.getAll.observe(this, Observer {
            it.let {
                if (editSearch.text.length > 0) {
                    // If the search field is not empty,
                    // update the list based on the search result
                    findItem(editSearch.text.toString())

                } else {
                    // Else, fetch all the from history_table
                    adapter.setHistoryList(it)
                }
            }
        })

        // Set up adapter
        adapter = HistoryAdapter(this, object : HistoryAdapter.OnHistoryActionItemListener {

            // This will be triggered when user deleted an item in history
            override fun onDelete(history: History) {
                // Update the database and list
                viewModel.delete(history.id)
            }

            // This will be triggered when user selected an item from history
            override fun onItemSelected(history: History) {

                // Go to the main activity and load the selected URL
                editSearch.setText("")
                val intent = Intent(this@HistoryActivity, MainActivity::class.java)
                intent.putExtra(MainActivity.SELECTED_URL, history.url)
                startActivity(intent)
                finish()
            }
        })

        // Set up recycle view
        rvHistory.adapter = adapter
        rvHistory.layoutManager = LinearLayoutManager(this)

        // This callback handles the text changed event that is used for searching
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Update list based on result
                findItem(text.toString())
            }
        })
    }

    /**
     * This function is used update the list based on the search result
     */
    public fun findItem(text: String) {

        // Perform coroutine on Main thread to fetch the search result
        viewModel.viewModelScope.launch(Dispatchers.Main) {
            val result = viewModel.find(text)

            // Update the adapter
            adapter.setHistoryList(result)
        }
    }

    /**
     * This overridden function is used to initialize the menu UI
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        return true
    }

    /**
     * This overridden function is used to handle the click events on the menus
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_clear -> {
                showConfirmation(adapter.itemCount > 0)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Call this function to show the Alert Dialog
     */
    public fun showConfirmation(isNotEmpty: Boolean) {
        val dialogBuilder = AlertDialog.Builder(this)

        var title = getString(R.string.confirmation)
        var message = getString(R.string.confirmation_clear_history)

        if (isNotEmpty) {
            // Positive button text and action
            dialogBuilder.setPositiveButton(getString(R.string.clear_history), DialogInterface.OnClickListener { dialog, _ ->
                editSearch.setText("")
                viewModel.deleteAll()
                dialog.dismiss()
            })

            // Negative button text and action
            dialogBuilder.setNegativeButton(getString(R.string.cancel), DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })

        } else {
            title = getString(R.string.information)
            message = getString(R.string.information_clear_history)
            dialogBuilder.setPositiveButton(getString(R.string.okay), DialogInterface.OnClickListener { dialog, _ ->
                viewModel.deleteAll()
                dialog.dismiss()
            })
        }

        // Set message of alert dialog
        dialogBuilder.setMessage(message)

        // If the dialog is cancelable
        dialogBuilder.setCancelable(false)

        // Create dialog box
        val alert = dialogBuilder.create()

        // Set title for alert dialog box
        alert.setTitle(title)

        // Show alert dialog
        alert.show()
    }
}
