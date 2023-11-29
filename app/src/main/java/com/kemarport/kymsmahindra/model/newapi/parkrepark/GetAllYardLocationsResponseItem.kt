package com.kemarport.kymsmahindra.model.newapi.parkrepark

data class GetAllYardLocationsResponseItem(
    val coordinates: String,
    val createdBy: String,
    val createdDate: String,
    val dealerId: Int,
    val dealerName: String,
    val deviceLocationMappings: List<Any>,
    val displayName: String,
    val isActive: Boolean,
    val locationCode: String,
    val locationId: Int,
    val locationName: String,
    val locationType: String,
    val modifiedBy: String,
    val modifiedDate: String,
    val parentLocationCode: Any,
    val parkingCapacity: Int,
    val remarks: Any,
    val runningLoad: Int
)