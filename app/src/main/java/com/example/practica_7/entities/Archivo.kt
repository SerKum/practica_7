package com.example.practica_7.entities

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class Archivo(
    var id: String = "",
    var title: String = "",
    var fileUri: String = ""
)
