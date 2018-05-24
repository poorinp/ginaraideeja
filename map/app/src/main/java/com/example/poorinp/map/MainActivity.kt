package com.example.poorinp.ginaraideeja

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.example.poorinp.map.MapsActivity
import com.example.poorinp.map.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_register.*

class MainActivity : AppCompatActivity() {

    lateinit var handler: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handler = DatabaseHelper(this)

        showHome()

        register.setOnClickListener {
            showRegis()
        }

        save.setOnClickListener {
            handler.insertUserData(id.text.toString(), pass.text.toString())
            showHome()
        }

        loginbtn.setOnClickListener {
            if (handler.userPresent(username.text.toString(), password.text.toString())) {
                Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
            else Toast.makeText(this, "Username or password is invalid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRegis() {
        regis_layout.visibility = View.VISIBLE
        home.visibility = View.GONE
    }

    private fun showHome() {
        regis_layout.visibility = View.GONE
        home.visibility = View.VISIBLE
    }
}
