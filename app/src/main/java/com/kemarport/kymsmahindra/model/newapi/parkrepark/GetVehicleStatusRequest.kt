package com.kemarport.kymsmahindra.model.newapi.parkrepark

data class GetVehicleStatusRequest(
    val VIN: String,
    val RFIDTag: String,

)