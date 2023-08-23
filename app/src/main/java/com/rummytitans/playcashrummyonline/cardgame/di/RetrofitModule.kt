package com.rummytitans.playcashrummyonline.cardgame.di

import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.MainApplication
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import android.text.TextUtils
import com.google.gson.Gson
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
    fun getApiInterface(retrofit: Retrofit) = retrofit.create(APIInterface::class.java)

    @Singleton
    @Provides
    fun provideGson() = Gson()

    @Singleton
    @Provides
    fun getRetrofit(okHttpClient: OkHttpClient):Retrofit {
      val baseApiUrl= if (TextUtils.isEmpty(MainApplication.appUrl))
          MyConstants.APP_CURRENT_URL
      else
          MainApplication.appUrl ?: MyConstants.APP_CURRENT_URL
        return Retrofit.Builder()
            .baseUrl(baseApiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient).build()
    }

    @Singleton
    @Provides
    fun getOkHttpClient(interceptor: Interceptor) =
        OkHttpClient.Builder().addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).build()

    @Singleton
    @Provides
    fun getInterceptor(pref: SharedPreferenceStorage, gson: Gson): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->
            val request = chain.request()
            val httpUrl = request.url
            val url = httpUrl.newBuilder().build()
            val builder = request.newBuilder().url(url)
            builder.addHeader("AppVersion", BuildConfig.VERSION_CODE.toString())
            builder.addHeader("AppType", "${MyConstants.APP_TYPE}") //uses in BaseViewModel and Analytic helper fireevent()
            builder.addHeader("GameType", "1")
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
