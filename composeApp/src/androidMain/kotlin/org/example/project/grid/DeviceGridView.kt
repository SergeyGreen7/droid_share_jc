//package org.example.project.grid
//
//import android.content.Context
//import android.net.http.UrlRequest.Status
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import org.example.project.StatusUpdater
//
//class DeviceGridView(
//    context : Context,
//    private val recyclerView: RecyclerView,
//    private var statusUpdater : StatusUpdater,
//    private val numColumns: Int
//) {
//    var customAdapter: DeviceCustomAdapter
//
//    init {
//        recyclerView.layoutManager = GridLayoutManager(context,numColumns)
//        customAdapter = DeviceCustomAdapter(statusUpdater)
//        recyclerView.adapter = customAdapter
//
////        val tmpList = listOf(
////            DeviceInfo("qeqwe","qweqwe"),
////            DeviceInfo("asdasdasd","asdasdasd")
////        )
////        customAdapter.updateDataSet(tmpList)
//    }
//
//    fun updateDataSet(deviceList: List<DeviceInfo>) {
//        customAdapter.updateDataSet(deviceList)
//    }
//}
//
