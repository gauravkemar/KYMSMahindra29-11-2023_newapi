package com.kemarport.kymsmahindra.model.newapi.parkrepark

data class                                                                                                PostVehicleMovementRequest(
    val ActionedBy: String,
    val Coordinates: String,
    val MovementType: String,
    val VIN: String,
    val CurrentLocationId:Int
)