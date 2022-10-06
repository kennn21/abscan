package com.kendev.abscan

import android.app.AlertDialog
import android.app.SearchManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Build.VERSION_CODES.P
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kendev.abscan.others.SharedVariables
import kotlinx.coroutines.tasks.await
import java.lang.Exception

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

    private fun doAttendance(code:String){
        val db = Firebase.firestore
        val classes = db.collection("classes")

        classes.get()
            .addOnSuccessListener { result ->
                for (cls in result) {
                    if(cls.data["code"] == code){
                        var className = cls.data["name"].toString()
                        Log.d("CLASSNAME", className)
                        SharedVariables().changeName(className)
                        val intent = Intent(this, presentActivity::class.java)
                        intent.putExtra("className", className)
                        startActivity(intent)

                    }
//                    AlertDialog.Builder(this)
//                        .setMessage("${cls.id} => ${cls.data["code"]}")
//                        .create()
//                        .show()
                }
            }

    }
}