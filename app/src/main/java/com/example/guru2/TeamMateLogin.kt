package com.example.guru2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

class TeamMateLogin : ComponentActivity() {

    /*private val jdbcUrl = "jdbc:mysql://check_list_db" // 서버 IP 및 포트 설정
    private val dbUser = "root" // DB 사용자 이름 , 오류가 있으니 수정 요망
    private val dbPassword = "123456" // DB 비밀번호, 오류 있음(원인 모름)*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teammatelogin) // 팀 로그인 화면의 XML 파일을 연결

        // 팀 이름 받아오기
        val teamName = intent.getStringExtra("TEAM_NAME")
        val teamNameTextView = findViewById<TextView>(R.id.teamName)
        teamNameTextView.text = teamName // TextView에 팀 이름 표시

        // 사용자 입력 필드 및 버튼 참조
        val nicknameEditText = findViewById<EditText>(R.id.nicknameEditText)
        val saveButton = findViewById<Button>(R.id.loginTeamMate)

        // 초기 버튼 상태 설정
        saveButton.isEnabled = false
        saveButton.setBackgroundColor(Color.GRAY) // 버튼을 회색으로 설정

        // 닉네임 입력 시 버튼 활성화/비활성화 처리
        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().trim()
                saveButton.isEnabled = input.isNotEmpty()
                saveButton.setBackgroundColor(
                    if (input.isEmpty()) Color.GRAY else Color.parseColor("#FF7900")
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        saveButton.setOnClickListener {
            val nickname = nicknameEditText.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                // MySQL 데이터 저장 (코루틴으로 백그라운드 작업 처리)
                /*CoroutineScope(Dispatchers.IO).launch {
                    val isSuccess = saveToDatabase(teamName, nickname)
                    runOnUiThread {
                        if (isSuccess) { */
                            Toast.makeText(this@TeamMateLogin, "저장 성공!", Toast.LENGTH_SHORT).show()
                            // 다음 화면으로 이동
                            val intent = Intent(this@TeamMateLogin, Home::class.java)
                            intent.putExtra("TEAM_NAME", teamName)
                            intent.putExtra("NICKNAME", nickname)
                            startActivity(intent)
                        } /*else {
                            Toast.makeText(this@TeamMateLogin, "저장 실패! 다시 시도하세요.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }*/
        }
    }
/*
    // MySQL 데이터 저장 함수
    private fun saveToDatabase(teamName: String?, nickname: String): Boolean {
        return try {
            // MySQL JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver")

            // 연결 설정
            DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                val query = "INSERT INTO check_list (team_name, team_mate) VALUES (?, ?)"
                val statement: PreparedStatement = connection.prepareStatement(query)

                // 데이터 바인딩
                statement.setString(1, teamName)
                statement.setString(2, nickname)

                // 쿼리 실행
                statement.executeUpdate() > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }*/
}