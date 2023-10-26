package com.rummytitans.playcashrummyonline.cardgame.di

import android.os.Build
import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.MainApplication
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import android.text.TextUtils
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.RummyTitanSDK
import com.rummytitans.playcashrummyonline.cardgame.di.anotation.RummySdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Singleton
    @Provides
    fun getApiInterface(@RummySdk retrofit: Retrofit) = retrofit.create(APIInterface::class.java)

    @Singleton
    @Provides
   // @RummySdk
    fun provideGson() = Gson()

    @Singleton
    @Provides
    @RummySdk
    fun getRetrofit(@RummySdk okHttpClient: OkHttpClient):Retrofit {
        val baseurl = RummyTitanSDK.getOption().baseUrl
        val baseApiUrl= if (TextUtils.isEmpty(baseurl))
          MyConstants.APP_CURRENT_URL
        else
          baseurl

        return Retrofit.Builder()
            .baseUrl(baseApiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient).build()
    }

    @Singleton
    @Provides
    @RummySdk
    fun getOkHttpClient(@RummySdk interceptor: Interceptor) =
        OkHttpClient.Builder().addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).build()

    @Singleton
    @Provides
    @RummySdk
    fun getInterceptor(pref: SharedPreferenceStorageRummy): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->
            val request = chain.request()
            val httpUrl = request.url
            val url = httpUrl.newBuilder().build()
            val builder = request.newBuilder().url(url)
            builder.addHeader("AppVersion", BuildConfig.VERSION_CODE.toString())
            builder.addHeader("AppType", "${RummyTitanSDK.getOption().currentAppType}") //uses in BaseViewModel and Analytic helper fireevent()
            builder.addHeader("GameType", "1")
            builder.addHeader("DeviceName", Build.MODEL)
            builder.addHeader("DeviceOS", "Android")
            builder.addHeader("IsPlayStore",BuildConfig.isPlayStoreApk.toString())
//            pref.toUserDetail(gson)?.let {
//                builder.addHeader("UserId", it.UserId.toString())
//                builder.addHeader("AuthExpire", it.AuthExpire)
//                builder.addHeader("ExpireToken", it.ExpireToken)
//            }
            val requestBuilder = builder.build()
            return@Interceptor chain.proceed(requestBuilder)
        }
    }

}
