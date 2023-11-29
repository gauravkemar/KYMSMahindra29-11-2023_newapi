package com.kemarport.mahindrakiosk.api

import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.model.GeneralResponse
import com.kemarport.kymsmahindra.model.appDetails.GetAppDetailsResponse
import com.kemarport.kymsmahindra.model.login.LoginRequest
import com.kemarport.kymsmahindra.model.login.LoginResponse
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetAllYardLocationsResponseItem
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetVehicleStatusRequest
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetVehicleStatusResponse
import com.kemarport.kymsmahindra.model.newapi.parkrepark.PostVehicleMovementRequest
import com.kemarport.kymsmahindra.model.newapi.prdout.PrdOutListResponse
import com.kemarport.kymsmahindra.model.newapi.searchvehicles.GetSearchVehiclesListResponse
import com.kemarport.kymsmahindra.model.newapi.searchvehicles.GetVehicleColorsModelResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.GetAllDealersResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.GetChildLocationsResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.GetParentLocationsResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.PostGeofenceCoordinatesRequest
import com.kemarport.kymsmahindra.model.newapi.setgeofence.PostGeofenceCoordinatesResponse

import retrofit2.Response
import retrofit2.http.*


interface KYMSAPI {

    @GET(Constants.GET_APP_DETAILS)
    suspend fun getAppDetails(
    ): Response<GetAppDetailsResponse>

    @POST(Constants.LOGIN_URL)
    suspend fun login(
        @Body
        loginRequest: LoginRequest
    ): Response<LoginResponse>


    //new api KYMS Mahindra

    //park/repark page
    @POST(Constants.VEHICLE_PARK_REPARK)
    suspend fun parkReparkVehicle(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Body
        postVehicleMovementRequest: PostVehicleMovementRequest
    ): Response<GeneralResponse>

    @POST(Constants.GET_VEHICLE_STATUS)
    suspend fun getVehicleStatus(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Body
        getVehicleStatusRequest: GetVehicleStatusRequest
    ): Response<GetVehicleStatusResponse>

    @GET(Constants.GET_ALL_INTERNAL_LOCATION)
    suspend fun getAllInternalYardLocations(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String
    ): Response<ArrayList<GetAllYardLocationsResponseItem>>

    //set geofence api
    @GET(Constants.GET_ACTIVE_DEALERS)
    suspend fun getAllActiveDealers(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String
    ): Response<ArrayList<GetAllDealersResponse>>

    @GET(Constants.GET_PARENT_LOCATIONS_NEW)
    suspend fun getAllParentLocations(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String,
        @Query("dealerId") dealerId: Int?
    ): Response<ArrayList<GetParentLocationsResponse>>

    @GET(Constants.GET_CHILD_LOCATIONS)
    suspend fun getAllChildLocations(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String,
        @Query("parentLocationCode") parentLocationCode: Int?
    ): Response<ArrayList<GetChildLocationsResponse>>

    @POST(Constants.POST_GEOFENCE_COORDINATES)
    suspend fun postGeofenceCoordinates(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Body
        postGeofenceCoordinatesRequest: PostGeofenceCoordinatesRequest
    ): Response<PostGeofenceCoordinatesResponse>

    //prd out list

    @GET(Constants.GET_PRD_OUT_LIST)
    suspend fun getVehicleStatus(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String,
    ): Response<ArrayList<PrdOutListResponse>>

    // search vehicle

    @GET(Constants.GET_SEARCH_VEHICLES_LIST)
    suspend fun getSearchVehicleList(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String,
        @Query("model") model: String?,
        @Query("color") color: String?
    ): Response<ArrayList<GetSearchVehiclesListResponse>>

    @GET(Constants.GET_COLOR_DESCRIPTION)
    suspend fun getVehicleColors(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String,
    ): Response<ArrayList<GetVehicleColorsModelResponse>>

    @GET(Constants.GET_MODEL_CODE)
    suspend fun getVehicleModels(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String,
    ): Response<ArrayList<GetVehicleColorsModelResponse>>

    // my transaction vehicle

    @GET(Constants.GET_MY_TRANSACTION_VEHICLE_LIST)
    suspend fun getMyTransactions(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) token: String,
        @Query("userName") userName: String?
    ): Response<ArrayList<GetSearchVehiclesListResponse>>


}