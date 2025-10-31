package com.example.hansen_color_palette

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Referencias de interfaz
    private lateinit var hueBar: SeekBar
    private lateinit var saturationBar: SeekBar
    private lateinit var brightnessBar: SeekBar
    private lateinit var colorPreview: ImageView
    private lateinit var saveButton: Button
    private lateinit var savedColorsContainer: LinearLayout
    private lateinit var colorInfo: TextView

    // Valores HSV
    private var hue = 0f
    private var saturation = 1f
    private var brightness = 1f

    // Lista con máximo de 3 colores guardados
    private val savedColors = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de vistas
        hueBar = findViewById(R.id.hueBar)
        saturationBar = findViewById(R.id.saturationBar)
        brightnessBar = findViewById(R.id.brightnessBar)
        colorPreview = findViewById(R.id.colorPreview)
        saveButton = findViewById(R.id.saveButton)
        savedColorsContainer = findViewById(R.id.savedColorsContainer)
        colorInfo = findViewById(R.id.colorInfo)

        // Configurar rangos
        hueBar.max = 360
        saturationBar.max = 100
        brightnessBar.max = 100

        // Listener compartido para las tres barras
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

        // Botón guardar
        saveButton.setOnClickListener {
            val color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
            val hex = String.format("#%06X", 0xFFFFFF and color)
            copyToClipboard(hex)
            saveColor(color)
        }

        updateColor()
    }

    // Actualiza el color principal, texto y degradados de las barras
    private fun updateColor() {
        val color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
        colorPreview.setBackgroundColor(color)

        val hex = String.format("#%06X", 0xFFFFFF and color)
        colorInfo.text = "HEX: $hex | DEC: ${color and 0xFFFFFF}"

        // Actualiza los gradientes de las barras
        updateHueGradient()
        updateSaturationGradient()
        updateBrightnessGradient()
    }

    // Gradiente de tono
    private fun updateHueGradient() {
        val hueColors = IntArray(361) { i ->
            Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
        }
        val gradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, hueColors)
        hueBar.progressDrawable = gradient
    }

    // Gradiente de saturación (del gris al color actual)
    private fun updateSaturationGradient() {
        val startColor = Color.HSVToColor(floatArrayOf(hue, 0f, brightness))
        val endColor = Color.HSVToColor(floatArrayOf(hue, 1f, brightness))
        val gradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(startColor, endColor))
        saturationBar.progressDrawable = gradient
    }

    // Gradiente de brillo (negro al color actual)
    private fun updateBrightnessGradient() {
        val startColor = Color.HSVToColor(floatArrayOf(hue, saturation, 0f))
        val endColor = Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
        val gradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(startColor, endColor))
        brightnessBar.progressDrawable = gradient
    }

    // Guarda el color (máximo 3)
    private fun saveColor(color: Int) {
        if (savedColors.size >= 3) {
            savedColors.removeAt(0)
            savedColorsContainer.removeViewAt(0)
        }

        savedColors.add(color)

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.gravity = Gravity.CENTER
        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1f
        params.setMargins(10, 0, 10, 0)
        container.layoutParams = params

        val colorView = View(this)
        val colorParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120)
        colorView.layoutParams = colorParams
        colorView.setBackgroundColor(color)
        container.addView(colorView)

        val hex = String.format("#%06X", 0xFFFFFF and color)
        val text = TextView(this)
        text.text = "$hex\nDEC: ${color and 0xFFFFFF}"
        text.gravity = Gravity.CENTER
        container.addView(text)

        container.setOnClickListener {
            showSavedColor(color)
        }

        savedColorsContainer.addView(container)
    }

    // Muestra color guardado y copia su HEX
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
    }

    // Copia texto al portapapeles
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Color HEX", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copiado al portapapeles: $text", Toast.LENGTH_SHORT).show()
    }
}
