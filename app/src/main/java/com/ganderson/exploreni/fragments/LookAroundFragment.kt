package com.ganderson.exploreni.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.ganderson.exploreni.R
import com.ganderson.exploreni.models.Location
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
    private var loadingFinished = false

    private val location1 = Location(
        "", "Belfast Castle", 123f, "",
        "54.64276", "-5.942225"
    )
    private val locationList = ArrayList<Location>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_look_around, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(asvLookAround.session == null) {
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

    private fun setupMarkers() {
        locationList.forEach { location ->
            val vrFuture = ViewRenderable.builder()
                .setView(this.activity, R.layout.layout_look_around_location)
                .build()

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
                        locationScene?.refreshAnchors()
                        loadingFinished = true
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

    private fun loadNode(location: Location,
                         lookAroundLayoutFuture: CompletableFuture<ViewRenderable>): Node {
        val node = Node()
        node.renderable = lookAroundLayoutFuture.get()

        val nodeLayout = lookAroundLayoutFuture.get().view
        val locationName = nodeLayout.name

        locationName.text = location.name

        nodeLayout.setOnTouchListener { _, _ ->
            Toast.makeText(this.activity, location.name, Toast.LENGTH_SHORT).show()
            false
        }

        return node
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
