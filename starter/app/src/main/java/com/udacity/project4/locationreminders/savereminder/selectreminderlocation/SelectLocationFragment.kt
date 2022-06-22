package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment() , OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var maps: GoogleMap
    private lateinit var selectedPOI: PointOfInterest

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    companion object {
        const val DEFAULT_ZOOM_LEVEL = 17f
        const val REQUEST_CODE= 1

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_selected) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.save.setOnClickListener {
                onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        // chek first if selected poi is not null
        if (this::selectedPOI.isInitialized) {
            _viewModel.latitude.value = selectedPOI.latLng.latitude
            _viewModel.longitude.value = selectedPOI.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedPOI.name
            _viewModel.selectedPOI.value = selectedPOI
            _viewModel.navigationCommand.value = NavigationCommand.Back
        } else {
            Toast.makeText(context, "Please select a location", Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
      // set map style from MenuItem
        R.id.hybrid_map -> {
            maps.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.normal_map -> {
            maps.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.satellite_map -> {
            maps.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            maps.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(p0: GoogleMap?) {
        //when map is ready
        maps = p0!!
        setPoiClick(maps)
        setOnMapClick(maps)
        setMapLongClickListener(maps)
        setMapStyle(maps)

        if (isPermissionGranted()) {
            enableMyLocation()
        } else {
            requestQPermission()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        // point of interest
        map.setOnPoiClickListener { pointOfInterest ->
            map.clear()

            this.selectedPOI = pointOfInterest
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .title(pointOfInterest.name)
                    .position(pointOfInterest.latLng)
            )
            poiMarker?.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLng(pointOfInterest.latLng))

        }
    }

    private fun setOnMapClick(maps: GoogleMap) {
        // click on map
        maps.setOnMapClickListener { latLng ->
            maps.clear()
         // snippet is a text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLng.latitude,
                latLng.longitude
            )
            // set selected poi and add marker
            selectedPOI = PointOfInterest(latLng, "", getString(R.string.dropped_pin))
            val poi = selectedPOI
           val marker= maps.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .snippet(snippet)
            )
            marker?.showInfoWindow()
            maps.animateCamera(CameraUpdateFactory.newLatLng(latLng))


        }
    }

    private fun setMapLongClickListener(maps: GoogleMap) {
        // long click on map
            maps.setOnMapLongClickListener { latLng ->
            maps.clear()
                val snippet = String.format(
                    Locale.getDefault(),
                    getString(R.string.lat_long_snippet),
                    latLng.latitude,
                    latLng.longitude
                )
                // set selected poi and add marker
                selectedPOI = PointOfInterest(latLng, "", getString(R.string.dropped_pin))
                val poi = selectedPOI
                val marker= maps.addMarker(
                    MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)
                        .snippet(snippet)
                )
                marker?.showInfoWindow()
                maps.animateCamera(CameraUpdateFactory.newLatLng(latLng))


            }
    }
    private fun showRationale() {
        //create alert dialog to warning the user we need a location permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(requireActivity())
                .setTitle("Location Permission")
                .setMessage(R.string.permission_denied_explanation)
                .setPositiveButton("OK") { _,_->
                    // if user click ok then we request permission again
                    requestQPermission()
                }
                    // create the dialog
                .create()
                .show()

        } else {
            requestQPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        maps.isMyLocationEnabled = true
        // get last location
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    maps.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLocation,
                            DEFAULT_ZOOM_LEVEL
                        )
                    )
                    // user current location
                   val marker = maps.addMarker(
                        MarkerOptions().position(userLocation)
                            .title("im here"))
                    marker?.showInfoWindow()
                }
            }
    }

    private fun setMapStyle(maps: GoogleMap) {
        // Customize the styling of the base map using a JSON object defined
        // in a raw resource file.
        try {
            maps.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            Log.i("MAPS STYLE FAILED ", e.message.toString())
        }
    }

    private fun isPermissionGranted(): Boolean {
        // chek if permission is granted
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestQPermission() {
        // request permission
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            maps.isMyLocationEnabled = true
        } else {
            this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE)
        }
    }


    //
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // if permission is granted
        if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            // get last location
            enableMyLocation()
        } else {
            //create alert dialog to warning the user we need a location permission
            showRationale()
        }
    }
    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (isPermissionGranted())
            if (::maps.isInitialized)
                maps.isMyLocationEnabled = true
    }

}
