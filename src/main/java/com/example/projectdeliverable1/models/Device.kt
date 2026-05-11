package com.example.projectdeliverable1.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    val id: Int,
    val name: String,
    val location: String,
    val status: String,
    val type: String,
    val lastUpdated: String,
    val description: String,
    val roomId: Int = 0,
    val firestoreId: String = ""
) : Parcelable
