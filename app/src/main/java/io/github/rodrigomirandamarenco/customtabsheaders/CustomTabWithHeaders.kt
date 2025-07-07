package io.github.rodrigomirandamarenco.customtabsheaders

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.net.toUri

class CustomTabWithHeaders(
    private val activity: ComponentActivity,
    private val urlToOpen: String,
    private val headers: Map<String, String>,
) {
    private val url = urlToOpen.toUri()
    private val validBrowserPackageToUse by lazy { CustomTabsHelper.getPackageNameToUse(activity) }

    private var customTabsServiceConnection: CustomTabsServiceConnection? = null
    private var customTabsSession: CustomTabsSession? = null

    fun launch() {
        if (!isUrlValid(urlToOpen)) {
            safeThrow(Exception("Provided url is invalid"))
            return
        }

        customTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                customTabsSession = client.newSession(object : CustomTabsCallback() {
                    override fun onRelationshipValidationResult(
                        relation: Int,
                        requestedOrigin: Uri,
                        result: Boolean,
                        extras: Bundle?
                    ) {
                        Log.d(
                            "CustomTabWithHeaders",
                            "onRelationshipValidationResult: result: $result <><> extra: $extras <><> req: $requestedOrigin <><> relation: $relation"
                        )

                        // If the result is returned as "false" meaning that the relationship validation failed then
                        // headers will not be sent to the custom tab. This mainly happens when the url you are trying to open
                        // is not hosting the assetlinks.json file.
                        // For example, if you are trying to open url: https://www.mySocialMediaApp.com/posts/
                        // then make sure the assetlinks.json file is hosted at https://www.mySocialMediaApp.com/.well-known/assetlinks.json
                        // i.e "base_url" + ".well-known/assetlinks.json" and the assetlinks file contains "delegate_permission/common.use_as_origin"
                        // in the "relation" array inside the file. Then only the headers will be sent to the custom tab.
                        // Also make sure you are testing it using a SIGNED APK.


                        // we are opening the tab irrespective of the result.
                        // Even if validation fails in worst case, at least we redirect the user to web (without headers).
                        openCustomChromeTab()

                        // unbind the service connection as we only needed it for validation and that is completed at this point of execution.
                        unbindTabConnectionService()
                    }
                })
                client.warmup(0L)
                customTabsSession?.validateRelationship(
                    CustomTabsService.RELATION_USE_AS_ORIGIN,
                    url,
                    null
                )
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }.also { service ->
            CustomTabsClient.bindCustomTabsService(
                activity,
                validBrowserPackageToUse ?: "com.android.chrome",
                service
            )
        }
    }

    private fun openCustomChromeTab() {
        // build custom tabs intent
        val intentBuilder = CustomTabsIntent.Builder(customTabsSession)
        val customTabsIntent = intentBuilder.build()

        // add headers
        val bundle = Bundle()
        for ((key, value) in headers) {
            bundle.putString(key, value)
        }
        customTabsIntent.intent.putExtra(Browser.EXTRA_HEADERS, bundle)

        if (!validBrowserPackageToUse.isNullOrBlank()) {
            customTabsIntent.intent.setPackage(validBrowserPackageToUse)
            customTabsIntent.launchUrl(activity, url)
        } else {
            // no valid package name found, means there's no chromium browser.
            // here you can open Web View as fallback or handle it according to your requirements
        }
    }

    private fun unbindTabConnectionService() {
        customTabsServiceConnection?.let {
            activity.unbindService(it)
            customTabsServiceConnection = null
            customTabsSession = null
        }
    }

    private fun safeThrow(exception: Exception) {
        throw exception
    }

    private fun isUrlValid(url: String?): Boolean {
        return !url.isNullOrEmpty() && (url.startsWith("http://") || url.startsWith("https://"))
    }
}