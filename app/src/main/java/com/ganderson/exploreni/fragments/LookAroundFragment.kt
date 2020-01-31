package com.ganderson.exploreni.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.ganderson.exploreni.CAMERA_PERMISSION
import com.ganderson.exploreni.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.Utils
import com.ganderson.exploreni.models.NiLocation
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.android.synthetic.main.fragment_look_around.*
import kotlinx.android.synthetic.main.layout_look_around_location.view.*
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.LocationScene
import java.util.concurrent.CompletableFuture

class LookAroundFragment : Fragment() {
    private var locationScene: LocationScene? = null

    // One test location.
    private val location1 = NiLocation(
        "",
        "Belfast Castle",
        123f,
        "",
        "54.64276",
        "-5.942225",
        "Test description",
        "https://farm6.staticflickr.com/5580/15249161785_52eca1a13e_b.jpg",
        "Image by antxoa, licensed under CC BY-NC-SA 2.0."
    )

    private val locationList = ArrayList<NiLocation>()

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
        return inflater.inflate(R.layout.fragment_look_around, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        }
        else {
            startAr()
        }
    }

    private fun startAr() {
        // The ARCore session is created here. This is the "main entry point to the ARCore API".
        // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session
        if (asvLookAround.session == null) {
            val arSession = Session(this.activity)
            val config = Config(arSession)

            // LATEST_CAMERA_IMAGE is non-blocking.
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            arSession.configure(config)
            asvLookAround.setupSession(arSession)
        }
        locationScene = LocationScene(this.activity, asvLookAround)
        locationScene!!.setMinimalRefreshing(true)
        locationScene!!.setOffsetOverlapping(true)

        locationList.add(location1)
        setupMarkers()
    }

    /**
     * Create location markers and add them to the ArSceneView.
     */
    private fun setupMarkers() {
        locationList.forEach { location ->
            // Marker graphics must be created as ARCore ViewRenderables.
            val vrFuture = ViewRenderable.builder()
                .setView(this.activity, R.layout.layout_look_around_location)
                .build()

            // ViewRenderable.Builder#build returns a CompleteableFuture containing the view to
            // render. It is processed here.
            CompletableFuture.anyOf(vrFuture)
                .handle<Any> { _, exception ->
                    if(exception != null) return@handle null
                    else {
                        val locNode = loadNode(location, vrFuture)
                        val locationMarker = LocationMarker(
                            location.long.toDouble(),
                            location.lat.toDouble(),
                            locNode
                        )
                        locationScene?.mLocationMarkers?.add(locationMarker)
                        locationMarker.anchorNode?.isEnabled = true
                        locationMarker.scalingMode = LocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN
                        locationMarker.scaleModifier = 0.75f // Size reduced.

                        // Distance calculated in the marker's render event. Previously it was
                        // calculated in "loadNode" but this caused instances where the marker
                        // would not display at all.
                        locationMarker.setRenderEvent { updateDistance(location, vrFuture.get()) }

                        locationScene?.refreshAnchors()
                    }
                }

            //
            asvLookAround.scene.addOnUpdateListener {
                val arFrame = asvLookAround.arFrame
                if(arFrame!!.camera.trackingState == TrackingState.TRACKING) {
                    locationScene!!.processFrame(arFrame)
                }
            }
        }
    }

    /**
     * Load the Node which will contain the graphic and details for the marker.
     */
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

    private fun updateDistance(location: NiLocation, lookAroundRenderable: ViewRenderable) {
        val locationDistance = Utils.getHaversineGCD(
            locationScene!!.deviceLocation!!.currentBestLocation!!.latitude,
            locationScene!!.deviceLocation!!.currentBestLocation!!.longitude,
            location.lat.toDouble(),
            location.long.toDouble()
        )

        // TODO: Add check for user's distance measurement preference. Using imperial for now.
        val formattedDistance = Utils.DISTANCE_FORMATTER
            .format(Utils.distanceToImperial(locationDistance))

        lookAroundRenderable.view.distance.text = formattedDistance + " miles"
    }

    private fun requestCameraPermission() {
        if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(context!!)
                .setTitle("Camera access requested")
                .setMessage("Camera access is requested to enable the AR functionality.")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    requestPermissions(arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog?.dismiss() }
                .create()
                .show()
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
            AlertDialog.Builder(context!!)
                .setTitle("Caution")
                .setMessage("Camera is required for this function.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog?.dismiss() }
                .show()
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
}
