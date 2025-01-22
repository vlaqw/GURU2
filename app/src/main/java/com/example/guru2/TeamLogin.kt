package com.example.guru2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class TeamLogin : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teamlogin)

        val teamNameEditText = findViewById<EditText>(R.id.edtLoginTeam)
        val proceedTeamButton = findViewById<Button>(R.id.loginTeam)

        // 초기 버튼 상태 설정
        proceedTeamButton.isEnabled = false
        proceedTeamButton.setBackgroundColor(Color.GRAY) // 버튼을 회색으로 설정

        // 팀 이름 입력 시 버튼 활성화/비활성화 처리
        teamNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().trim()
                if (input.isEmpty()) {
                    // 입력이 없을 경우 버튼 비활성화 및 회색 처리
                    proceedTeamButton.isEnabled = false
                    proceedTeamButton.setBackgroundColor(Color.GRAY)
                } else {
                    // 입력이 있을 경우 버튼 활성화 및 오렌지색 처리
                    proceedTeamButton.isEnabled = true
                    proceedTeamButton.setBackgroundColor(Color.parseColor("#FF7900"))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 버튼 클릭 시 처리
        proceedTeamButton.setOnClickListener {
            val teamName = teamNameEditText.text.toString().trim()
            if (teamName.isNotEmpty()) {
                val intent = Intent(this@TeamLogin, TeamMateLogin::class.java)
                intent.putExtra("TEAM_NAME", teamName)
                startActivity(intent)
            } else {
                Toast.makeText(this, "팀 이름을 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}