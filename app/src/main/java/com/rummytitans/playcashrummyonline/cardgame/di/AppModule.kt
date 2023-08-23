package com.rummytitans.playcashrummyonline.cardgame.di

import com.rummytitans.playcashrummyonline.cardgame.MainApplication
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import android.content.ClipboardManager
import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {



    @Singleton
    @Provides
    fun providesPreferenceStorage(@ApplicationContext context: Context) = SharedPreferenceStorage(context)

    @Provides
    fun providesClipboardManager(@ApplicationContext context: Context) =
        context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    @Singleton
    @Provides
    fun providesConnectionDetector(@ApplicationContext context: Context) = ConnectionDetector(context)

    @Singleton
    @Provides
    fun providesAnalyticsHelper(@ApplicationContext context: Context, gson: Gson) = AnalyticsHelper(context, gson)
}
