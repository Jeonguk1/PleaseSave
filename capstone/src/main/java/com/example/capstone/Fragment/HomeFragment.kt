package com.example.capstone.Fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.capstone.R
import com.example.capstone.data.AddressResponse
import com.example.capstone.data.Hospital
import com.example.capstone.databinding.FragmentHomeBinding
import com.example.capstone.service.KakaoApiService
import com.example.capstone.service.RestApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.StringReader

class HomeFragment : Fragment(), MapView.MapViewEventListener {
    private var mBinding: FragmentHomeBinding? = null
    private val binding get() = mBinding!!

    private val restApiService = RestApiService.instance
    private val kakaoApiService = KakaoApiService.instance

    private lateinit var mapView: MapView
    private lateinit var markerCardView: CardView
    private lateinit var markerInfoTextView: TextView
    private lateinit var markerInfoAddressView: TextView
    private lateinit var markerInfoPhoneView: TextView

    private var hospitalList : ArrayList<Hospital> = ArrayList()


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = MapView(activity)

        checkAndRequestLocationPermissions()

        markerCardView = view.findViewById(R.id.markerCardView)
        markerInfoTextView = view.findViewById(R.id.markerInfoTextView)
        markerInfoAddressView = view.findViewById(R.id.markerInfoAddressView)


    }

    override fun onDestroyView() {
        binding.mapView.removeAllViews()
        mapView.onSurfaceDestroyed()

        mBinding = null

        super.onDestroyView()
    }

    private fun checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없을 경우, 사용자에게 권한 요청
            requestLocationPermissions()
        } else {
            // 권한이 이미 있을 경우, 위치 관련 작업 수행
            initMapView()

        }
    }

    private fun requestLocationPermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun initMapView() {
        val mapViewContainer = binding.mapView
        mapViewContainer.addView(mapView)

        mapView.setMapViewEventListener(this)
        //val balloonAdapter = CustomBalloonAdapter(layoutInflater)
        //mapView.setCalloutBalloonAdapter(balloonAdapter)
    }

    /*class CustomBalloonAdapter(inflater: LayoutInflater) : MapView. {
        private val mBalloonLayout: View = inflater.inflate(R.layout.custom_balloon_layout, null)

        override fun getBalloon(clusterItem: ClusterItem, convertView: View?, parent: ViewGroup?): View {
            // 말풍선 뷰 설정
            // 예: mBalloonLayout.findViewById<TextView>(R.id.balloon_text_view).text = clusterItem.title
            return mBalloonLayout
        }

    }*/

    private fun getHospitals(address1: String = "", address2: String = "", div: String) {
        Log.i("@@@@@", "getHospitals : $address1, $address2, $div")

        restApiService.getHospitals(RestApiService.serviceKey, address1, address2, div,100).enqueue(object :
            Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()?.string()

                    if (body != null) {
                        parseXmlAndAddToList(body)
                        showMarkers()
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.i("@@@@@", "onFailure : $t")

            }

        })
    }

    fun parseXmlAndAddToList(xmlData: String) {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlData))

        var eventType = parser.eventType
        var currentName: String? = null
        var currentLat: Double? = null
        var currentLon: Double? = null
        var currentAddress: String? = null
        var currentPhone: String? = null

        var currentTag: String? = null

        // 초기화
        hospitalList = ArrayList()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "item") {
                        currentName = null
                        currentLat = null
                        currentLon = null
                        currentAddress = null
                        currentPhone = null
                    }
                    currentTag = tagName


                }
                XmlPullParser.TEXT -> {
                    when (currentTag) {
                        "dutyName" -> {
                            currentName = parser.text.trim()
                        }
                        "wgs84Lat" -> {
                            currentLat = parser.text.trim().toDoubleOrNull()
                        }
                        "wgs84Lon" -> {
                            currentLon = parser.text.trim().toDoubleOrNull()
                        }
                        "dutyAddr" -> {
                            currentAddress = parser.text.trim()
                        }
                        "dutyTel1" -> {
                            currentPhone = parser.text.trim()
                        }

                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName == "item") {
                        if (currentName != null && currentLat != null && currentLon != null && currentPhone != null && currentAddress != null)  {
                            hospitalList.add(Hospital(currentName, currentAddress, currentPhone, currentLat, currentLon))
                            //Log.e("@@@@@", "==> add $currentName, $currentLat, $currentLon")
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    }

    // Reverse Geocoding 함수
    private suspend fun reverseGeocode(latitude: Double, longitude: Double) : List<String> {
        return withContext(Dispatchers.IO) {
            val response = kakaoApiService.coordToAddress(longitude.toString(), latitude.toString())

            if (response.isSuccessful) {
                val address = response.body() as AddressResponse

                val region1DepthName = address.documents.first().address.region_1depth_name
                val region2DepthName = address.documents.first().address.region_2depth_name

                Log.e("@@@@@", "====> region1DepthName : $region1DepthName, region2DepthName : $region2DepthName")

                listOf(region1DepthName, region2DepthName)
            } else {
                emptyList()
            }
        }
    }

    private fun showMarkerInfo(mapPOIItem: MapPOIItem) {
        val markerName = mapPOIItem.itemName

        val markerAddress = mapPOIItem.userObject as String

        markerCardView.visibility = View.VISIBLE

        markerInfoTextView.text = "기관명: $markerName"
        markerInfoAddressView.text = "주소: $markerAddress"

    }

    private fun showMarkers() {

        val markers = hospitalList.map { hospital ->
            MapPOIItem().apply {
                itemName = hospital.name
                mapPoint = MapPoint.mapPointWithGeoCoord(hospital.lat, hospital.lon)
                markerType = MapPOIItem.MarkerType.BluePin
                selectedMarkerType = MapPOIItem.MarkerType.RedPin
                userObject = hospital.address
            }
        }

        markers.forEach {marker ->
            mapView.addPOIItem(marker)
        }

        //mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(hospitalList.first().lat, hospitalList.first().lon), true)
        mapView.setPOIItemEventListener(object : MapView.POIItemEventListener {
            override fun onPOIItemSelected(mapView: MapView, mapPOIItem: MapPOIItem) {
                showMarkerInfo(mapPOIItem)
            }

            override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {}
            override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?, p2: MapPOIItem.CalloutBalloonButtonType?) {}
            override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {}
        })
    }

    override fun onMapViewInitialized(mapView: MapView) {
        // 최초 맵화면 진입시 현재위치로 맵 이동
        Log.e("@@@@@", "========> onMapViewInitialized")
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
        Log.e("@@@@@", "========> mapPointGeo.latitude : ${p1?.mapPointGeoCoord?.latitude}")
        Log.e("@@@@@", "========> mapPointGeo.longitude : ${p1?.mapPointGeoCoord?.longitude}")

        mapView.removeAllPOIItems()
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving

        p1?.let {
            val mapPointGeo = it.mapPointGeoCoord
            val latitude = mapPointGeo.latitude
            val longitude = mapPointGeo.longitude

            lifecycleScope.launch {
                // Reverse Geocoding API를 호출하여 시, 군, 구 정보 가져오기
                val addressList = reverseGeocode(latitude, longitude)

                // 시, 군, 구 정보를 파라미터로 병원 찾기 (a,b,c,g,h,m,n,r)
                var dutyDivs : MutableList<String> = mutableListOf()
                dutyDivs.add("A")
                dutyDivs.add("B")
                dutyDivs.add("C")
                dutyDivs.add("E")
                dutyDivs.add("D")
                dutyDivs.add("G")
                dutyDivs.add("H")
                dutyDivs.add("M")
                dutyDivs.add("N")
                dutyDivs.add("R")

                for (div in dutyDivs) {
                    getHospitals(addressList[0], addressList[1], div)
                }
            }

        }
    }
}
