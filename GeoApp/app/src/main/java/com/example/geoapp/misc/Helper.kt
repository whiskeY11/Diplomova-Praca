package com.example.geoapp.misc

import com.mapbox.mapboxsdk.geometry.LatLng
import com.example.geoapp.R
import android.content.DialogInterface
import android.view.LayoutInflater
import com.example.geoapp.map.MapImages
import com.example.geoapp.map.MapHelper
import android.widget.AdapterView.OnItemSelectedListener
import android.annotation.SuppressLint
import com.mapbox.geojson.LineString
import android.util.TypedValue
import android.graphics.Bitmap
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.graphics.drawable.VectorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.PictureDrawable
import android.os.Handler
import android.view.View
import android.widget.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import java.lang.IllegalArgumentException
import java.text.Normalizer

class Helper {
    private var callback: Handler? = null
    private var context: Context? = null
    private var progressDialog: AlertDialog? = null
    private var addDialog: AlertDialog? = null
    private var editDialog: AlertDialog? = null
    private var progressDialogText: TextView? = null
    private var baseText = ""
    private var currentProgress = 0
    private var editTextDialogName = ""
    var addingLatLng: LatLng? = null
    private var editTextDialogInput: EditText? = null
    private var addTextDialogInput: EditText? = null
    private var addSpinner: Spinner? = null
    private var editSpinner: Spinner? = null
    var selectedIcon: String? = null
        private set
    var editPointId = -1
    private var isInitialized = false

    fun Setup(callback: Handler?, context: Context?) {
        if (instance != null) {
            this.callback = callback
            this.context = context
            isInitialized = true
        }
    }

    fun instantiateInfoDialog(context: Context?, header: String?, text: String?, callbackCode: Int, cancelable: Boolean) {
        if (!isInitialized) return
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(header)
        alertDialog.setMessage(text)
        alertDialog.setIcon(R.drawable.ic_baseline_info_24)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            if (callback != null && callbackCode != -1) {
                callback!!.sendEmptyMessage(callbackCode)
            }
        }
        alertDialog.setCanceledOnTouchOutside(cancelable);
        alertDialog.show()
    }

    fun instantiateConfirmDialog(header: String?, text: String?, callbackCodeConfirm: Int, callbackCodeCancel: Int) {
        if (!isInitialized) return
        AlertDialog.Builder(context)
                .setTitle(header)
                .setMessage(text)
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setPositiveButton("Áno") { dialog: DialogInterface, whichButton: Int ->
                    dialog.dismiss()
                    if (callback != null) {
                        callback!!.sendEmptyMessage(callbackCodeConfirm)
                    }
                }
                .setNegativeButton("Nie") { dialog: DialogInterface, whichButton: Int ->
                    dialog.dismiss()
                    if (callback != null && callbackCodeCancel != -1) {
                        callback!!.sendEmptyMessage(callbackCodeCancel)
                    }
                }.show()
    }

    fun createAllDialogs() {
        if (!isInitialized) return
        createProgressDialog("Sťahovanie dát...")
        createEditTextDialog(0, "Pridať", "Zadajte názov a vyberte ikonu")
        createEditTextDialog(1, "Upraviť", "Zadajte názov a vyberte ikonu")
    }

    fun createProgressDialog(text: String) {
        if (!isInitialized) return
        currentProgress = 0
        baseText = text
        val dialogBuilder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        progressDialogText = dialogView.findViewById<View>(R.id.loading_msg) as TextView
        progressDialogText!!.text = text
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)
        progressDialog = dialogBuilder.create()
    }

    fun createEditTextDialog(type: Int, text: String?, text2: String?) {
        if (!isInitialized) return
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle(text)
        dialogBuilder.setMessage(text2)
        val mapImages = MapHelper.getInstance().mapImages
        val adapter = ArrayAdapter(
                context, android.R.layout.simple_spinner_item, mapImages.iconNames())
        val alertView = LayoutInflater.from(context).inflate(R.layout.edit_dialog, null)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val sItems = alertView.findViewById<Spinner>(R.id.iconDropdown)
        sItems.adapter = adapter
        sItems.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                val selected = sItems.getItemAtPosition(position).toString()
                val placeholder = alertView.findViewById<ImageView>(R.id.iconPlaceholder)
                if (mapImages.getIcon(selected) != null) {
                    placeholder.setImageBitmap(mapImages.getIcon(selected))
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        if (type == 0) {
            dialogBuilder.setIcon(R.drawable.ic_add_dialog)
            addTextDialogInput = alertView.findViewById(R.id.nameInput)
            addSpinner = sItems
        } else {
            dialogBuilder.setIcon(R.drawable.ic_edit_dialog)
            editTextDialogInput = alertView.findViewById(R.id.nameInput)
            editSpinner = sItems
        }
        dialogBuilder.setView(alertView)
        dialogBuilder.setPositiveButton("OK"
        ) { dialog: DialogInterface?, which: Int ->
            if (type == 0) {
                editTextDialogName = addTextDialogInput!!.text.toString()
                selectedIcon = addSpinner!!.selectedItem.toString()
            } else {
                editTextDialogName = editTextDialogInput!!.text.toString()
                selectedIcon = editSpinner!!.selectedItem.toString()
            }
            if (editTextDialogName != "") {
                if (callback != null) {
                    if (type == 0) callback!!.sendEmptyMessage(1) else callback!!.sendEmptyMessage(2)
                }
            } else {
                Toast.makeText(context,
                        "Prázdne meno!", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton("SPÄŤ"
        ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        dialogBuilder.setOnDismissListener { dialogInterface: DialogInterface? ->
            if (callback != null) {
                callback!!.sendEmptyMessage(7)
            }
        }
        if (type == 0) addDialog = dialogBuilder.create() else editDialog = dialogBuilder.create()
    }

    fun setProgressDialogActive(show: Boolean) {
        if (!isInitialized) return
        if (progressDialog == null) {
            println("ProgressDialog is not instantiated!")
            return
        }
        if (show) progressDialog!!.show() else progressDialog!!.dismiss()
    }

    @SuppressLint("SetTextI18n")
    fun appendProgress(value: Int) {
        currentProgress += value
        progressDialogText!!.text = "$baseText $currentProgress%"
    }

    fun setProgressDialogText(text: String) {
        currentProgress = 0
        baseText = text
        progressDialogText!!.text = text
    }

    fun showAddDialog() {
        if (!isInitialized) return
        if (addDialog == null) {
            println("AddDialog is not instantiated!")
            return
        }
        addTextDialogInput!!.setText("")
        addSpinner!!.setSelection(0)
        addDialog!!.show()
    }

    fun showEditDialog(name: String?, icon: String?) {
        if (!isInitialized) return
        if (editDialog == null) {
            println("EditDialog is not instantiated!")
            return
        }
        editTextDialogInput!!.setText(name)
        editSpinner!!.setSelection((editSpinner!!.adapter as ArrayAdapter<String?>).getPosition(icon))
        editDialog!!.show()
    }

    fun onIconDataChanged() {
        var shown = false
        if (addDialog != null && addDialog!!.isShowing) {
            addDialog!!.dismiss()
            shown = true
        }
        if (editDialog != null && editDialog!!.isShowing) {
            addDialog!!.dismiss()
            shown = true
        }
        createAllDialogs()
        if (callback != null && shown) {
            callback!!.sendEmptyMessage(8)
        }
    }

    fun getEditTextDialogInput(): String {
        return editTextDialogName
    }

    fun convertToLatLng(feature: Feature): LatLng {
        var symbolPoint: Point? = null
        when (feature.geometry()!!.type()) {
            "Point" -> symbolPoint = feature.geometry() as Point?
            "LineString" -> {
                val lineString = feature.geometry() as LineString?
                val coordinates = lineString!!.coordinates()
                symbolPoint = coordinates[(coordinates.size - 1) / 2]
            }
            "Polygon" -> {
                val polygon = feature.geometry() as Polygon?
                val coordinates1 = polygon!!.coordinates()
                val helper = coordinates1[0]
                symbolPoint = helper[(helper.size - 1) / 2]
            }
        }
        return LatLng(symbolPoint!!.latitude(), symbolPoint.longitude())
    }

    fun lerp(a: Double, b: Double, f: Double): Double {
        return a * (1.0 - f) + b * f
    }

    fun stripAccents(s: String): String {
        var s = s
        s = Normalizer.normalize(s, Normalizer.Form.NFD)
        s = s.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        return s
    }

    fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context!!.resources.displayMetrics).toInt()
    }

    fun generate(view: View): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)
        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight
        view.layout(0, 0, measuredWidth, measuredHeight)
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    fun getBitmap(context: Context?, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawable) {
            getBitmap(drawable)
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false)
        bm.recycle()
        return resizedBitmap
    }

    fun getBitmapFromPictureDrawable(view: PictureDrawable): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.intrinsicWidth, view.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        view.setBounds(0, 0, canvas.width, canvas.height)
        view.draw(canvas)
        return returnedBitmap
    }

    companion object {
        var instance: Helper? = null
            private set

        @JvmStatic
        fun createInstance(): Boolean {
            return if (instance == null) {
                instance = Helper()
                true
            } else {
                false
            }
        }
    }
}