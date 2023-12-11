package com.rummytitans.sdk.cardgame.di

import android.content.ClipboardManager
import android.content.Context
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.di.anotation.RummySdk
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


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
