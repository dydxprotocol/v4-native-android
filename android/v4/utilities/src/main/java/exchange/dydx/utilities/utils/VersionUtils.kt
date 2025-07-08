package exchange.dydx.utilities.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

object VersionUtils {
    fun versionName(context: Context): String? {
        try {
            val pInfo: PackageInfo =
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    fun versionCode(context: Context): Int? {
        try {
            val pInfo: PackageInfo =
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
            return pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }
}
