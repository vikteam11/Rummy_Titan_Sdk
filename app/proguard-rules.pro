-dontobfuscate
#retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keep class com.rummytitans.sdk.cardgame.ui.RummyMainActivity { *; }
-keep class androidx.lifecycle.ViewModel.** {*;}
#okhttp3
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class androidx.viewbinding.** { *; }
-keep class androidx.viewbinding.adapters.** { *; }

-optimizationpasses 1
# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-keep class com.google.gson.stream.** { *; }
-dontwarn com.sothree.**
-keep class com.sothree.**
-keep interface com.sothree.**

#juspay
-dontwarn in.juspay.**
-keep class androidx.** {*;}
-keep class com.rummytitans.sdk.cardgame.models.** {*;}
-keep class com.rummytitans.sdk.cardgame.ui.quiz.realtime.question.** {*;}
-keep class com.rummytitans.sdk.cardgame.ui.** {*;}


-dontwarn com.squareup.okhttp.**

-dontwarn io.hansel.**
-keep class io.hansel.**{*;}
-keep class * implements io.hansel.**.* {*;}
-keep class * extends io.hansel.**.* {*;}
-dontwarn org.cocos2dx.**
-keep class androidx.lifecycle.ViewModel { *; }
-keepclassmembers class androidx.lifecycle.LiveData { *; }
-keepclassmembers class androidx.lifecycle.MutableLiveData { *; }
-dontwarn kotlin.time.jdk8.**


-dontwarn module-info

#### -- Picasso --
 -dontwarn com.squareup.picasso.**

 #### -- OkHttp --
 -dontwarn com.squareup.okhttp.internal.**

 #### -- Apache Commons --
 -dontwarn org.apache.commons.logging.**


-verbose
#-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*


-keepclassmembers class com.google.devtools.build.android.desugar.runtime.ThrowableExtension {
    ** SDK_INT;
}
-keep class com.google.android.gms.dynamic.IObjectWrapper
-keep class com.google.android.gms.tasks.Task
-keep class com.google.android.gms.tasks.TaskCompletionSource
-keep class com.google.android.gms.tasks.OnSuccessListener
-keep class com.google.android.gms.tasks.OnFailureListener
-keep class com.google.android.gms.tasks.OnCompleteListener
-keep class com.google.android.gms.tasks.Continuation
-keep class com.google.android.gms.measurement.AppMeasurement$EventInterceptor
-keep class com.google.android.gms.measurement.AppMeasurement$OnEventListener
-keep class com.google.android.gms.measurement.AppMeasurement$zza
-keep class com.google.android.gms.internal.zzcgl
-keep class com.google.android.gms.internal.zzbhh
-keep class com.google.android.gms.internal.aad
-keep class com.google.android.gms.internal.aae
-keep class com.google.android.gms.internal.iq
-keep class com.google.android.gms.internal.ly
-keep class com.google.android.gms.internal.kx
-keep class com.google.android.gms.internal.xf
-keep class com.google.android.gms.internal.qu
-keep class com.google.android.gms.internal.qr
-keep class com.google.android.gms.internal.xm
-keep class com.google.android.gms.internal.aaj
-keep class com.google.android.gms.internal.aat
-keep class com.google.android.gms.internal.aah
-keep class com.google.android.gms.internal.rx
-keep class com.google.android.gms.internal.qg
-keep class com.google.android.gms.internal.sh
-keep class com.google.android.gms.internal.qu
-keep class com.google.android.gms.internal.vq
-keep class com.google.android.gms.internal.qi
-keep class com.google.android.gms.internal.oh
-keep class com.google.android.gms.internal.oo
-keep class com.google.android.gms.internal.oc
-keep class com.google.android.gms.internal.oi
-keep class com.google.android.gms.internal.ol
-keep class com.google.android.gms.internal.wn
-keep class com.google.android.gms.internal.oj
-keep class com.google.android.gms.internal.om
-keep class com.google.android.gms.internal.pf
-keep class com.google.android.gms.internal.za
-keep class com.google.android.gms.internal.pz
-keep class com.google.android.gms.internal.zn
-keep class com.google.android.gms.internal.zi
-keep class com.google.android.gms.internal.aen
-keep class com.google.android.gms.internal.aas
-keep class com.google.android.gms.internal.aav
-keep class com.google.android.gms.internal.aag
-keep class com.google.android.gms.internal.abh
-keep class com.google.android.gms.internal.abk
-keep class com.google.android.gms.internal.abq
-keep class com.google.android.gms.internal.abl
-keep class com.google.android.gms.internal.acf
-keep class com.google.android.gms.common.api.Result
-keep class com.google.android.gms.common.zza

-keep class com.apxor.androidsdk.**{*;}
-dontwarn com.apxor.**

-dontnote com.google.android.gms.internal.ql
-dontnote com.google.android.gms.internal.zzcem
-dontnote com.google.android.gms.internal.zzchl

# Protobuf notes
-dontnote com.google.protobuf.zzc
-dontnote com.google.protobuf.zzd
-dontnote com.google.protobuf.zze

#firebase
-keep class com.firebase.** { *; }
-dontwarn com.firebase.**
-dontnote com.firebase.client.core.GaePlatform

# Crashlytics 1.+
-keep class com.crashlytics.** { *; }
-keepattributes SourceFile,LineNumberTable

-dontwarn in.myteam11.**
-dontwarn kotlinx.**

-keep class com.microsoft.signalr.** { *; }
 -keep interface com.microsoft.signalr.** { *; }

-optimizations !class/unboxing/enum

# Smartech
# Smartech Base SDK
-dontwarn com.netcore.android.**
-keep class com.netcore.android.**{*;}
-keep class * implements com.netcore.android.**.* {*;}
-keep class * implements com.netcore.android.*. {*;}
-keep class * extends com.netcore.android.**.* {*;}
-keep class * extends com.netcore.android.*. {*;}
# Smartech Push SDK
-dontwarn com.netcore.android.smartechpush.**
-keep class com.netcore.android.smartechpush.**{*;}
-keep class * implements com.netcore.android.smartechpush.**.* {*;}
-keep class * extends com.netcore.android.smartechpush.**.* {*;}
-keep class in.netcore.** { *; }

-keep interface in.netcore.** { *; }

-keep class com.appsflyer.** { *; }



# Proguard Cocos2d-x-lite for release
-keep public class org.cocos2dx.** { *; }
-dontwarn org.cocos2dx.**

# Proguard Apache HTTP for release
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**



-keep class okio.** { *; }
-dontwarn okio.**

# Proguard Android Webivew for release. you can comment if you are not using a webview
-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient





#region SDK rules
-verbose

-dontwarn android.support.**
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.utils.Box2DBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*
-dontwarn com.badlogic.gdx.graphics.g2d.freetype.FreetypeBuild

-keep class com.badlogic.gdx.controllers.android.AndroidControllers
-keep class com.badlogic.gdx.backends.android.AndroidApplication
-keep class com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
-keep class com.pocket52.application.P52Poker
-keep class com.pocket52.helper.NumberFormatter
-keep class com.pocket52.hld.table.IGameLifecycleListener


-keepclassmembers class com.pocket52.application.P52Poker {
   void    init(com.pocket52.application.Config);
   com.pocket52.application.P52Poker    get();
   void    disposeToken();
   void    openPokerRooms();
   void    closePokerRooms();
}

-keep class com.pocket52.application.Config {
*;
}
-keep class com.pocket52.application.IPokerListener {
*;
}
-keep class com.pocket52.application.ERROR {
*;
}
-keep class com.pocket52.application.MODE {
*;
}
-keep class com.pocket52.application.PrivateTableInfo {
*;
}

-keepnames class com.badlogic.gdx.backends.android.AndroidInput*
-keepclassmembers class com.badlogic.gdx.backends.android.AndroidInput* {
   <init>(com.badlogic.gdx.Application, android.content.Context, java.lang.Object, com.badlogic.gdx.backends.android.AndroidApplicationConfiguration);
}

-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
   boolean contactFilter(long, long);
   void    beginContact(long);
   void    endContact(long);
   void    preSolve(long, long);
   void    postSolve(long, long);
   boolean reportFixture(long);
   float   reportRayFixture(long, float, float, float, float, float);
}
#endregion

#region okHttp rules (used by picasso)
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
#endregion

#region LoganSquare
-keep class com.bluelinelabs.logansquare.** { *; }
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *
-keep class **$$JsonObjectMapper { *; }
#endregion

#region Branch
-dontwarn com.android.installreferrer.api.**
-dontwarn com.crashlytics.android.answers.shim.**
-dontwarn com.google.firebase.appindexing.**
#endregion
-keep class android.arch.** { *; }

-keep class org.apache.commons.logging.**               { *; }
-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }
-keep class org.codehaus.**                             { *; }
-keepattributes Signature,*Annotation*

-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn com.amazonaws.**