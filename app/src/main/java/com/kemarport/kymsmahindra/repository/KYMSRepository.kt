package com.kemarport.kymsmahindra.repository

import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.model.changepassword.ChangePasswordRequest
import com.kemarport.kymsmahindra.model.login.LoginRequest
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetVehicleStatusRequest
import com.kemarport.kymsmahindra.model.newapi.parkrepark.PostVehicleMovementRequest
import com.kemarport.kymsmahindra.model.newapi.setgeofence.PostGeofenceCoordinatesRequest
import com.kemarport.mahindrakiosk.api.RetrofitInstance
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Query

class KYMSRepository {
    suspend fun getAppDetails(
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).getAppDetails()

    suspend fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) = RetrofitInstance.api(baseUrl).login(loginRequest)

//new apis

    //park/repark page
    suspend fun parkReparkVehicle(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
        postVehicleMovementRequest: PostVehicleMovementRequest
    ) = RetrofitInstance.api(baseUrl).parkReparkVehicle(bearerToken,postVehicleMovementRequest)

    suspend fun getVehicleStatus(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
        getVehicleStatusRequest: GetVehicleStatusRequest
    ) = RetrofitInstance.api(baseUrl).getVehicleStatus(bearerToken,getVehicleStatusRequest)

    suspend fun getAllInternalYardLocations(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).getAllInternalYardLocations(bearerToken)


    //setGeofence page
    suspend fun getAllActiveDealers(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).getAllActiveDealers(bearerToken)

    suspend fun getAllParentLocations(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
        dealerId:Int?,
    ) = RetrofitInstance.api(baseUrl).getAllParentLocations(bearerToken,dealerId)

    suspend fun getAllChildLocations(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
        parentLocationCode:Int?,
    ) = RetrofitInstance.api(baseUrl).getAllChildLocations(bearerToken,parentLocationCode)

    suspend fun postGeofenceCoordinates(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
        @Body
        postGeofenceCoordinatesRequest: PostGeofenceCoordinatesRequest
    ) = RetrofitInstance.api(baseUrl).postGeofenceCoordinates(bearerToken, postGeofenceCoordinatesRequest)


    //Prd Out List
    suspend fun getVehicleStatus(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).getVehicleStatus(bearerToken)

    //search vehicle List
    suspend fun getSearchVehicleList(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
        @Query("model")  model: String?,
        @Query("color")  color: String?
    ) = RetrofitInstance.api(baseUrl).getSearchVehicleList(bearerToken,model,color)

    suspend fun getVehicleColors(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).getVehicleColors(bearerToken)

    suspend fun getVehicleModels(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).getVehicleModels(bearerToken)

    //my transaction vehicle List
    suspend fun getMyTransactions(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        baseUrl: String,
        @Query("userName")  userName: String?
    ) = RetrofitInstance.api(baseUrl).getMyTransactions(bearerToken,userName)

    suspend fun changePassword(
        token:String,
        baseUrl: String,
        changePasswordRequest: ChangePasswordRequest
    ) = RetrofitInstance.api(baseUrl).changePassword(token,changePasswordRequest)
}