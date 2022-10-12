package com.kendev.abscan

import android.content.Intent
import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase


class UserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        auth = Firebase.auth

        val button_back_to_main = findViewById<GridLayout>(R.id.button_back_to_main)
        button_back_to_main.setOnClickListener() {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val currUid = auth?.uid
        readData(currUid.toString())

    }
    private fun readData(uid: String) {
        //Initialize vars
        val tvname = findViewById<TextView>(R.id.name)
        val tvnim = findViewById<TextView>(R.id.nim)
        val tvemail = findViewById<TextView>(R.id.email)

        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(uid).get().addOnSuccessListener {

            if (it.exists()){

                val fullname = it.child("fullname").value
                val nim = it.child("nim").value
                val email = it.child("email").value
//                Toast.makeText(this,"Successfuly Read",Toast.LENGTH_SHORT).show()
                tvname.text = fullname.toString()
                tvnim.text = nim.toString()
                tvemail.text = email.toString()

            }else{

                Toast.makeText(this,"User Doesn't Exist",Toast.LENGTH_SHORT).show()


            }

        }.addOnFailureListener{

            Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()


        }

    }
}

