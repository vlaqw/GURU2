package com.example.guru2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class Home : ComponentActivity() {
    private val saveHandler = Handler(Looper.getMainLooper()) // 저장 처리용 핸들러
    private var lastSavedMemo = "" // 마지막으로 저장된 메모 내용을 기록

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homelayout)

        // XML의 팀명과 닉네임 TextView 참조
        val teamNameTextView = findViewById<TextView>(R.id.teamName)
        val nicknameTextView = findViewById<TextView>(R.id.nickname)
        val memoEditText = findViewById<EditText>(R.id.memoEditText)

        // 이전 화면에서 전달된 팀명과 닉네임 설정
        val teamName = intent.getStringExtra("TEAM_NAME")
        val nickname = intent.getStringExtra("NICKNAME")
        teamNameTextView.text = teamName ?: "팀명 없음"
        nicknameTextView.text = nickname ?: "닉네임 없음"

        // 메모 EditText에 변경 리스너 추가
        memoEditText.addTextChangedListener(object : android.text.TextWatcher {
            private var saveRunnable: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 기존 대기 중인 저장 작업 제거 (최신 입력 대기)
                saveHandler.removeCallbacks(saveRunnable ?: Runnable {})
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                val currentMemo = s?.toString()?.trim() ?: ""

                // 텍스트 변경 후 1초 뒤에 저장
                saveRunnable = Runnable {
                    if (currentMemo != lastSavedMemo) { // 이전 저장된 메모와 다르면 저장
                        lastSavedMemo = currentMemo
                        saveMemo(currentMemo) // 저장 함수 호출
                    }
                }
                saveHandler.postDelayed(saveRunnable!!, 1000) // 1초 대기 후 실행
            }
        })
    }
    private fun saveMemo(memo: String) {
        Toast.makeText(this, "메모 저장됨: $memo", Toast.LENGTH_SHORT).show()
        // SQLite, SharedPreferences 등 실제 저장 로직 추가 가능
    }
}
