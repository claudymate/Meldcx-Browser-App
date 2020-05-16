package com.claudemate.meldcxbrowser.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {

    @Query("SELECT * from history_table ORDER BY date_time DESC")
    fun getAll(): LiveData<List<History>>

    @Query("SELECT * from history_table ORDER BY date_time DESC LIMIT 1")
    fun getRecent(): History?

    @Query("SELECT * from history_table WHERE url LIKE :text ORDER BY date_time DESC")
    suspend fun find(text: String): List<History>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: History)

    @Query("DELETE FROM history_table WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM history_table")
    suspend fun deleteAll()
}