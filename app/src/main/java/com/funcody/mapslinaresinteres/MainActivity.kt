package com.funcody.mapslinaresinteres

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.funcody.mapslinaresinteres.databinding.ActivityMainBinding
import com.funcody.mapslinaresinteres.databinding.InfoWindowLugarBinding
import com.funcody.mapslinaresinteres.models.Lugar
import com.funcody.mapslinaresinteres.retrofit.Repositorio
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tenemos que localizar el id del mapa que hemos añadido. Lo queremos de tipo SupportMapFragment
        val fragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fragment.getMapAsync(this) // this: Cuando se cargue el mapa, que el listener de abajo funcione. override fun onMapReady(map: GoogleMap) {}
    }

    // Librería que lle el texto y reproduce (investigar (texttospeech))
    override fun onMapReady(map: GoogleMap) = recibirLugaresDeInteres(map)

    // obtener todos los lugares de interes
    private fun recibirLugaresDeInteres(map: GoogleMap) {

        val repositorio = Repositorio()
        CoroutineScope(Dispatchers.IO).launch {
            val respuesta = repositorio.getLugares()

            withContext(Dispatchers.Main) {
                if (respuesta.isSuccessful && respuesta.code() == 200) {

                    val arrayLugares = respuesta.body()
                    arrayLugares?.let {
                        configurarMapa(map, arrayLugares)
                    }
                } else {
                    val error = respuesta.errorBody()
                    error?.let {
                        println(error)
                    }
                }
            }
        }
    }

    private fun configurarMapa(map: GoogleMap, arrayLugares: ArrayList<Lugar>) {

        // Tipo de mapa:
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        // Activar el tráfico
        map.isTrafficEnabled = true
        // Creamos una variable que nos permita crear ajustes de mapa
        val uiSettings = map.uiSettings
        // Todos los gestos de la aplicación se habilitan
        uiSettings.isZoomControlsEnabled = true //controles de zoom
        uiSettings.isCompassEnabled = true //mostrar la brújula
        uiSettings.isZoomGesturesEnabled = true //gestos de zoom
        uiSettings.isScrollGesturesEnabled = true //Gestos de scroll
        uiSettings.isTiltGesturesEnabled = true //Gestos de ángulo
        uiSettings.isRotateGesturesEnabled = true //Gestos de rotación
        uiSettings.isMyLocationButtonEnabled = true //botón de localización
        uiSettings.isMapToolbarEnabled = true //barra de herramientas
        uiSettings.isIndoorLevelPickerEnabled = true //selector de niveles

        // Creamos una zona rectangular
        val zona = LatLngBounds.builder()
        // Añadimos las coordenadas de cada lugar
        arrayLugares.forEach { lugar ->

            // Añadimos las coordenadas de cada lugar
            zona.include(LatLng(lugar.lat?.toDouble()!!, lugar.lng?.toDouble()!!))

            // Añadimos el marcador en la ubicación de cada lugar
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(lugar.lat.toDouble(), lugar.lng.toDouble()))
                    .title(lugar.name)
                    .snippet("Pulsa aquí para acceder")
                    .icon(BitmapDescriptorFactory.defaultMarker(insertaColor(lugar.type)))
            )
        }
        // Indicamos la zona rectangular
        map.setOnMapLoadedCallback { // Actúa cuando se ha cargado el mapa está cargado al 100%
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(zona.build(), 20))
        }

        // Añadimos un listener para que cuando pulsemos en un marcador, se muestre una ventana emergente
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoContents(marker: Marker): View {
                val lugar = arrayLugares.find { it.name == marker.title }
                val binding = InfoWindowLugarBinding.inflate(layoutInflater)
                binding.tituloLugar.text = lugar?.name
                binding.direccion.text = lugar?.direccion

                val imagen = "https://qastusoft.com.es/test/estech/android/images/${lugar?.image}.jpg"

                Picasso.get()
                    .load(imagen)
                    // Utiliza un callback que permite realizar una función.
                    .into(binding.imagenLugar, CargaImagen(marker, imagen, binding.imagenLugar))

                return binding.root
            }

            override fun getInfoWindow(p0: Marker): View? {
                return null
            }

            inner class CargaImagen(
                val marker: Marker,
                val imagen: String,
                val imagenLugar: ImageView
            ) : Callback {
                override fun onSuccess() {
                    // Si se está mostrando el InfoWindow ciérralo y la vuelvo a mostrar
                    if (marker.isInfoWindowShown) {
                        marker.hideInfoWindow()
                        Picasso.get().load(imagen).into(imagenLugar)
                        marker.showInfoWindow()
                    }
                }

                override fun onError(e: Exception?) {
                    Log.e("ERROR MAPA", "Error Loading image")
                }
            }
        })

        // Cuando pinchas encima del cartel que sale cuando le pulsas encima del marcador
        map.setOnInfoWindowClickListener { marker ->
            // Obtenemos el objeto del lugar
            val lugar = arrayLugares.filter { lugar -> marker.title == lugar.name }
            // Pasamos el lugar con gson
            val intent = Intent(this, DetalleLugar::class.java)
            intent.putExtra("lugar", Gson().toJson(lugar[0]))
            startActivity(intent)
        }
    }

    private fun insertaColor(type: Int?): Float {
        return when (type) {
            1 -> BitmapDescriptorFactory.HUE_BLUE
            2 -> BitmapDescriptorFactory.HUE_RED
            3 -> BitmapDescriptorFactory.HUE_VIOLET
            4 -> BitmapDescriptorFactory.HUE_GREEN
            else -> BitmapDescriptorFactory.HUE_YELLOW

        }
    }
}