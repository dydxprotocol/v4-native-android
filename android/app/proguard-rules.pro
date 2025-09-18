-dontoptimize

# Some methods are only called from tests, so make sure the shrinker keeps them.
-keep class com.example.android.architecture.blueprints.** { *; }

-keep class androidx.drawerlayout.widget.DrawerLayout { *; }
-keep class androidx.test.espresso.**
# keep the class and specified members from being removed or renamed
-keep class androidx.test.espresso.IdlingRegistry { *; }
-keep class androidx.test.espresso.IdlingResource { *; }

-keep class com.google.common.base.Preconditions { *; }

-keep class androidx.room.RoomDataBase { *; }
-keep class androidx.room.Room { *; }
-keep class android.arch.** { *; }

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class org.web3j.** { *; }
-keep class exchange.dydx.web3.** { *; }

# cartera
-keep interface com.walletconnect.foundation.network.data.service.RelayService
-keep interface kotlinx.coroutines.flow.Flow
-keep class com.walletconnect.foundation.network.model.RelayDTO.BatchSubscribe.Result.Acknowledgement
-keep class exchange.dydx.cartera.**
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Proguard rules that are applied to your test apk/code.
-ignorewarnings

-keepattributes *Annotation*

-dontnote junit.framework.**
-dontnote junit.runner.**

-dontwarn androidx.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.javawriter.JavaWriter
# Uncomment this if you use Mockito
-dontwarn org.mockito.**

-dontwarn org.slf4j.impl.StaticMDCBinder

-keep class  exchange.dydx.dydxCartera.solana.** { *; }

# Keep EventBus annotations
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keep class com.proyecto26.inappbrowser.** { *; }