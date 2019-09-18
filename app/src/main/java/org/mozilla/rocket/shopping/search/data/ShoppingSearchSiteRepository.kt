package org.mozilla.rocket.shopping.search.data

import android.content.Context
import androidx.lifecycle.LiveData
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.rocket.extension.map
import org.mozilla.rocket.preference.stringLiveData
import org.mozilla.strictmodeviolator.StrictModeViolation

class ShoppingSearchSiteRepository(appContext: Context) {

    private val preference = StrictModeViolation.tempGrant({ builder ->
        builder.permitDiskReads()
    }, {
        appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    })

    private val mockPreferenceSiteList = listOf(
        ShoppingSite("Lazada", "https://www.lazada.co.id/catalog/?q=", "lazada.co.id", isEnabled = true),
        ShoppingSite("Bukalapak", "https://www.bukalapak.com/products?utf8=✓&search%5Bkeywords%5D=", "bukalapak.com", isEnabled = true),
        ShoppingSite("Tokopedia", "https://www.tokopedia.com/search?st=product&q=", "tokopedia.com", isEnabled = true),
        ShoppingSite("JD.ID", "https://m.jd.id/search?keywords=", "jd.id", isEnabled = true),
        ShoppingSite("Shopee", "https://shopee.co.id/search?keyword=", "shopee.co.id", isEnabled = true),
        ShoppingSite("BliBli", "https://www.blibli.com/jual/backpack?searchTerm=", "blibli.com", isEnabled = true)
    )

    fun getShoppingSites(): List<ShoppingSite> {
        val shoppingSitesJsonString = preference.getString(KEY_SHOPPING_SEARCH_SITE, "")
        return if (shoppingSitesJsonString.isNullOrEmpty()) {
            getDefaultShoppingSites()
        } else {
            shoppingSitesJsonString.toPreferenceSiteList()
        }
    }

    fun getShoppingSitesLiveData(): LiveData<List<ShoppingSite>> =
        preference.stringLiveData(KEY_SHOPPING_SEARCH_SITE, "")
            .map {
                if (it.isNotEmpty()) {
                    it.toPreferenceSiteList()
                } else {
                    getDefaultShoppingSites()
                }
            }

    private fun getDefaultShoppingSites(): List<ShoppingSite> {
        // TODO:
        return mockPreferenceSiteList
    }

    fun updateShoppingSites(shoppingSites: List<ShoppingSite>) {
        val siteJsonArray = JSONArray()
        shoppingSites.map { it.toJson() }
            .forEach { siteJsonArray.put(it) }
        preference.edit().putString(KEY_SHOPPING_SEARCH_SITE, siteJsonArray.toString()).apply()
    }

    companion object {
        const val PREF_NAME = "shopping_search"
        const val KEY_SHOPPING_SEARCH_SITE = "shopping_search_site"
    }
}

data class ShoppingSite(
    val title: String,
    val searchUrl: String,
    val displayUrl: String,
    var isEnabled: Boolean
) {
    constructor(obj: JSONObject) : this(
        obj.optString("title"),
        obj.optString("searchUrl"),
        obj.optString("displayUrl"),
        obj.optBoolean("isEnabled")
    )

    fun toJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("searchUrl", searchUrl)
        put("displayUrl", displayUrl)
        put("isEnabled", isEnabled)
    }
}

private fun String.toPreferenceSiteList(): List<ShoppingSite> =
    JSONArray(this).run {
        (0 until length())
            .map { index -> optJSONObject(index) }
            .map { jsonObject -> ShoppingSite(jsonObject) }
    }
