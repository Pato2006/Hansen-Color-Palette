package com.example.hansen_color_palette

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var hueBar: SeekBar
    private lateinit var saturationBar: SeekBar
    private lateinit var brightnessBar: SeekBar
    private lateinit var colorPreview: ImageView
    private lateinit var saveButton: Button
    private lateinit var clearColorsButton: Button
    private lateinit var savedColorsContainer: LinearLayout
    private lateinit var colorInfo: TextView

    private lateinit var paletteButton1: Button
    private lateinit var paletteButton2: Button
    private lateinit var paletteButton3: Button

    private lateinit var clearPalette1: Button
    private lateinit var clearPalette2: Button
    private lateinit var clearPalette3: Button

    private var hue = 0f
    private var saturation = 1f
    private var brightness = 1f

    private val savedColors = mutableListOf<Int>()
    private val paletteColors = arrayOf(mutableListOf<Int>(), mutableListOf<Int>(), mutableListOf<Int>())
    private val paletteNames = arrayOf("", "", "")

    private lateinit var sharedPrefs: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences("palettes", Context.MODE_PRIVATE)

        hueBar = findViewById(R.id.hueBar)
        saturationBar = findViewById(R.id.saturationBar)
        brightnessBar = findViewById(R.id.brightnessBar)
        colorPreview = findViewById(R.id.colorPreview)
        saveButton = findViewById(R.id.saveButton)
        clearColorsButton = findViewById(R.id.clearColorsButton)
        savedColorsContainer = findViewById(R.id.savedColorsContainer)
        colorInfo = findViewById(R.id.colorInfo)

        paletteButton1 = findViewById(R.id.paletteButton1)
        paletteButton2 = findViewById(R.id.paletteButton2)
        paletteButton3 = findViewById(R.id.paletteButton3)

        clearPalette1 = findViewById(R.id.clearPalette1)
        clearPalette2 = findViewById(R.id.clearPalette2)
        clearPalette3 = findViewById(R.id.clearPalette3)

        // ----------- APLICAR TU DRAWABLE REDONDEADO AL IMAGEVIEW ----------- //
        colorPreview.setBackgroundResource(R.drawable.redondeado)
        // ------------------------------------------------------------------- //

        loadPalettes()

        hueBar.max = 360
        saturationBar.max = 100
        brightnessBar.max = 100

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

        saveButton.setOnClickListener {
            val color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
            saveColor(color)
        }

        clearColorsButton.setOnClickListener {
            savedColors.clear()
            savedColorsContainer.removeAllViews()
        }

        paletteButton1.setOnClickListener { handlePaletteButton(0, paletteButton1) }
        paletteButton2.setOnClickListener { handlePaletteButton(1, paletteButton2) }
        paletteButton3.setOnClickListener { handlePaletteButton(2, paletteButton3) }

        clearPalette1.setOnClickListener { clearPalette(0, paletteButton1) }
        clearPalette2.setOnClickListener { clearPalette(1, paletteButton2) }
        clearPalette3.setOnClickListener { clearPalette(2, paletteButton3) }

        updateColor()
    }

    private fun updateColor() {
        val color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))

        // Obtener el drawable del fondo
        val bg = colorPreview.background.mutate()

        if (bg is GradientDrawable) {
            bg.setColor(color)

            // Borde negro de 2dp
            val borderWidth = (1 * resources.displayMetrics.density).toInt()
            bg.setStroke(borderWidth, Color.BLACK)
        }

        val hex = String.format("#%06X", 0xFFFFFF and color)
        colorInfo.text = "HEX: $hex | DEC: ${color and 0xFFFFFF}"

        updateHueGradient()
        updateSaturationGradient()
        updateBrightnessGradient()
    }

    private fun updateHueGradient() {
        val hueColors = IntArray(361) { i -> Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f)) }
        val gradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, hueColors)
        hueBar.progressDrawable = gradient
    }

    private fun updateSaturationGradient() {
        val startColor = Color.HSVToColor(floatArrayOf(hue, 0f, brightness))
        val endColor = Color.HSVToColor(floatArrayOf(hue, 1f, brightness))
        val gradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(startColor, endColor))
        saturationBar.progressDrawable = gradient
    }

    private fun updateBrightnessGradient() {
        val startColor = Color.HSVToColor(floatArrayOf(hue, saturation, 0f))
        val endColor = Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
        val gradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(startColor, endColor))
        brightnessBar.progressDrawable = gradient
    }

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

        savedColorsContainer.addView(container)
    }

    private fun handlePaletteButton(index: Int, button: Button) {
        if (paletteColors[index].isEmpty()) {
            val editText = EditText(this)
            editText.hint = "Nombre de la paleta"

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 10)
            layout.addView(editText)

            android.app.AlertDialog.Builder(this)
                .setTitle("Guardar paleta")
                .setView(layout)
                .setPositiveButton("Guardar") { _, _ ->
                    val name = editText.text.toString()
                    if (name.isNotEmpty()) {
                        paletteColors[index].clear()
                        paletteColors[index].addAll(savedColors)
                        paletteNames[index] = name
                        button.text = name
                        savePalettesToPrefs()
                        showPalettePopup(index)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            showPalettePopup(index)
        }
    }

    private fun showPalettePopup(index: Int) {
        val colorsLayout = LinearLayout(this)
        colorsLayout.orientation = LinearLayout.VERTICAL
        colorsLayout.setPadding(30, 30, 30, 30)

        paletteColors[index].forEach { color ->
            val colorView = View(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            )
            params.setMargins(0, 10, 0, 10)
            colorView.layoutParams = params
            colorView.setBackgroundColor(color)

            colorView.setOnClickListener {
                val hex = String.format("#%06X", 0xFFFFFF and color)
                copyToClipboard(hex)
            }

            colorsLayout.addView(colorView)
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Colores guardados")
            .setView(colorsLayout)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun clearPalette(index: Int, button: Button) {
        paletteColors[index].clear()
        paletteNames[index] = ""
        button.text = "Guardar paleta"
        savePalettesToPrefs()
        Toast.makeText(this, "Paleta borrada", Toast.LENGTH_SHORT).show()
    }

    private fun savePalettesToPrefs() {
        val editor = sharedPrefs.edit()
        editor.putString("paletteColors", gson.toJson(paletteColors.map { it.toList() }))
        editor.putString("paletteNames", gson.toJson(paletteNames.toList()))
        editor.apply()
    }

    private fun loadPalettes() {
        val colorsJson = sharedPrefs.getString("paletteColors", null)
        val namesJson = sharedPrefs.getString("paletteNames", null)

        if (!colorsJson.isNullOrEmpty() && !namesJson.isNullOrEmpty()) {
            val typeColors = object : TypeToken<List<List<Int>>>() {}.type
            val typeNames = object : TypeToken<List<String>>() {}.type
            val loadedColors: List<List<Int>> = gson.fromJson(colorsJson, typeColors)
            val loadedNames: List<String> = gson.fromJson(namesJson, typeNames)

            for (i in 0..2) {
                paletteColors[i].clear()
                paletteColors[i].addAll(loadedColors.getOrNull(i) ?: emptyList())
                paletteNames[i] = loadedNames.getOrNull(i) ?: ""
            }

            paletteButton1.text = if (paletteNames[0].isNotEmpty()) paletteNames[0] else "Guardar paleta"
            paletteButton2.text = if (paletteNames[1].isNotEmpty()) paletteNames[1] else "Guardar paleta"
            paletteButton3.text = if (paletteNames[2].isNotEmpty()) paletteNames[2] else "Guardar paleta"
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Color HEX", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copiado al portapapeles: $text", Toast.LENGTH_SHORT).show()
    }
}
