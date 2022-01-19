package com.example.datenightv3.data

import android.content.res.Resources
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.example.datenightv3.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.lang.Exception
import java.net.URL

class WebInterface {

    enum class elementType { CLASS, ID }

    data class JsonFile(
        var data: Array<Geocode>
    )
    data class Geocode(
        var latitude: Double?, var longitude: Double?, var type: String?, var name: String?, var String: Int?, var postal_code: String?,
        var street: String?, var confidence: Double?, var region: String?, var region_code: String?, var county: String?, var locality: String?,
        var administrative_area: String?, var neighbourhood: String?, var country: String?, var country_code: String?, var continent: String?,
        var label: String?
    )

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
        val url = "https://www.findlatitudeandlongitude.com/l/Singapore+" + postalCode
        var coordinateList: MutableList<Double?> = mutableListOf(0.0, 0.0)
        coordinateList[0] = getElement(url, latitudeIdentifier, elementType.ID)?.toDouble()
        coordinateList[1] = getElement(url, longitudeIdentifier, elementType.ID)?.toDouble()
        return coordinateList
    }

    suspend fun getLocationCoordinatesAlternate(locationName: String): MutableList<Double?> {
        var coordinateList: MutableList<Double?> = mutableListOf(null, null)
        val noPostCodeLocationName = locationName.replaceAfter(',', "").replace(" ", "%20")
        val postalCode = locationName.substring(locationName.length-6, locationName.length)
        val apiKey = "0dbb931e01b8528009d24b773743961b"
        var urlList: MutableList<String> = mutableListOf("","")
        urlList[0] = "http://api.positionstack.com/v1/forward?access_key="+ apiKey +"&query=" + locationName + "&output=json"
        urlList[1] = "http://api.positionstack.com/v1/forward?access_key="+ apiKey +"&query=" + noPostCodeLocationName + "&output=json"
        try {
            for (url in urlList) {
                val jsonString = withContext(Dispatchers.IO) { URL(url).readText() }
                val typeToken = object : TypeToken<JsonFile>() {}.type
                var jsonFile: JsonFile = Gson().fromJson(jsonString, typeToken)
                Log.d("test", url)
                Log.d("test", jsonFile.toString())
                for (entry in jsonFile.data) {
                    if (entry.postal_code == postalCode || entry.confidence == 1.0) { //temporary fix to bypass no postal code issue
                        coordinateList[0] = entry.latitude
                        coordinateList[1] = entry.longitude
                        return coordinateList
                    }
                }
            }
        } catch (e: Exception) {}
        return coordinateList
    }


    private suspend fun getElement(url: String, identifier: String?, elemType: WebInterface.elementType?): String? {
        try {
            val doc: Document = withContext(Dispatchers.IO) {
                Jsoup.connect(url).get()
            }
            if (elemType == WebInterface.elementType.CLASS) {
                val addressSpan = doc.getElementsByClass(identifier)
                for (element in addressSpan) {
                    if (element.className() == identifier) return element.text()
                }
            } else if (elemType == WebInterface.elementType.ID){
                val element = doc.getElementById(identifier)
                return element.getElementsByClass("value green").text()
            } else {
                return doc.text()
            }
        } catch (e: IOException) { }
        return null
    }
}