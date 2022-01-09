package com.example.datenightv3.data

import android.util.Log
import com.example.datenightv3.data.classes.Idea
import java.util.stream.IntStream.range

class DataSorter(var ideaList: MutableList<Idea>, var distanceList: MutableList<Double>, var ideasCount: Int){

    fun sortDistance(): MutableList<Idea> { //currently bubble sort, might change to more efficient sorting later
        var i = 0
        while (i < ideasCount-1) {
            if (distanceList[i] > distanceList[i+1]) {
                var tempDistance = distanceList[i]
                distanceList[i] = distanceList[i+1]
                distanceList[i+1] = tempDistance

                var tempIdea = ideaList[i]
                ideaList[i] = ideaList[i+1]
                ideaList[i+1] = tempIdea
                i = 0
            } else i++
        }
        return ideaList
    }
}