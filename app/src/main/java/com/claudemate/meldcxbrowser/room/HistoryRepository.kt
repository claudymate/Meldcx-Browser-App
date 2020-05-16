package com.claudemate.meldcxbrowser.room

import androidx.lifecycle.LiveData

class HistoryRepository(private val historyDao: HistoryDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val getAll: LiveData<List<History>> = historyDao.getAll()

    suspend fun getRecent(): History? {
        return historyDao.getRecent()
    }

    suspend fun find(text: String): List<History> {
        return historyDao.find("%$text%")
    }

    suspend fun insert(history: History) {
        historyDao.insert(history)
    }

    suspend fun delete(id: Int) {
        historyDao.delete(id)
    }

    suspend fun deleteAll() {
        historyDao.deleteAll()
    }
}