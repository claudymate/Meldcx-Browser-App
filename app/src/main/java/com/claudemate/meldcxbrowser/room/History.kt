package com.claudemate.meldcxbrowser.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
class History(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "date_time")
    val dateTime: String,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "icon")
    val icon: ByteArray

)