package com.kendev.abscan

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SchedulesActivity : AppCompatActivity() {

    lateinit var list_schedules: ListView
    lateinit var scheduleList: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedules)
        supportActionBar?.hide()

        list_schedules = findViewById(R.id.list_schedules)
        scheduleList = ArrayList()

        scheduleList.add(
            "Web and Mobile Programming"
        )
        scheduleList.add(
            "Server Side Internet Programming"
        )
        scheduleList.add(
            "Computer Networking"
        )
        scheduleList.add(
            "Image Processing and Understanding"
        )
        scheduleList.add(
            "Computer Graphics and Animation"
        )
        scheduleList.add(
            "Data Structure and Algorithm"
        )

        val adapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
            this@SchedulesActivity,
            android.R.layout.simple_list_item_1,
            scheduleList as List<String?>
        )

        val db = Firebase.firestore
        val classes = db.collection("classes")

        fun getLastNCharsOfString(str: String, n: Int): String? {
            var lastnChars = str
            if (lastnChars.length > n) {
                lastnChars = lastnChars.substring(lastnChars.length - n, lastnChars.length)
            }
            return lastnChars
        }

        val button_back_to_main = findViewById<GridLayout>(R.id.button_back_to_main)
        button_back_to_main.setOnClickListener(){
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        list_schedules.adapter = adapter
        list_schedules.setOnItemClickListener { parent, view, position, id ->
            classes.get()
                .addOnSuccessListener(){ result ->
                    for(cls in result){
                        var clsID = getLastNCharsOfString(cls.data["code"].toString(), 1);
                            if((id+1).toString() == clsID){
                                AlertDialog.Builder(this)
                                    .setMessage("${cls.data["name"]}\n" +
                                            "${cls.data["day"]}\n" +
                                            "Starts at: ${cls.data["time_start"]}\n" +
                                            "Ends at: ${cls.data["time_end"]}\n")
                                    .setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
                                    })
                                    .create()
                                    .show()
                            }
                    }
                }
        }
    }
}