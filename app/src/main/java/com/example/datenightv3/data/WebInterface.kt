package com.example.datenightv3.data

import android.os.Looper
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException

class WebInterface {
    enum class elementType { CLASS, ID }
    suspend fun getLocationName(ideaName: String): String? {
        val identifier = "LrzXr"
        val concatName = ideaName.replace(' ', '+')
        val url = "https://www.google.com/search?q=food+" + concatName
        var locationName: String?
        locationName = getElement(url, identifier, elementType.CLASS)
        return locationName
    }

    suspend fun getLocationCoordinates(locationName: String): MutableList<Double?> {
        val latitudeIdentifier = "lat_address"
        val longitudeIdentifier = "lon_address"
        val postalCode = locationName.substring(locationName.length-6, locationName.length)
        val url = "https://www.findlatitudeandlongitude.com/l/" + postalCode
        var coordinateList: MutableList<Double?> = mutableListOf(0.0, 0.0)
        coordinateList[0] = getElement(url, latitudeIdentifier, elementType.ID)?.toDouble()
        coordinateList[1] = getElement(url, longitudeIdentifier, elementType.ID)?.toDouble()
        return coordinateList
    }

    private suspend fun getElement(url: String, identifier: String, elemType: WebInterface.elementType): String? {
        try {
            val doc: Document = withContext(Dispatchers.IO) {
                Jsoup.connect(url).get()
            }
            if (elemType == elementType.CLASS) {
                val addressSpan = doc.getElementsByClass(identifier)
                for (element in addressSpan) {
                    if (element.className() == identifier) return element.text()
                }
            } else {
                val element = doc.getElementById(identifier)
                return element.getElementsByClass("value green").text()
            }
        } catch (e: IOException) { }
        return null
    }
}