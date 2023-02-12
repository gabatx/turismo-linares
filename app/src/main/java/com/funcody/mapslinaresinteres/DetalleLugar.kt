package com.funcody.mapslinaresinteres

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.funcody.mapslinaresinteres.databinding.ActivityDetalleLugarBinding
import com.funcody.mapslinaresinteres.models.Lugar
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class DetalleLugar : AppCompatActivity() {
    lateinit var binding: ActivityDetalleLugarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleLugarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibimos el objeto lugar
        val lugar = Gson().fromJson(intent.getStringExtra("lugar"), Lugar::class.java)
        // Insertamos el toolbar como actionbar
        setSupportActionBar(binding.toolbar)
        // Introducimos el nombre del lugar en el toolbar
        title = lugar?.name
        // Habilitamos el boton de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
       // Cargamos los datos del lugar
        inflarDatos(lugar)

        // Validamos si el lugar tiene teléfono
        if (lugar?.telefono.isNullOrEmpty()) {
            binding.botonLlamarDetalle.visibility = View.GONE
        }

        // Validamos si el lugar tiene web
        if (lugar?.web.isNullOrEmpty()) {
            binding.botonWebDetalle.visibility = View.GONE
        }

        // Validamos si el lugar tiene dirección
        if (lugar?.direccion.isNullOrEmpty()) {
            binding.direccionDetalle.visibility = View.GONE
        }

        // Validamos si el lugar tiene foto
        if (lugar?.image.isNullOrEmpty()) {
            binding.botonGaleriaDetalle.visibility = View.GONE
        }

    }

    // La función sirve de evento al pulsar un item del toolbar superior
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else              -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun inflarDatos(lugar: Lugar?) {


        // Cargamos la imagen
        val imagen = "https://qastusoft.com.es/test/estech/android/images/${lugar?.image}.jpg"
        if (lugar?.image != null) {
            Picasso.get()
                .load(imagen)
                .into(binding.imagenDetalle)
        } else {
            binding.imagenDetalle.setImageResource(R.drawable.imagen_no_encontrada)
        }

        // Cargamos la dirección
        binding.direccionDetalle.text = lugar?.direccion
        // Cargamos el telefono
        binding.telefonoDetalle.text = lugar?.telefono
        // Cargamos el horario
        binding.horarioDetalle.text = lugar?.horario
        // Cargamos la descripción
        binding.descripcionDetalle.text = lugar?.texto

        // asignamos el listener al boton de llamar
        binding.botonLlamarDetalle.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${lugar?.telefono}")
            startActivity(intent)
        }

        // asignamos el listener al boton de direccionar
        binding.botonWebDetalle.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(lugar?.web)
            startActivity(intent)
        }

        // asignamos el listener al botón de ubicación
        binding.botonUbicacionDetalle.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("geo:${lugar?.lat},${lugar?.lng}")
            startActivity(intent)
        }

        // asignamos el listener al botón de compartir
        binding.botonGaleriaDetalle.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("content://media/external/images/media/${lugar?.image}")
            startActivity(intent)
        }

    }
}