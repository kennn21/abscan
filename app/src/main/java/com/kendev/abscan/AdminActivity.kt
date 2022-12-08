package com.kendev.abscan

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.kendev.abscan.model.ClassModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class AdminActivity : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    lateinit var selected: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        db = Firebase.firestore
        val classes = db.collection("classes")
        val tvClassToday = findViewById<TextView>(R.id.tvClassToday)
        val tvCurrentCode = findViewById<TextView>(R.id.tvCurrentCode)
        val ButtonRandomizeCode = findViewById<GridLayout>(R.id.ButtonRandomizeCode)
        val ivQr = findViewById<ImageView>(R.id.ivQr)
        val BackButton = findViewById<GridLayout>(R.id.button_back_to_main)

        var classList: MutableList<ClassModel> = mutableListOf<ClassModel>()
        classes.get()
            .addOnSuccessListener { result ->
                for (cls in result) {
                    var dateAndTime = getDateTime()
                    var convDate = dateAndTime.toString().split("\\s".toRegex()).toTypedArray()
                    if (
                        cls.data["day"].toString().trim() == convDate[0].trim()
                    ) {
                        classList.add(
                            ClassModel(
                                id = cls.id,
                                name = cls.data["name"].toString(),
                                day = cls.data["day"].toString()
                            )
                        )
                        createRadioSelection(classList, tvClassToday, tvCurrentCode)
                        classList.clear()
                    }
//                        AlertDialog.Builder(this)
//                        .setMessage("${cls.id} => ${cls.data["code"]}")
//                            .setMessage(cls.data["day"].toString())
//                            .create()
//                            .show()
                }
            }

        BackButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        ButtonRandomizeCode.setOnClickListener {
            randomizeCode(
                db = db,
                tvClassToday = tvClassToday,
                tvCurrentCode = tvCurrentCode,
                ivQr = ivQr,
                selected = selected
            )
        }

    }

    private fun createRadioSelection(
        classList: MutableList<ClassModel>,
        tvClassToday: TextView,
        tvCurrentCode: TextView
    ) {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_enabled)
            ), intArrayOf(
                R.color.light_gray,  // disabled
                R.color.blue // enabled
            )
        )

        val rgp = findViewById<View>(R.id.ClassChoices) as RadioGroup
        classList.map { c ->
            val rbn = RadioButton(this)
            rbn.id = View.generateViewId()
            rbn.text = "${c.name}"
            rbn.setTextColor(Color.BLACK)
            rbn.buttonTintList = colorStateList
            rbn.setOnClickListener {
                selected = c.id
                showSelectedClassInfo(tvClassToday, tvCurrentCode)
            }
            rgp.addView(rbn)
        }
    }

    private fun showSelectedClassInfo(tvClassToday: TextView, tvCurrentCode: TextView) {
        val classes = db.collection("classes")
        classes.get()
            .addOnSuccessListener { result ->
                for (cls in result) {
                    var dateAndTime = getDateTime()
                    var convDate = dateAndTime.toString().split("\\s".toRegex()).toTypedArray()
                    if (
                        cls.data["day"].toString().trim() == convDate[0].trim() &&
                        cls.id == selected
                    ) {
                        tvClassToday.text = cls.data["name"].toString()
                        tvCurrentCode.text = cls.data["code"].toString()
                    }
                }
            }
    }

    private fun getDateTime(): String? {
        val dateFormat: DateFormat = SimpleDateFormat("EEEE HH:mm:ss")
        val date = Date()
        return dateFormat.format(date)
    }

    private fun randomizeCode(
        db: FirebaseFirestore,
        tvClassToday: TextView,
        tvCurrentCode: TextView,
        ivQr: ImageView,
        selected: String
    ) {

        val classes = db.collection("classes")
        classes.get()
            .addOnSuccessListener { result ->
                for (cls in result) {
                    var dateAndTime = getDateTime()
                    var convDate = dateAndTime.toString().split("\\s".toRegex()).toTypedArray()
                    if (
                        cls.data["day"].toString().trim() == convDate[0].trim() &&
                        cls.id == selected
                    ) {
                        var newCode = randomAlphaNumericString(10)
                        var newQr = generateQr(newCode)

                        db.collection("classes").document(cls.id).update("code", newCode)
                        ivQr.setImageBitmap(newQr)
                        refreshTextViews(tvClassToday, tvCurrentCode, convDate[0].trim())
                    }
//                        AlertDialog.Builder(this)
//                        .setMessage("${cls.id} => ${cls.data["code"]}")
//                            .setMessage(cls.data["day"].toString())
//                            .create()
//                            .show()
                }
            }
    }

    private fun refreshTextViews(tvClassToday: TextView, tvCurrentCode: TextView, day: String) {
        val classes = db.collection("classes")
        classes.get()
            .addOnSuccessListener { result->
                for(cls in result){
                    if(cls.id == selected &&
                        cls.data["day"].toString().trim() == day
                    ){
                        tvClassToday.text = cls.data["name"].toString()
                        tvCurrentCode.text = cls.data["code"].toString()
                    }
                }
            }
    }

    private fun randomAlphaNumericString(desiredStrLength: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return (1..desiredStrLength)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private fun generateQr(code: String): Bitmap? {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(code, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            return bmp

        } catch (e: WriterException) {
            return e.toString() as Bitmap
        }
    }
}