package com.kemarport.kymsmahindra.model.newapi.setgeofence

data class PostGeofenceCoordinatesRequest(
    val ChildLocationId: Int,
    val Coordinates: String,
    val DealerId: Int,
    val ParentLocationId: Int
)