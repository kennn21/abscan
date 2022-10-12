package com.kendev.abscan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RegisterUser : AppCompatActivity() {
    private lateinit var rdb: DatabaseReference
    private lateinit var auth: FirebaseAuth
    val database = Firebase.database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val wideView = findViewById<ConstraintLayout>(R.id.wideview)
        wideView.apply { post { scrollView.scrollTo(0, (left + right - scrollView.height) / 2) } }
        supportActionBar?.hide()


        // Initialize Firebase Auth
        auth = Firebase.auth

        val button_login:TextView = findViewById(R.id.button_hasaccount)
        button_login.setOnClickListener(){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        val image_loading = findViewById<ProgressBar>(R.id.image_loading)

        //get email, name, nim, pass
        val button_register = findViewById<Button>(R.id.button_signup)
        button_register.setOnClickListener(){
            validateCreds(image_loading)
        }
    }

    private fun validateCreds(loading:View){
        val inputEmail = findViewById<EditText>(R.id.input_email)
        val inputFullname = findViewById<EditText>(R.id.input_fullname)
        val inputNim = findViewById<EditText>(R.id.input_nim)
        val inputPassword = findViewById<EditText>(R.id.input_password)

        val email = inputEmail.text.toString()
        val fullName = inputFullname.text.toString()
        val nim = inputNim.text.toString()
        val password = inputPassword.text.toString()

        if(email.isNotEmpty() || fullName.isNotEmpty() || nim.isNotEmpty() || password.isNotEmpty()){

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
            } else if(fullName.length < 3) {
                Toast.makeText(
                    baseContext, "Name must consist of 3 or more characters!",
                    Toast.LENGTH_SHORT
                ).show()
            } else{
                toggleLoad(loading)
                performSignUp(email, password, fullName, nim)
                toggleLoad(loading)
//                val uid = auth.uid
//                val database = FirebaseDatabase.getInstance().getReference("users")
//                val userInfo:HashMap<String,Any?> = HashMap()
//                userInfo["uid"] = uid
//                userInfo["email"] = email
//                userInfo["fullName"] = fullName
//                userInfo["nim"] = nim
//
//                database.child(uid!!)
//                    .setValue(userInfo)
//                    .addOnSuccessListener {
//                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(this, MainActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    }
//                    .addOnFailureListener(){
//                        Toast.makeText(this, "Failed saving user info", Toast.LENGTH_SHORT).show()
//                    }
//                toggleLoad(loading)
            }
        }

    }

    fun toggleLoad(loading: View){
        loading.visibility = if (loading.visibility == View.VISIBLE){
            View.INVISIBLE
        } else{
            View.VISIBLE
        }
    }

    private fun saveUserInfo(uid:String, fullname:String, email: String, nim:String){
        rdb = FirebaseDatabase.getInstance().getReference("Users")
        val newUser = newUser(uid, fullname, email, nim)
        rdb.child(uid).setValue(newUser).addOnSuccessListener {
            Toast.makeText(this, " Succesfully saved! ", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show()
            }
    }

    private fun performSignUp(email:String,password:String, fullname: String, nim: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful) {
                    // Sign up success

                    //save user info
                    val currUser = auth.currentUser
                    val currUid =  currUser?.uid
                    saveUserInfo( currUid.toString(), fullname, email, nim)
//                    Toast.makeText(this, " ${currUid} ",Toast.LENGTH_SHORT).show()

                    //Redirect to main act
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign up fail, display error
                    Toast.makeText(
                        baseContext, "Registration failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
            .addOnFailureListener(){
                Toast.makeText(this, "Error occurred ${it.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}