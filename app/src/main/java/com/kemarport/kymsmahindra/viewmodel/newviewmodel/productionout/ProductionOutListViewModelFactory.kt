package com.kemarport.kymsmahindra.viewmodel.newviewmodel.productionout

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kymsmahindra.repository.KYMSRepository


class ProductionOutListViewModelFactory(
    private val application: Application,
    private val kymsRepository: KYMSRepository
) : ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductionOutListViewModel(application, kymsRepository) as T
    }
}