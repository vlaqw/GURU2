package com.example.guru2

import androidx.activity.ComponentActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homelogin);

        val buttonIn = findViewById<Button>(R.id.makingTeam)
        val buttonLogin = findViewById<Button>(R.id.login)

        //버튼 선택시 이동할 코틀른 코드로...
        buttonIn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val myIntent = Intent(this@MainActivity, TeamLogin::class.java) //팀 가입 로그인 페이지로 이동
                startActivity(myIntent)

            }
        })

        buttonLogin.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val myIntent = Intent(this@MainActivity, Login::class.java) //팀 페이지로 이동.
                startActivity(myIntent)

            }
        })

    }
}