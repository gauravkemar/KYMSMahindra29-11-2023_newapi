package com.kemarport.kymsmahindra.model.newapi.searchvehicles

import android.os.Parcel
import android.os.Parcelable

data class GetSearchVehiclesListResponse (
    val actionTime: String,
    val currentLocation: String,
    val coordinates: String,
    val colorDescription: String,
    val country: String,
    val modelCode: String,
    val vehicleStatus: String,
    val vin: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(actionTime)
        parcel.writeString(currentLocation)
        parcel.writeString(coordinates)
        parcel.writeString(colorDescription)
        parcel.writeString(country)
        parcel.writeString(modelCode)
        parcel.writeString(vehicleStatus)
        parcel.writeString(vin)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GetSearchVehiclesListResponse> {
        override fun createFromParcel(parcel: Parcel): GetSearchVehiclesListResponse {
            return GetSearchVehiclesListResponse(parcel)
        }

        override fun newArray(size: Int): Array<GetSearchVehiclesListResponse?> {
            return arrayOfNulls(size)
        }
    }
}