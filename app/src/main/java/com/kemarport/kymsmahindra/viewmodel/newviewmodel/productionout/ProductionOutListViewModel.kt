package com.kemarport.kymsmahindra.viewmodel.newviewmodel.productionout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.Utils
import com.kemarport.kymsmahindra.model.newapi.prdout.PrdOutListResponse
import com.kemarport.kymsmahindra.repository.KYMSRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.sql.Array

class ProductionOutListViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {

    val getVehicleStatusMutable: MutableLiveData<Resource<ArrayList<PrdOutListResponse>>> =
        MutableLiveData()

    fun getVehicleStatus(
        token: String,
        baseUrl: String
    ) {
        viewModelScope.launch {
            safeAPICallGetVehicleStatus(token,baseUrl)
        }
    }

    private suspend fun safeAPICallGetVehicleStatus(
        token: String,
        baseUrl: String
    ) {
        getVehicleStatusMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getVehicleStatus(token, baseUrl)
                getVehicleStatusMutable.postValue(handleGetVehicleStatusResponse(response))
            } else {
                getVehicleStatusMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getVehicleStatusMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getVehicleStatusMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetVehicleStatusResponse(response: Response<ArrayList<PrdOutListResponse>>): Resource<ArrayList<PrdOutListResponse>>? {
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