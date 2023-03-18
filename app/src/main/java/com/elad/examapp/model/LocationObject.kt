package com.elad.examapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_tbl")
data class LocationObject(
    @PrimaryKey
    @ColumnInfo
    val id: String,

    @ColumnInfo
    val lat: Double,

    @ColumnInfo
    val lon: Double,

    @ColumnInfo
    val alt: Double
)
