package com.kemarport.kymsmahindra.viewmodel.newviewmodel.searchvehicle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.Utils
import com.kemarport.kymsmahindra.model.newapi.searchvehicles.GetSearchVehiclesListResponse
import com.kemarport.kymsmahindra.model.newapi.searchvehicles.GetVehicleColorsModelResponse
import com.kemarport.kymsmahindra.repository.KYMSRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SearchVehicleViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application)
{

   // val searchVehicleListMutableLiveData: MutableLiveData<Resource<MutableList<VehicleListResponseItem>?>> = MutableLiveData()
   // val generateTokenMutableLiveData: MutableLiveData<Resource<GenerateTokenResponse>> = MutableLiveData()

  /*  init {
        generateToken()
    }
*/
    /*fun searchVehicleList(
        token: String,
        baseUrl: String

    ) {
        viewModelScope.launch {
            safeAPICallSearchVehicleList(token,baseUrl )
        }
    }
    private suspend fun safeAPICallSearchVehicleList(  token: String,baseUrl: String ) {
        searchVehicleListMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.searchVehicleList(token,baseUrl)
                searchVehicleListMutableLiveData.postValue(Resource.Success(response.body())!!)
            } else {
                searchVehicleListMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    searchVehicleListMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> searchVehicleListMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }*/

/*    private suspend fun generateToken(baseUrl: String ,vehicleListRequest: SearchVehiclePostRequest) {
        generateTokenMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.generateToken(baseUrl, vehicleListRequest)
                generateTokenMutableLiveData.postValue(handleGenerateTokenResponse(response))
            } else {
                generateTokenMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    generateTokenMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> generateTokenMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }*/
    /*private fun handleSearchVehicleListResponse(response: Response<MutableList<VehicleListResponseItem>>): Resource<MutableList<VehicleListResponseItem>> {
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

    private fun handleGenerateTokenResponse(response: Response<GenerateTokenResponse>): Resource<GenerateTokenResponse> {
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
    }*/

 /*   fun generateToken() {
        viewModelScope.launch {
            generateToken(
                baseUrl = Constants.BASE_URL, vehicleListRequest = SearchVehiclePostRequest(
                    UserName = "Bhola",
                    Password = "Bhola@123"
                )
            )
        }
    }*/


    val getSearchVehicleListMutable: MutableLiveData<Resource<ArrayList<GetSearchVehiclesListResponse>>> =
        MutableLiveData()

    fun getSearchVehicleList(
        token: String,
        baseUrl: String,
        model: String?,
        color: String?
    ) {
        viewModelScope.launch {
            safeAPICallGetSearchVehicleList(token,baseUrl,model,color)
        }
    }

    private suspend fun safeAPICallGetSearchVehicleList(
        token: String,
        baseUrl: String,
        model: String?,
        color: String?
    ) {
        getSearchVehicleListMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getSearchVehicleList(token, baseUrl,model,color)
                getSearchVehicleListMutable.postValue(handleGetSearchVehicleListResponse(response))
            } else {
                getSearchVehicleListMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getSearchVehicleListMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getSearchVehicleListMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetSearchVehicleListResponse(response: Response<ArrayList<GetSearchVehiclesListResponse>>): Resource<ArrayList<GetSearchVehiclesListResponse>>? {
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

    ////get color
    val getVehicleColorsMutable: MutableLiveData<Resource<ArrayList<GetVehicleColorsModelResponse>>> =
        MutableLiveData()

    fun getVehicleColors(
        token: String,
        baseUrl: String
    ) {
        viewModelScope.launch {
            safeAPICallGetVehicleColors(token,baseUrl)
        }
    }

    private suspend fun safeAPICallGetVehicleColors(
        token: String,
        baseUrl: String
    ) {
        getVehicleColorsMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getVehicleColors(token, baseUrl)
                getVehicleColorsMutable.postValue(handleGetVehicleColorsResponse(response))
            } else {
                getVehicleColorsMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getVehicleColorsMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getVehicleColorsMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetVehicleColorsResponse(response: Response<ArrayList<GetVehicleColorsModelResponse>>): Resource<ArrayList<GetVehicleColorsModelResponse>>? {
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


    ////get model code
    val getVehicleModelsMutable: MutableLiveData<Resource<ArrayList<GetVehicleColorsModelResponse>>> =
        MutableLiveData()

    fun getVehicleModels(
        token: String,
        baseUrl: String
    ) {
        viewModelScope.launch {
            safeAPICallGetVehicleModels(token,baseUrl)
        }
    }

    private suspend fun safeAPICallGetVehicleModels(
        token: String,
        baseUrl: String
    ) {
        getVehicleModelsMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getVehicleModels(token, baseUrl)
                getVehicleModelsMutable.postValue(handleGetVehicleModelsResponse(response))
            } else {
                getVehicleModelsMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getVehicleModelsMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getVehicleModelsMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetVehicleModelsResponse(response: Response<ArrayList<GetVehicleColorsModelResponse>>): Resource<ArrayList<GetVehicleColorsModelResponse>>? {
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

    ////my transaction list
    val getMyTransactionsMutable: MutableLiveData<Resource<ArrayList<GetSearchVehiclesListResponse>>> =
        MutableLiveData()

    fun getMyTransactions(
        token: String,
        baseUrl: String,
        userName:String
    ) {
        viewModelScope.launch {
            safeAPICallGetMyTransactions(token,baseUrl,userName)
        }
    }

    private suspend fun safeAPICallGetMyTransactions(
        token: String,
        baseUrl: String,
        userName: String
    ) {
        getMyTransactionsMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getMyTransactions(token, baseUrl,userName)
                getMyTransactionsMutable.postValue(handleGetMyTransactionsResponse(response))
            } else {
                getMyTransactionsMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getMyTransactionsMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getMyTransactionsMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetMyTransactionsResponse(response: Response<ArrayList<GetSearchVehiclesListResponse>>): Resource<ArrayList<GetSearchVehiclesListResponse>>? {
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