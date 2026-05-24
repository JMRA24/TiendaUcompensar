package com.project.store.di

import com.project.store.data.repository.FirebaseRepository

object AppModule {
    fun provideFirebaseRepository(): FirebaseRepository = FirebaseRepository.getInstance()
}
