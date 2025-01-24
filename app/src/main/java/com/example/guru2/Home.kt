package com.example.guru2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.DriverManager

class Home : ComponentActivity() {


    private val jdbcUrl = "jdbc:mysql://192.168.45.227:3306/check_list_db"//장소를 이동하면 꼭 확인!!cmd르고 들어가서
    //ipv4 주소로 변경
    private val dbUser = "root" //사용자 이름
    private val dbPassword = "123456" //비밀번호

    private val saveHandler = Handler(Looper.getMainLooper()) // 핸들러
    private var lastSavedMemo = "" //메모용.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homelayout)

        // Retrieve references for UI elements
        val teamNameTextView = findViewById<TextView>(R.id.teamName)
        val nicknameContainer = findViewById<LinearLayout>(R.id.nicknameContainer)
        val addNicknameButton = findViewById<ImageButton>(R.id.plusNickname)
        val memoEditText = findViewById<EditText>(R.id.memoEditText)

        // 팀이름, 닉네임 받음
        val teamName = intent.getStringExtra("TEAM_NAME")
        val initialNickname = intent.getStringExtra("NICKNAME")




        //혹시나... 정보가 잘 전달이 안된다면...
        teamNameTextView.text = teamName ?: "팀 이름이 없습니다."
        addNicknameToView(initialNickname ?: "닉네임이 없습니다.", nicknameContainer)

        //멤버 추가 버튼
        addNicknameButton.setOnClickListener {
            showAddFriendDialog(teamName, nicknameContainer)
        }

        //메모 구현
        memoEditText.addTextChangedListener(object : android.text.TextWatcher {
            private var saveRunnable: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                saveHandler.removeCallbacks(saveRunnable ?: Runnable {})
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                val currentMemo = s?.toString()?.trim() ?: ""
                saveRunnable = Runnable {
                    if (currentMemo != lastSavedMemo) {
                        lastSavedMemo = currentMemo
                        saveMemo(currentMemo)
                    }
                }
                saveHandler.postDelayed(saveRunnable!!, 1000)
            }
        })
    }


    private fun showAddFriendDialog(teamName: String?, nicknameContainer: LinearLayout) {
        val dialogView = layoutInflater.inflate(R.layout.dialog, null)

        val editText = dialogView.findViewById<EditText>(R.id.editTextNickname)
        val yesButton = dialogView.findViewById<Button>(R.id.yesButton)
        val noButton = dialogView.findViewById<Button>(R.id.noButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        yesButton.setOnClickListener {
            val newNickname = editText.text.toString().trim()
            if (newNickname.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val isSaved = saveNicknameToDatabase(teamName, newNickname)
                    runOnUiThread {
                        if (isSaved) {
                            Toast.makeText(this@Home, "친구 추가: $newNickname", Toast.LENGTH_SHORT).show()
                            addNicknameToView(newNickname, nicknameContainer)
                        } else {
                            Toast.makeText(this@Home, "친구 추가 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        noButton.setOnClickListener { dialog.dismiss() }

        dialog.setOnShowListener {
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        dialog.show()
    }


    private fun addNicknameToView(nickname: String, container: LinearLayout) {
        if (nickname.isNotBlank()) {
            val textView = TextView(this).apply {
                text = nickname
                setPadding(16, 16, 16, 16)
                textSize = 16f
                setTextColor(android.graphics.Color.BLACK)
            }
            container.addView(textView)
        }
    }

   //데이터 베이스
    private fun saveMemo(memo: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                    val query = "UPDATE team_memos SET memo_content = ? WHERE team_name = ?"
                    val statement = connection.prepareStatement(query)
                    statement.setString(1, memo)
                    statement.setString(2, intent.getStringExtra("TEAM_NAME"))
                    statement.executeUpdate()
                    Log.d("Database", "Memo saved successfully")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Database", "Failed to save memo: ${e.message}")
            }
        }
    }

    //
    private fun saveNicknameToDatabase(teamName: String?, nickname: String): Boolean {
        return try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                val query = "INSERT INTO check_list (team_name, team_mate) VALUES (?, ?)"
                val statement = connection.prepareStatement(query)
                statement.setString(1, teamName)
                statement.setString(2, nickname)
                statement.executeUpdate() > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DatabaseError", "MySQL Exception: ${e.message}")
            false
        }
    }
}
/*
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.DriverManager

class Home : ComponentActivity() {

    // Database connection parameters (replace with your values)
    private val jdbcUrl = "jdbc:mysql://192.168.45.227:3306/check_list_db"
    private val dbUser = "root"
    private val dbPassword = "123456"

    private val saveHandler = Handler(Looper.getMainLooper()) // Save handler for delayed actions
    private var lastSavedMemo = "" // Last saved memo content

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homelayout)



        // Retrieve references for UI elements
        val teamNameTextView = findViewById<TextView>(R.id.teamName)
        val nicknameContainer = findViewById<LinearLayout>(R.id.nicknameContainer)
        val addNicknameButton = findViewById<ImageButton>(R.id.plusNickname)
        val memoEditText = findViewById<EditText>(R.id.memoEditText)

        // Receive team data passed from another screen
        val teamName = intent.getStringExtra("TEAM_NAME")
        val initialNickname = intent.getStringExtra("NICKNAME")

        // Set values on the UI elements
        teamNameTextView.text = teamName ?: "팀 이름이 없습니다."
        addNicknameToView(initialNickname ?: "닉네임이 없습니다.", nicknameContainer)

        // Set click listener for the 'Add Nickname' button
        addNicknameButton.setOnClickListener {
            showAddFriendDialog(teamName, nicknameContainer)
        }

        // Listener for Memo EditText changes (Save after 1 second of typing)
        memoEditText.addTextChangedListener(object : android.text.TextWatcher {
            private var saveRunnable: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Remove previous save task while waiting for new text
                saveHandler.removeCallbacks(saveRunnable ?: Runnable {})
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                val currentMemo = s?.toString()?.trim() ?: ""

                // Save after 1 second if the memo content has changed
                saveRunnable = Runnable {
                    if (currentMemo != lastSavedMemo) {
                        lastSavedMemo = currentMemo
                        saveMemo(currentMemo)
                    }
                }
                saveHandler.postDelayed(saveRunnable!!, 1000)
            }
        })
    }

    private fun showAddFriendDialog(teamName: String?, nicknameContainer: LinearLayout) {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog, null)


        // Get references to EditText and Buttons
        val editText = dialogView.findViewById<EditText>(R.id.editTextNickname)
        val yesButton = dialogView.findViewById<Button>(R.id.yesButton)
        val noButton = dialogView.findViewById<Button>(R.id.noButton)

        // Set up the dialog with the custom view
        val dialog = AlertDialog.Builder(this)
            //.setTitle("추가할 친구의 닉네임을 입력해주세요.")
            .setView(dialogView)
            .create()

        // Set up the actions for the buttons
        yesButton.setOnClickListener {
            val newNickname = editText.text.toString().trim()
            if (newNickname.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val isSaved = saveNicknameToDatabase(teamName, newNickname)
                    runOnUiThread {
                        if (isSaved) {
                            Toast.makeText(this@Home, "친구 추가: $newNickname", Toast.LENGTH_SHORT).show()
                            addNicknameToView(newNickname, nicknameContainer)
                        } else {
                            Toast.makeText(this@Home, "친구 추가 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss() // Close the dialog when confirmed
        }

        noButton.setOnClickListener {
            dialog.dismiss() // Close the dialog on cancel
        }

        // Resize the dialog if necessary
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        dialog.show() // Show the dialog
    }
/*
    // Show a dialog to input a friend's nickname
    private fun showAddFriendDialog(teamName: String?, nicknameContainer: LinearLayout) {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val editText = EditText(this).apply {
            hint = "닉네임"
            maxLines = 1
        }

        // Configure the dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("추가할 친구의 닉네임을 입력해주세요.")
            //.setMessage("Enter the friend's nickname to add them:") //제목이랑 같은 내용인 듯 함...
            .setView(editText)
            .setPositiveButton("추가") { _, _ ->
                val newNickname = editText.text.toString().trim()
                if (newNickname.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val isSaved = saveNicknameToDatabase(teamName, newNickname)
                        runOnUiThread {
                            if (isSaved) {
                                Toast.makeText(this@Home, "친구 추가: $newNickname", Toast.LENGTH_SHORT).show()
                                addNicknameToView(newNickname, nicknameContainer)
                            } else {
                                Toast.makeText(this@Home, "친구 추가 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "닉네임을 입력해주세여ㅛ.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()

        // Resize dialog
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        dialog.show()
    } */

    // Save nickname to MySQL database
    private fun saveNicknameToDatabase(teamName: String?, nickname: String): Boolean {
        return try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                val query = "INSERT INTO check_list (team_name, team_mate) VALUES (?, ?)"
                connection.prepareStatement(query).use { stmt ->
                    stmt.setString(1, teamName)
                    stmt.setString(2, nickname)
                    stmt.executeUpdate() > 0 // Returns true if insert was successful
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseError", "MySQL Exception: ${e.message}")
            false
        }
    }

    // Add nickname to the UI dynamically
    private fun addNicknameToView(nickname: String, nicknameContainer: LinearLayout) {
        val newNicknameView = TextView(this).apply {
            text = nickname
            textSize = 16f
            setPadding(16, 8, 16, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 16 // Spacing between nicknames
            }
        }
        nicknameContainer.addView(newNicknameView)
    }

    // Save Memo functionality (not implemented, placeholder)
    private fun saveMemo(memo: String) {
        Toast.makeText(this, "Memo saved: $memo", Toast.LENGTH_SHORT).show()
    }
}
*/