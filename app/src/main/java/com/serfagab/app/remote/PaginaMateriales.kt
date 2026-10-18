package com.serfagab.app.remote

import com.serfagab.app.model.Material

data class PaginaMateriales(
    val content: List<Material>,
    val last: Boolean
)
