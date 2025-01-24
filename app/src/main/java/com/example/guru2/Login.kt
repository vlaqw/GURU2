package com.example.guru2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.DriverManager

class Login : ComponentActivity() {
    private val jdbcUrl = "jdbc:mysql://192.168.45.188:3306/check_list_db"
    private val dbUser = "root"
    private val dbPassword = "123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login) // 위 XML 레이아웃 연결

        val teamNameEditText = findViewById<EditText>(R.id.teamName)
        val nicknameEditText = findViewById<EditText>(R.id.nicknameEditText)
        val loginButton = findViewById<Button>(R.id.loginTeamMate)

        loginButton.setOnClickListener {
            val teamName = teamNameEditText.text.toString().trim()
            val nickname = nicknameEditText.text.toString().trim()

            if (teamName.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "모든 입력란을 채워주세요!", Toast.LENGTH_SHORT).show()
            } else {
                // 로그인 확인 로직
                CoroutineScope(Dispatchers.IO).launch {
                    val isValid = checkLoginCredentials(teamName, nickname)
                    runOnUiThread {
                        if (isValid) {
                            Toast.makeText(this@Login, "로그인 성공!", Toast.LENGTH_SHORT).show()

                            // 로그인 성공 시 Home 액티비티로 팀 이름과 닉네임을 함께 넘겨주기
                            val intent = Intent(this@Login, Home::class.java).apply {
                                putExtra("TEAM_NAME", teamName)  // 팀 이름 전달
                                putExtra("NICKNAME", nickname)  // 닉네임 전달
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@Login, "팀 이름과 닉네임을 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun checkLoginCredentials(teamName: String, nickname: String): Boolean {
        return try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                val query = "SELECT COUNT(*) FROM check_list WHERE team_name = ? AND team_mate = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, teamName)
                    statement.setString(2, nickname)
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            resultSet.getInt(1) > 0 // 일치하는 행이 있는지 확인
                        } else {
                            false
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}