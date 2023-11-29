package com.kemarport.kymsmahindra.viewmodel.newviewmodel.setgeofence

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.Utils
import com.kemarport.kymsmahindra.model.newapi.setgeofence.GetAllDealersResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.GetChildLocationsResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.GetParentLocationsResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.PostGeofenceCoordinatesRequest
import com.kemarport.kymsmahindra.model.newapi.setgeofence.PostGeofenceCoordinatesResponse
import com.kemarport.kymsmahindra.repository.KYMSRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SetGeofenceViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
///get all dealers
    val getAllActiveDealersMutable: MutableLiveData<Resource<ArrayList<GetAllDealersResponse>>> =
        MutableLiveData()

    fun getAllActiveDealers(
        token: String,
        baseUrl: String
    ) {
        viewModelScope.launch {
            safeAPICallgetAllActiveDealersList(token,baseUrl)
        }
    }

    private suspend fun safeAPICallgetAllActiveDealersList(
        token: String,
        baseUrl: String
    ) {
        getAllActiveDealersMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getAllActiveDealers(token, baseUrl)
                getAllActiveDealersMutable.postValue(handleGetAllActiveDealersResponse(response))
            } else {
                getAllActiveDealersMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getAllActiveDealersMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getAllActiveDealersMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetAllActiveDealersResponse(response: Response<ArrayList<GetAllDealersResponse>>): Resource<ArrayList<GetAllDealersResponse>>? {
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

    ///get parent locations
    val getAllParentLocationsMutable: MutableLiveData<Resource<ArrayList<GetParentLocationsResponse>>> =
        MutableLiveData()

    fun getAllParentLocations(
        token: String,
        baseUrl: String,
        dealerId:Int?
    ) {
        viewModelScope.launch {
            safeAPICallGetAllParentLocations(token,baseUrl,dealerId)
        }
    }

    private suspend fun safeAPICallGetAllParentLocations(
        token: String,
        baseUrl: String,
        dealerId:Int?
    ) {
        getAllParentLocationsMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getAllParentLocations(token, baseUrl,dealerId)
                getAllParentLocationsMutable.postValue(handleGetAllParentLocationsResponse(response))
            } else {
                getAllParentLocationsMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getAllParentLocationsMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getAllParentLocationsMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetAllParentLocationsResponse(response: Response<ArrayList<GetParentLocationsResponse>>): Resource<ArrayList<GetParentLocationsResponse>>? {
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

    ///get child locations
    val getAllChildLocationsMutable: MutableLiveData<Resource<ArrayList<GetChildLocationsResponse>>> =
        MutableLiveData()

    fun getAllChildLocations(
        token: String,
        baseUrl: String,
        parentLocationCode:Int?
    ) {
        viewModelScope.launch {
            safeAPICallGetAllChildLocations(token,baseUrl,parentLocationCode)
        }
    }

    private suspend fun safeAPICallGetAllChildLocations(
        token: String,
        baseUrl: String,
        parentLocationCode:Int?
    ) {
        getAllChildLocationsMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getAllChildLocations(token, baseUrl,parentLocationCode)
                getAllChildLocationsMutable.postValue(handleGetAllChildLocationsResponse(response))
            } else {
                getAllChildLocationsMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getAllChildLocationsMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getAllChildLocationsMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetAllChildLocationsResponse(response: Response<ArrayList<GetChildLocationsResponse>>): Resource<ArrayList<GetChildLocationsResponse>>? {
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


    ///post child locations
    val postGeofenceCoordinatesMutable: MutableLiveData<Resource<PostGeofenceCoordinatesResponse>> =
        MutableLiveData()

    fun postGeofenceCoordinates(
        token: String,
        baseUrl: String,
        postGeofenceCoordinatesRequest: PostGeofenceCoordinatesRequest
    ) {
        viewModelScope.launch {
            safeAPICallPostGeofenceCoordinates(token,baseUrl,postGeofenceCoordinatesRequest)
        }
    }

    private suspend fun safeAPICallPostGeofenceCoordinates(
        token: String,
        baseUrl: String,
        postGeofenceCoordinatesRequest: PostGeofenceCoordinatesRequest
    ) {
        postGeofenceCoordinatesMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.postGeofenceCoordinates(token, baseUrl,postGeofenceCoordinatesRequest)
                postGeofenceCoordinatesMutable.postValue(handlePostGeofenceCoordinatesResponse(response))
            } else {
                postGeofenceCoordinatesMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    postGeofenceCoordinatesMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> postGeofenceCoordinatesMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handlePostGeofenceCoordinatesResponse(response: Response<PostGeofenceCoordinatesResponse>):
            Resource<PostGeofenceCoordinatesResponse>? {
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

}