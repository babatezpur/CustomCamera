package com.example.customcamera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {

    lateinit var editTextName : EditText
    lateinit var editTextEmail : EditText
    lateinit var buttonTakeTest : Button

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val reference : DatabaseReference = database.reference.child("UserData")
    val oldReference : DatabaseReference = database.reference.child("UserData")

    //@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonTakeTest = findViewById(R.id.buttonTakeTest)



        oldReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("Name").exists()) {
                        //If user has already entered name and email before, that is stored in db.
                        val oldName : String = snapshot.child("Name").value as String
                        val oldEmail : String = snapshot.child("Email").value as String
                        editTextName.setText(oldName)
                        editTextEmail.setText(oldEmail)
                    } else {
                        //If user isn't already in db
                        Toast.makeText(applicationContext, "No User in DB. Please enter name and email.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


        buttonTakeTest.setOnClickListener {

            val userName = editTextName.text.toString()
            val userEmail = editTextEmail.text.toString()

            reference.child("Name").setValue(userName)
            reference.child("Email").setValue(userEmail)

            val intent = Intent(this@MainActivity , SecondActivity::class.java)
            startActivity(intent)
        }

    }
}