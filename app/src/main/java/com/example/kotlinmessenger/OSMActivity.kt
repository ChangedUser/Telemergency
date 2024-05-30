package com.example.kotlinmessenger

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_osm.*
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.kml.KmlDocument
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.webrtc.ContextUtils.getApplicationContext


class OSMActivity : AppCompatActivity() {
    private var map: MapView? = null
    private var mapController: IMapController? = null



    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        if (userRole.toString() == "Patient") {
            setTheme(R.style.Theme_TelemergencyPatient)
        }

        super.onCreate(savedInstanceState)

        //handle permissions first, before map is created. not depicted here


        //load/initialize the osmdroid configuration, this can be done
        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_osm)
        if (Build.VERSION.SDK_INT >= 23) {
            if (isStoragePermissionGranted) {
            }
        }
        map = findViewById(R.id.mapView)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        map?.setBuiltInZoomControls(true)
        map?.setMultiTouchControls(true)
        mapController = map?.getController()
        //mapController?.setZoom(15)

        val startPoint = GeoPoint(51496994, -134733)
        mapController?.setCenter(startPoint)

        val mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), map )
        //val mapController = map?.getController()
        mMyLocationOverlay.enableMyLocation()
        //mMyLocationOverlay.disableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                mapController?.animateTo(mMyLocationOverlay.myLocation)
                mapController?.setZoom(18)
            }
        }
        map?.overlays?.add(mMyLocationOverlay)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (map != null) map!!.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    public override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (map != null) map!!.onPause() //needed for compass, my location overlays, v6.0.0 and up
    }

    //permission is automatically granted on sdk<23 upon installation
    val isStoragePermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            run {
                Log.v(TAG, "Permission is granted")
                return true
            }
            run {
                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    1
                )
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            true
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED)
        run { Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]) }
    }



    fun loadKml() {
        KmlLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    companion object {
        private const val TAG = "OsmActivity"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    class KmlLoader : AsyncTask<Void?, Void?, Void?>() {
        var progressDialog: ProgressDialog = ProgressDialog(OSMActivity().applicationContext)
        var kmlDocument: KmlDocument? = null

        protected override fun onPreExecute() {
            super.onPreExecute()
            progressDialog.setMessage("Loading Project...")
            progressDialog.show()
        }

        @SuppressLint("WrongThread")
        protected override fun doInBackground(vararg voids: Void?): Void? {
            kmlDocument = KmlDocument()
            kmlDocument?.parseKMLStream(OSMActivity().applicationContext.getResources().openRawResource(R.raw.study_areas), null)
            val kmlOverlay: FolderOverlay =
                kmlDocument?.mKmlRoot?.buildOverlay(OSMActivity().map, null, null, kmlDocument) as FolderOverlay
            OSMActivity().map?.getOverlays()?.add(kmlOverlay)
            return null
        }

        protected override fun onPostExecute(aVoid: Void?) {
            progressDialog.dismiss()
            OSMActivity().map?.invalidate()
            val bb: BoundingBox = kmlDocument?.mKmlRoot!!.boundingBox
            OSMActivity().map?.zoomToBoundingBox(bb, true)
            //            mapView.getController().setCenter(bb.getCenter());
            super.onPostExecute(aVoid)
        }
    }
}



