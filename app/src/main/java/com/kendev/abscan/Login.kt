package com.kendev.abscan

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.*
import com.google.firebase.ktx.Firebase


class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Initialize Firebase Auth
        auth = Firebase.auth
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val button_register: TextView = findViewById(R.id.button_noaccount)
        button_register.setOnClickListener() {
            val intent = Intent(this, RegisterUser::class.java)
            startActivity(intent)
        }

        val image_loading = findViewById<ProgressBar>(R.id.image_loading)

        val button_login:Button = findViewById(R.id.button_login)
        button_login.setOnClickListener(){
            validateCreds(image_loading)
            toggleLoad(image_loading)
        }
    }

    fun toggleLoad(loading: View){
        loading.visibility = if (loading.visibility == View.VISIBLE){
            View.INVISIBLE
        } else{
            View.VISIBLE
        }
    }
    private fun validateCreds(loading: View){
        val inputEmail = findViewById<EditText>(R.id.input_email)
        val inputPassword = findViewById<EditText>(R.id.input_password)

        val email = inputEmail.text.toString()
        val password = inputPassword.text.toString()

        if (email.isNotEmpty() || password.isNotEmpty()){
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(
                    baseContext, "Incorrect email!",
                    Toast.LENGTH_SHORT
                ).show()
            } else if(password.length < 6){
                Toast.makeText(
                    baseContext, "Password must be more than 6 characters!",
                    Toast.LENGTH_SHORT
                ).show()
            } else{
                toggleLoad(loading)
                performSignIn(email, password)
                toggleLoad(loading)
            }
        } else {

            Toast.makeText(
                baseContext, "Please fill in the information",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun performSignIn(email:String, password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }


}