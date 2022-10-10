package com.kendev.abscan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.model.LocalTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.kendev.abscan.others.SharedVariables
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val scanButton:LinearLayout = findViewById(R.id.button_checkin)
        scanButton.setOnClickListener({
            val intentIntegrator = IntentIntegrator(this)

            intentIntegrator.setDesiredBarcodeFormats(listOf(IntentIntegrator.QR_CODE))
            intentIntegrator.initiateScan()
        })

        val button_signout = findViewById<GridLayout>(R.id.button_signout)
        button_signout.setOnClickListener(){
            FirebaseAuth.getInstance().signOut();
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        val button_check_schedules: LinearLayout = findViewById(R.id.button_check_schedules)
        button_check_schedules.setOnClickListener(){
            val intent = Intent(this, SchedulesActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var result = IntentIntegrator.parseActivityResult(resultCode, data)
        if (result != null){
//            AlertDialog.Builder(this)
//                .setMessage("Would you like to go to ${result.contents}?")
//                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->
//                    val intent = Intent(Intent.ACTION_WEB_SEARCH)
//                    intent.putExtra(SearchManager.QUERY,result.contents)
//                    startActivity(intent)
//                })
//                .setNegativeButton("No",DialogInterface.OnClickListener { dialogInterface, i ->  })
//                .create()
//                .show()
            doAttendance(result.contents.toString())
        }
    }

    private fun getDateTime(): String? {
        val dateFormat: DateFormat = SimpleDateFormat("EEEE HH:mm:ss")
        val date = Date()
        return dateFormat.format(date)
    }

    private fun doAttendance(code:String) {
        val db = Firebase.firestore
        val classes = db.collection("classes")

        classes.get()
            .addOnSuccessListener { result ->
                for (cls in result) {
                    var dateAndTime = getDateTime()
                    var convDate = dateAndTime.toString().split("\\s".toRegex()).toTypedArray()
                    if (
                        cls.data["code"] == code
                    ) {
                        if(
                            cls.data["day"].toString().trim() == convDate[0].trim()
                        ){
                            val d1 = convDate[1]
                            val d2 = cls.data["time_start"].toString().trim()
                            val d3 = cls.data["time_end"].toString().trim()

                            val sdf = SimpleDateFormat("HH:mm:ss")

                            val currTime: Date = sdf.parse(d1)
                            val startTime: Date = sdf.parse(d2)
                            val endTime: Date = sdf.parse(d3)
                            if(
                                currTime.compareTo(startTime) > 0
                            ){
                                if(
                                    currTime.compareTo(endTime) < 0
                                ){
                                    var className = cls.data["name"].toString()
                                    Log.d("CLASSNAME", className)
                                    SharedVariables().changeName(className)
                                    val intent = Intent(this, presentActivity::class.java)
                                    intent.putExtra("className", className)
                                    startActivity(intent)
                                } else{
                                    Toast.makeText(baseContext,"Now is "+convDate[1]+". You should come before "+ cls.data["time_end"],Toast.LENGTH_LONG).show()
                                }
                            } else{
                                Toast.makeText(baseContext,"Now is "+convDate[1]+". You should come after "+ cls.data["time_start"],Toast.LENGTH_LONG).show()
                            }

                        } else{
                            Toast.makeText(baseContext,"Today is "+convDate[0]+". You should come on "+ cls.data["day"],Toast.LENGTH_LONG).show()
                        }
//                        AlertDialog.Builder(this)
//                        .setMessage("${cls.id} => ${cls.data["code"]}")
//                            .setMessage(cls.data["day"].toString())
//                            .create()
//                            .show()
                    }
                }
            }
    }
    }