package com.example.guru2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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

    //private val jdbcUrl = "jdbc:mysql://192.168.45.26:3306/check_list_db"

   private val jdbcUrl = "jdbc:mysql://192.168.219.106:3306/check_list_db" // 서버 IP 및 포트 설정
//192.168.45.125
    //사소한 사항이나, 이로 인해 오류 확인 후 수정까지 4시간이 걸림... 꼭 확인!!
    //cmd 들어가서, 나의 ipv4 주소 확인 후 꼭!! 수정하기.
    //mysql에 외부 접근 혀용 점검
    //예시)  grant all privileges on *.* to 'root'@'192.168.45.227' identified by '123456';
    //mysql 구현 코드
    //mysql> CREATE TABLE check_list (
    //    -> team_name VARCHAR(20) NOT NULL,
    //    -> team_mate VARCHAR(20) NOT NULL
    //    -> );
    private val dbUser = "root" // DB 사용자 이름
    private val dbPassword = "123456" // DB 비밀번호

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
//사용자 팀원 가입시 mysql과 연동 후 저장.
        saveButton.setOnClickListener {
            val nickname = nicknameEditText.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                // MySQL 데이터 저장 (코루틴으로 백그라운드 작업 처리)
                CoroutineScope(Dispatchers.IO).launch {
                    val isSuccess = saveToDatabase(teamName, nickname)
                    runOnUiThread {
                        if (isSuccess) {
                            Toast.makeText(this@TeamMateLogin, "저장 성공!", Toast.LENGTH_SHORT).show()
                            // 다음 화면으로 이동
                            val intent = Intent(this@TeamMateLogin, Home::class.java)
                            intent.putExtra("TEAM_NAME", teamName)
                            intent.putExtra("NICKNAME", nickname)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@TeamMateLogin,
                                "저장 실패! 다시 시도하세요.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            }
        }
    }

//DB 저장 코드
    private fun saveToDatabase(teamName: String?, nickname: String): Boolean {
        return try {
            //Class.forName("com.mysql.cj.jdbc.Driver") //이것으로 인해 연결이 안되었음....(6시간의 사투...) 잟 확인하기.
           Class.forName("com.mysql.jdbc.Driver");
            DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                val query = "INSERT INTO check_list (team_name, team_mate,team_memo) VALUES (?, ?, ?)" //db저장
                val statement = connection.prepareStatement(query)
                statement.setString(1, teamName)
                statement.setString(2, nickname)
                statement.setString(3, "")
                statement.executeUpdate() > 0
            }
        } catch (e: Exception) {
            e.printStackTrace() // 자세한 오류 로그 출력

            Log.e("DatabaseError", "MySQL Exception: ${e.message}") // 로그를 Android Logcat에서 확인 가능
            false
        }
    }
}

