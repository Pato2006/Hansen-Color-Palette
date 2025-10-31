package com.example.hansen_color_palette

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Referencias a los elementos visuales
    private lateinit var hueBar: SeekBar
    private lateinit var saturationBar: SeekBar
    private lateinit var brightnessBar: SeekBar
    private lateinit var colorPreview: ImageView
    private lateinit var saveButton: Button
    private lateinit var savedColorsContainer: LinearLayout

    // Variables HSV
    private var hue = 0f
    private var saturation = 1f
    private var brightness = 1f

    // Lista de colores guardados (máximo 3)
    private val savedColors = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Asignación de vistas
        hueBar = findViewById(R.id.hueBar)
        saturationBar = findViewById(R.id.saturationBar)
        brightnessBar = findViewById(R.id.brightnessBar)
        colorPreview = findViewById(R.id.colorPreview)
        saveButton = findViewById(R.id.saveButton)
        savedColorsContainer = findViewById(R.id.savedColorsContainer)

        // Configuración de rangos
        hueBar.max = 360
        saturationBar.max = 100
        brightnessBar.max = 100

        // Listener para los sliders
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar?.id) {
                    R.id.hueBar -> hue = progress.toFloat()
                    R.id.saturationBar -> saturation = progress / 100f
                    R.id.brightnessBar -> brightness = progress / 100f
                }
                updateColor()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        hueBar.setOnSeekBarChangeListener(listener)
        saturationBar.setOnSeekBarChangeListener(listener)
        brightnessBar.setOnSeekBarChangeListener(listener)

        // Acción del botón GUARDAR
        saveButton.setOnClickListener {
            val color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
            val hex = String.format("#%06X", 0xFFFFFF and color)
            copyToClipboard(hex) // Copia el color al portapapeles
            saveColor(color)
        }

        updateColor()
    }

    // Actualiza el color del cuadrado principal
    private fun updateColor() {
        val color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
        colorPreview.setBackgroundColor(color)
    }

    // Guarda el color en la lista (máximo 3, se borra el primero si hay más)
    private fun saveColor(color: Int) {
        if (savedColors.size >= 3) {
            savedColors.removeAt(0) // Elimina el más antiguo
            savedColorsContainer.removeViewAt(0)
        }

        savedColors.add(color)

        val colorView = View(this)
        val params = LinearLayout.LayoutParams(0, 150)
        params.weight = 1f
        params.setMargins(10, 0, 10, 0)
        colorView.layoutParams = params
        colorView.setBackgroundColor(color)

        // Al tocar el color guardado, se muestra y copia el HEX
        colorView.setOnClickListener {
            showSavedColor(color)
        }

        savedColorsContainer.addView(colorView)
    }

    // Muestra un color guardado y copia su código al portapapeles
    private fun showSavedColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]

        hueBar.progress = hue.toInt()
        saturationBar.progress = (saturation * 100).toInt()
        brightnessBar.progress = (brightness * 100).toInt()

        updateColor()

        val hex = String.format("#%06X", 0xFFFFFF and color)
        copyToClipboard(hex)
        Toast.makeText(this, "Copiado: $hex", Toast.LENGTH_SHORT).show()
    }

    // Copia texto al portapapeles
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Color HEX", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copiado al portapapeles: $text", Toast.LENGTH_SHORT).show()
    }
}
