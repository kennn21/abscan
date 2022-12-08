package com.kendev.abscan

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.size
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.kendev.abscan.adapter.CustomAdapter
import com.kendev.abscan.model.AttendanceModel
import com.kendev.abscan.model.ItemsViewModel
import com.kendev.abscan.others.SharedVariables
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawer : DrawerLayout

    lateinit var rv_history_attendance: RecyclerView
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawer = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()


        // Initialize Firebase Auth
        auth = Firebase.auth

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val db = Firebase.firestore

        //getuserinfo
        val scanButton:LinearLayout = findViewById(R.id.button_checkin)
        scanButton.setOnClickListener {
            val intentIntegrator = IntentIntegrator(this)
            intentIntegrator.setDesiredBarcodeFormats(listOf(IntentIntegrator.QR_CODE))
            intentIntegrator.setCameraId(0)
            intentIntegrator.setOrientationLocked(false)
            intentIntegrator.initiateScan()
        }

        val button_signout = findViewById<GridLayout>(R.id.button_signout)
        button_signout.setOnClickListener(){
            FirebaseAuth.getInstance().signOut();
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val button_check_schedules: LinearLayout = findViewById(R.id.button_check_schedules)
        button_check_schedules.setOnClickListener(){
            val intent = Intent(this, SchedulesActivity::class.java)
            startActivity(intent)
        }

        rv_history_attendance = findViewById(R.id.rv_history_attendance)

        rv_history_attendance.layoutManager = LinearLayoutManager(this)

        db.collection("attendance_history")
            .get().addOnSuccessListener { result ->

                val db = Firebase.firestore
                db.collection("classes")
                    .get()
                    .addOnSuccessListener { res2->
                        val data = ArrayList<ItemsViewModel>()

                        for(cls in res2){
                            for(atd in result){
                                if(atd.data["class_code"] == cls.data["code"]){
                                    val user = Firebase.auth.currentUser
                                    val currUid = user?.uid
                                    if(atd.data["uid"].toString() == currUid.toString()){
                                        data.add(
                                            ItemsViewModel(
                                            "${cls.data["name"]}" +
                                                    "\n" +
                                                    "${atd.data["date"]}" +
                                                    "\n" +
                                                    "${atd.data["time"]}" +
                                                    "\n" +
                                                    "${getUserEmail()}")
                                        )
                                    }
                                }
                            }

                        }
                        val adapter = CustomAdapter(data)
                        rv_history_attendance.adapter = adapter
                        if(rv_history_attendance.size < 1){
                            data.add(ItemsViewModel("No more attendance found.."))
                        }
                    }

            }
        // This will pass the ArrayList to our Adapter

        // Setting the Adapter with the recyclerview
    }

    fun getUserEmail(): String? {
            val user = Firebase.auth.currentUser
            val email = user?.email
//            Toast.makeText(baseContext, "${email}",Toast.LENGTH_LONG).show()
            return email
    }

    override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_user -> {
                val intent = Intent(this, UserProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_admin -> {
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        } else{
            super.onBackPressed()
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

                                    //Add attendance to database
                                    val class_code = cls.data["code"].toString()
                                    val date = convDate[0]
                                    val time = convDate[1]
                                    val uid = Firebase.auth.currentUser?.uid

                                    val attendanceObj = AttendanceModel(class_code, date, time, uid )
                                    db.collection("attendance_history")
                                        .add(attendanceObj)
                                        .addOnSuccessListener { docRef ->
                                            Log.d(TAG, "DocSnap written w/ id ${docRef.id}")
                                            val intent = Intent(this, PresentActivity::class.java)
                                            intent.putExtra("className", className)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener{ e ->
                                        Log.w(TAG, "Error adding doc", e)
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                        }
                                    val intent = Intent(this, MainActivity::class.java)
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