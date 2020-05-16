package com.claudemate.meldcxbrowser.viewModel

import android.app.Application
import androidx.lifecycle.*
import com.claudemate.meldcxbrowser.room.History
import com.claudemate.meldcxbrowser.room.HistoryRepository
import com.claudemate.meldcxbrowser.room.RoomDB
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    // The ViewModel maintains a reference to the repository to get data.
    private val repository: HistoryRepository

    // LiveData gives us updated list of history
    val getAll: LiveData<List<History>>

    init {
        // Gets reference to HistoryDao from RoomDB to construct
        // the correct HistoryRepository.
        val historyDao = RoomDB.getDatabase(application).historyDao()
        repository = HistoryRepository(historyDao)
        getAll = repository.getAll
    }

    /**
     * The implementation of insert() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called
     * viewModelScope which we can use here.
     */
    fun insert(history: History) = viewModelScope.launch {
        repository.insert(history)
    }

    /**
     * Gets the recent history from history_table
     */
    suspend fun getRecent() : History? {
        return repository.getRecent()
    }

    /**
     * Returns the search result
     */
    suspend fun find(text: String) : List<History> {
        return repository.find(text)
    }

    /**
     * Deletes an item in history_table
     */
    fun delete(id: Int) = viewModelScope.launch {
        repository.delete(id)
    }

    /**
     * Deletes all the items in history_table
     */
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}