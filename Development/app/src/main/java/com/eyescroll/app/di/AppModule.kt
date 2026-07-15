package com.eyescroll.app.di

import com.eyescroll.app.data.SettingsRepository
import com.eyescroll.app.domain.gesture.GestureStateMachine
import com.eyescroll.app.presentation.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SettingsRepository(androidContext()) }
    factory { GestureStateMachine() }
    viewModel { HomeViewModel(get()) }
}
