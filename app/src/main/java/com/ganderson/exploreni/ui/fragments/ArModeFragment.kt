package com.ganderson.exploreni.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.ganderson.exploreni.ui.activities.CAMERA_PERMISSION
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.Utils
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.ganderson.exploreni.ui.viewmodels.ArModeViewModel
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_ar_mode.*
import kotlinx.android.synthetic.main.layout_ar_mode_location.view.*
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.LocationScene
import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture

class ArModeFragment : Fragment() {
    // "by viewModels()" returns the ViewModel of the type specified, scoped to the current
    // Fragment.
    private val viewModel: ArModeViewModel by viewModels()

    private lateinit var loadingDialog: LoadingDialog
    private var locationScene: LocationScene? = null
    private var locationReady = false
    private var useMetric = false
    private var currentSeekRadius = 5

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "AR Mode"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setDisplayShowHomeEnabled(false)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ar_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        }
        else if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Enable location to use the AR feature.",
                Toast.LENGTH_SHORT).show()
        }
        else {
            startAr()
        }
    }

    private fun startAr() {
        // The ARCore session is created here. This is the "main entry point to the ARCore API".
        // Ref: https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session
        if (asvLookAround.session == null) {
            val arSession = Session(this.activity)
            val config = Config(arSession)

            // LATEST_CAMERA_IMAGE is non-blocking.
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            arSession.configure(config)
            asvLookAround.setupSession(arSession)
        }

        // Set up seekbar here.
        useMetric = PreferenceManager
            .getDefaultSharedPreferences(this.context)
            .getBoolean("measurement_distance", false)

        if(useMetric) {
            tvCurrentArRange.text = "${currentSeekRadius}km"
            skbArRange.max = Utils.MAX_SEEK_KM
            tvMaxArRange.text = "${Utils.MAX_SEEK_KM}km"
        }
        else {
            tvCurrentArRange.text = "${currentSeekRadius}mi"
            skbArRange.max = Utils.MAX_SEEK_MILES
            tvMaxArRange.text = "${Utils.MAX_SEEK_MILES}mi"
        }
        skbArRange.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int,
                                           fromUser: Boolean) {
                seekBar?.let {
                    if(useMetric) {
                        tvCurrentArRange.text = "${progress}km"
                    }
                    else {
                        tvCurrentArRange.text = "${progress}mi"
                    }
                }
            }

            // Required to override all three methods here, even if some may not be implemented.
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(locationReady) {
                    seekBar?.let {
                        if (it.progress != currentSeekRadius) {
                            currentSeekRadius = it.progress
                            getNearbyLocations()
                        }
                    }
                }
            }
        })

        loadingDialog = LoadingDialog(this.requireContext(), "Loading locations, please wait.")

        // Begin observing changes to in-range locations. The observer lambda will run after the
        // parameters are updated in "getNearbyLocations()" and the associated LiveData has been
        // updated.
        viewModel
            .nearbyLocations
            .observe(viewLifecycleOwner) { listResult ->
                loadingDialog.dismiss()
                if(listResult.data != null) {
                    val list = listResult.data
                    locationScene!!.clearMarkers()
                    if (list.isNotEmpty()) {
                        setupMarkers(list)
                    } else {
                        Toast.makeText(this.context, "No locations found.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                else {
                    Toast.makeText(this.context, "Unable to load locations.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        // Set up LocationScene. "apply" is a handy Kotlin extension function to call multiple of
        // an object's methods without needing to refer to the object each time.
        locationScene = LocationScene(this.activity, asvLookAround).apply {
            setMinimalRefreshing(true)
            setOffsetOverlapping(true)
        }

        val locationTask = LocationTask(WeakReference(this))
        locationTask.execute(locationScene)
    }

    private fun locationIsReady(ready: Boolean) {
        locationReady = ready
        getNearbyLocations()
    }

    private fun getNearbyLocations() {
        loadingDialog.show()

        val lat = locationScene!!.deviceLocation.currentBestLocation.latitude
        val lon = locationScene!!.deviceLocation.currentBestLocation.longitude
        val miles: Int
        if(useMetric) {
            miles = Utils.distanceToImperial(currentSeekRadius.toDouble()).toInt()
        }
        else {
            miles = currentSeekRadius
        }

        viewModel.updateParameters(lat, lon, miles)
    }

    /**
     * Create location markers and add them to the ArSceneView.
     */
    private fun setupMarkers(locationList: List<NiLocation>) {
        locationList.forEach { location ->
            // Marker graphics must be created as ARCore ViewRenderables.
            val vrFuture = ViewRenderable.builder()
                .setView(this.activity, R.layout.layout_ar_mode_location)
                .build()

            // ViewRenderable.Builder#build returns a CompleteableFuture containing the view to
            // render. It is processed here.
            CompletableFuture.anyOf(vrFuture)
                .handle<Any> { _, exception ->
                    if(exception != null) {
                        Toast.makeText(requireContext(),
                            "Could not create marker for ${location.name}", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else {
                        val locNode = loadNode(location, vrFuture)
                        loadImage(location, vrFuture.get())

                        val locationMarker = LocationMarker(
                            location.long.toDouble(),
                            location.lat.toDouble(),
                            locNode
                        )
                        locationScene?.mLocationMarkers?.add(locationMarker)
                        locationMarker.anchorNode?.isEnabled = true
                        locationMarker.scalingMode = LocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN
                        locationMarker.scaleModifier = 0.65f // Size reduced.
                        locationMarker.height = generateRandomHeightBasedOnDistance(location)

                        // Distance calculated in the marker's render event. Previously it was
                        // calculated in "loadNode" but this caused instances where the marker
                        // would not display at all.
                        locationMarker.setRenderEvent { updateDistance(location, vrFuture.get()) }

                        locationScene?.refreshAnchors()
                    }
                }

            asvLookAround.scene.addOnUpdateListener {
                val arFrame = asvLookAround.arFrame
                if(arFrame!!.camera.trackingState == TrackingState.TRACKING) {
                    locationScene!!.processFrame(arFrame)
                }
            }
        }
    }

    // Load the Node which will contain the graphic and details for the marker.
    private fun loadNode(location: NiLocation,
                         lookAroundLayoutFuture: CompletableFuture<ViewRenderable>): Node {
        val node = Node()
        node.renderable = lookAroundLayoutFuture.get()

        val nodeLayout = lookAroundLayoutFuture.get().view
        nodeLayout.name.text = location.name

        // Load the attraction detail screen describing the attraction on which the user tapped.
        nodeLayout.setOnTouchListener { _, _ ->
            val attractionDetailFragment = AttractionDetailFragment(location, false)
            val mainActivity = this.activity as MainActivity
            mainActivity.displayFragment(attractionDetailFragment)
            false
        }

        return node
    }

    private fun loadImage(location: NiLocation, arRenderable: ViewRenderable) {
        // Loading spinner to be displayed while Glide loads the attraction image.
        val loadingSpinner = CircularProgressDrawable(requireContext())
        loadingSpinner.strokeWidth = 5f
        loadingSpinner.centerRadius = 30f
        loadingSpinner.setTint(Color.WHITE)
        loadingSpinner.start()

        // Load image asynchronously with Glide.
        Glide.with(this)
            .load(location.imgUrl)
            .error(R.drawable.placeholder_no_image_available)
            .placeholder(loadingSpinner)
            .into(arRenderable.view.ivArLocation)
    }

    private fun updateDistance(location: NiLocation, lookAroundRenderable: ViewRenderable) {
        val locationDistance = Utils.getHaversineGCD(
            locationScene!!.deviceLocation!!.currentBestLocation!!.latitude,
            locationScene!!.deviceLocation!!.currentBestLocation!!.longitude,
            location.lat.toDouble(),
            location.long.toDouble()
        )

        val formattedDistance: String

        // The Haversine formula returns the distance in kilometres. If the user has elected to use
        // kilometres, do no conversion. Otherwise, convert to miles.
        if(useMetric) {
            formattedDistance = Utils.distanceFormatter.format(locationDistance) + " km"
        }
        else {
            formattedDistance = Utils.distanceFormatter
                .format(
                    Utils.distanceToImperial(locationDistance)
                ) + " miles"
        }

        lookAroundRenderable.view.distance.text = formattedDistance
    }

    private fun generateRandomHeightBasedOnDistance(location: NiLocation): Float {
        val locationDistance = Utils.getHaversineGCD(
            locationScene!!.deviceLocation!!.currentBestLocation!!.latitude,
            locationScene!!.deviceLocation!!.currentBestLocation!!.longitude,
            location.lat.toDouble(),
            location.long.toDouble()
        )

        return when (locationDistance.toInt()) {
            in 0..1000 -> (1..3).random().toFloat()
            in 1001..1500 -> (4..6).random().toFloat()
            in 1501..2000 -> (7..9).random().toFloat()
            in 2001..3000 -> (10..12).random().toFloat()
            else -> (12..13).random().toFloat()
        }
    }

    private fun goHome() {
        val activity = activity as MainActivity
        activity.displayFragment(HomeFragment())

        // To ensure the consistency of the bottom navigation view component, the selected item
        // is set here. This is similar to the "onBackPressed()" method of the MainActivity.
        bnvNavigation.selectedItemId = R.id.nav_home
    }

    private fun requestCameraPermission() {
        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            AlertDialog.Builder(requireContext())
                .setTitle("Camera access requested")
                .setMessage("Camera access is requested to enable the AR functionality.")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    requestPermissions(arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog?.dismiss()

                    // Take the user to the "Home" screen regardless of where they came from. This
                    // is the simplest solution to maintaining the consistency of the bottom
                    // navigation view.
                    goHome()
                }
                .create()
                .show()
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
            AlertDialog.Builder(requireContext())
                .setTitle("Caution")
                .setMessage("Camera is required for this function.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog?.dismiss()
                    goHome()
                }
                .show()
        }
        else if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Enable location to use the AR feature.",
                Toast.LENGTH_SHORT).show()
        }
        else {
            startAr()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        locationScene?.resume()
        asvLookAround.resume()
    }

    override fun onPause() {
        super.onPause()
        locationScene?.pause()
        asvLookAround.pause()
    }

    // The ARCore-Location library handles location retrieval itself, as it sets a large number
    // of parameters to obtain the most precise location possible. As such, loading the AR markers
    // cannot proceed until it has finished. This AsyncTask is an admittedly-hacky way of waiting
    // for it to be ready.
    class LocationTask(private val fragment: WeakReference<ArModeFragment>)
        : AsyncTask<LocationScene, Unit, Boolean>() {

        override fun doInBackground(vararg params: LocationScene?): Boolean {
            val array = DoubleArray(2)
            var latitude: Double?
            var longitude: Double?

            do {
                latitude = params[0]?.deviceLocation?.currentBestLocation?.latitude
                longitude = params[0]?.deviceLocation?.currentBestLocation?.longitude
            } while (latitude == null || longitude == null)

            array[0] = latitude
            array[1] = longitude

            return true
        }

        override fun onPostExecute(result: Boolean) {
            fragment.get()?.locationIsReady(result)
            super.onPostExecute(result)
        }
    }
}
