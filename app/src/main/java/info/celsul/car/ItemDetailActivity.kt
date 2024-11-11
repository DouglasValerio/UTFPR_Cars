package info.celsul.car

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import info.celsul.car.databinding.ActivityItemDetailBinding
import info.celsul.car.model.CarItemDetail
import info.celsul.car.service.RetrofitClient
import info.celsul.car.service.safeApiCall
import info.celsul.car.service.Result
import info.celsul.car.ui.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var carItem: CarItemDetail
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        loadItem()
        setupGoogleMap()

    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle(R.string.car_detail)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteCTA.setOnClickListener {
            deleteItem()
        }
        binding.editCTA.setOnClickListener {
            editItem()
        }
    }

    private fun loadItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getItem(itemId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {}
                    is Result.Success -> {
                        carItem = result.data
                        handleSuccess()
                    }
                }
            }
        }
    }

    private fun editItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.updateItem(
                    carItem.id,
                    carItem.value.copy(licence = binding.licensePlate.text.toString())
                )
            }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.unknown_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Result.Success -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.success_update,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.deleteItem(carItem.id) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.error_delete,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.success_delete,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleSuccess() {
        binding.name.text = "${carItem.value.name}"
        binding.manufacturingYear.text = getString(R.string.item_year, carItem.value.year)
        binding.licensePlate.setText(carItem.value.licence)
        binding.image.loadUrl(carItem.value.imageUrl)
        loadItemLocationInGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (::carItem.isInitialized) {
            loadItemLocationInGoogleMap()
        }
    }

    private fun loadItemLocationInGoogleMap() {
        carItem.value.place?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val latLng = LatLng(it.lat, it.long)
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Local")
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng,
                    17f
                )
            )
        }
    }

    companion object {

        private const val ARG_ID = "ARG_ID"

        fun newIntent(
            context: Context,
            itemId: String
        ) =
            Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
    }
}