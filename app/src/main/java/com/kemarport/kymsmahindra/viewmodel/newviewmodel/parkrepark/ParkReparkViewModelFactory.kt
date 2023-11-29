package com.kemarport.kymsmahindra.viewmodel.newviewmodel.parkrepark

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.searchvehicle.SearchVehicleViewModel

class ParkReparkViewModelFactory(
    private val application: Application,
    private val kymsRepository: KYMSRepository
) : ViewModelProvider.Factory   {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ParkReparkViewModel(application, kymsRepository) as T
    }
}