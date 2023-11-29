package com.kemarport.kymsmahindra.viewmodel.newviewmodel.setgeofence

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.login.LoginViewModel

class SetGeofenceViewModelFactory(
    private val application: Application,
    private val kymsRepository: KYMSRepository
) : ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return  SetGeofenceViewModel(application, kymsRepository) as T
    }
}