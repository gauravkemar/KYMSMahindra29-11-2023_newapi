package com.kemarport.kymsmahindra.viewmodel.newviewmodel.parkrepark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.Utils
import com.kemarport.kymsmahindra.model.GeneralResponse
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetAllYardLocationsResponseItem
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetVehicleStatusRequest
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetVehicleStatusResponse
import com.kemarport.kymsmahindra.model.newapi.parkrepark.PostVehicleMovementRequest
import com.kemarport.kymsmahindra.repository.KYMSRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class ParkReparkViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {

    /////////////////////////////////////////////////////////
    val getAllInternalYardMutable: MutableLiveData<Resource<ArrayList<GetAllYardLocationsResponseItem>>> =
        MutableLiveData()

    fun getAllInternalYardLocations(
        token: String,
        baseUrl: String
    ) {
        viewModelScope.launch {
            safeAPICallgetAllInternalYardLocations(token,baseUrl)
        }
    }

    private suspend fun safeAPICallgetAllInternalYardLocations(
        token: String,
        baseUrl: String
    ) {
        getAllInternalYardMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getAllInternalYardLocations(token, baseUrl)
                getAllInternalYardMutable.postValue(handleGetAllInternalYardResponse(response))
            } else {
                getAllInternalYardMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getAllInternalYardMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getAllInternalYardMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetAllInternalYardResponse(response: Response<ArrayList<GetAllYardLocationsResponseItem>>): Resource<ArrayList<GetAllYardLocationsResponseItem>>? {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    ///parkRepark vehicle
    val parkReparkVehicleMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun parkReparkVehicle(
        token: String,
        baseUrl: String,
        postVehicleMovementRequest: PostVehicleMovementRequest
    ) {
        viewModelScope.launch {
            safeAPICallParkReparkVehicle(token,baseUrl, postVehicleMovementRequest)
        }
    }
    private suspend fun safeAPICallParkReparkVehicle(token: String, baseUrl: String, postVehicleMovementRequest: PostVehicleMovementRequest) {
        parkReparkVehicleMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.parkReparkVehicle(token,baseUrl, postVehicleMovementRequest)
                parkReparkVehicleMutableLiveData.postValue(handleParkReparkVehicleResponse(response))
            } else {
                parkReparkVehicleMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    parkReparkVehicleMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> parkReparkVehicleMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleParkReparkVehicleResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    //get Vehicle status
    val getVehicleStatusMutableLiveData: MutableLiveData<Resource<GetVehicleStatusResponse>> = MutableLiveData()

    fun getVehicleStatus(
        token: String,
        baseUrl: String,
        getVehicleStatusRequest: GetVehicleStatusRequest
    ) {
        viewModelScope.launch {
            safeAPICallGetVehicleStatus(token,baseUrl, getVehicleStatusRequest)
        }
    }
    private suspend fun safeAPICallGetVehicleStatus(token: String, baseUrl: String, getVehicleStatusRequest: GetVehicleStatusRequest) {
        getVehicleStatusMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getVehicleStatus(token,baseUrl, getVehicleStatusRequest)
                getVehicleStatusMutableLiveData.postValue(handleGetVehicleStatusResponse(response))
            } else {
                getVehicleStatusMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getVehicleStatusMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> getVehicleStatusMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleGetVehicleStatusResponse(response: Response<GetVehicleStatusResponse>): Resource<GetVehicleStatusResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

}