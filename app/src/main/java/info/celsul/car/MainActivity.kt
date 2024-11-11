package info.celsul.car

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import info.celsul.car.adapter.CarItemAdapter
import info.celsul.car.database.DatabaseBuilder
import info.celsul.car.database.model.UserLocation
import info.celsul.car.databinding.ActivityMainBinding
import info.celsul.car.model.CarItem
import info.celsul.car.service.RetrofitClient
import info.celsul.car.service.safeApiCall
import info.celsul.car.service.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()
        setupView()

        // 1- Criar tela de Login com algum provedor do Firebase (Telefone, Google)
        //      Cadastrar o Seguinte celular para login de test: +5511912345678
        //      Código de verificação: 101010

        // 2- Criar Opção de Logout no aplicativo - OK

        // 3- Integrar API REST /car no aplicativo
        //      API será disponibilida no Github
        //      JSON Necessário para salvar e exibir no aplicativo
        //      O Image Url deve ser uma foto armazenada no Firebase Storage
        //      { "id": "001", "imageUrl":"https://image", "year":"2020/2020", "name":"Gaspar", "licence":"ABC-1234", "place": {"lat": 0, "long": 0} }

        // Opcionalmente trabalhar com o Google Maps ara enviar o place

    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                onLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            fetchItems()
        }
        binding.addCta.setOnClickListener {
            startActivity(NewItemActivity.newIntent(this))
        }
        supportActionBar?.setTitle("Aluno: Douglas Valério")

    }

    private fun requestLocationPermission() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }

        checkLocationPermissionAndRequest()
    }

    private fun checkLocationPermissionAndRequest() {
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val userLocation = UserLocation(latitude = location.latitude, longitude = location.longitude)
                Log.d("HELLO_WORLD", "Lat: ${userLocation.latitude} Long: ${userLocation.longitude}")
                CoroutineScope(Dispatchers.IO).launch {
                    DatabaseBuilder.getInstance()
                        .userLocationDao()
                        .insert(userLocation)
                }
            } else {
                Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getItems() }

            // Alterando execução para Main thread
            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Error -> {}
                    is Result.Success -> handleOnSuccess(result.data)
                }
            }
        }
    }

    private fun handleOnSuccess(data: List<CarItem>) {
        val adapter = CarItemAdapter(data) {
            startActivity(ItemDetailActivity.newIntent(
                this,
                it.id
            ))
        }
        binding.recyclerView.adapter = adapter
    }

    private fun onLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
