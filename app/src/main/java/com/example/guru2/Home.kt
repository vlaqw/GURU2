package com.example.guru2

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler //수정으로 필요가 없어졌으나, 혹시 다시 사용할 수도 있음...
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.DriverManager
import java.util.concurrent.Executors
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity // 추가
import androidx.fragment.app.Fragment // 추가


class Home : AppCompatActivity() {
    private val jdbcUrl = "jdbc:mysql://192.168.219.101:3306/check_list_db"
    //private val jdbcUrl = "jdbc:mysql://192.168.45.85:3306/check_list_db?useUnicode=true&characterEncoding=utf8mb4"
    private val dbUser = "root"
    private val dbPassword = "123456"

    //사소한 사항이나, 이로 인해 오류 확인 후 수정까지 4시간이 걸림... 꼭 확인!!
    //cmd 들어가서, 나의 ipv4 주소 확인 후 꼭!! 수정하기. //나는 매일.. 달라짐으로... 매일 확인...
    //mysql에 외부 접근 혀용 점검
    //예시)  grant all privileges on *.* to 'root'@'192.168.45.227' identified by '123456';
    //mysql 구현 코드
    //mysql> CREATE TABLE check_list (
    //    -> team_name VARCHAR(20) NOT NULL,
    //    -> team_mate VARCHAR(20) NOT NULL
    //    -> );

    //01.31 mysql 테이블 수정!!!
    // ALTER TABLE check_list
    //    -> MODIFY COLUMN team_memo VARCHAR(1200);
    //ALTER TABLE check_list ADD CONSTRAINT unique_team_mate UNIQUE (team_name, team_mate);

    //만약, mysql에서 테이블을 지우고 처음부터 생성하고 싶다면,
    //CREATE TABLE check_list (
    //    team_name VARCHAR(20) NOT NULL,
    //    team_mate VARCHAR(20) NOT NULL,
    //    team_memo VARCHAR(1200),
    //    PRIMARY KEY (team_name, team_mate)     +참고로 (team_name, team_mate) 조합이 유일해야 함!
    //);

    private lateinit var existingMemo: String  // 로그인한 사용자의 메모 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homelayout)

        val teamNameTextView = findViewById<TextView>(R.id.teamName) //Login에서 받아온 팀 이름..
        val nicknameContainer = findViewById<LinearLayout>(R.id.nicknameContainer) //닉네임컨테이너(이미 팀에 추가된 팀원들 정보 담는 용도)
        val addNicknameButton = findViewById<ImageButton>(R.id.plusNickname) //새로운 친구를 초대할 때, 사용하는 버튼
        val memoEditText = findViewById<EditText>(R.id.memoEditText) //메모 텍스트, 이미 mysql에 저장된 정보면 바로 나타나고, 사용자가 쉽게 수정할 수 있음.
        val saveMemoButton = findViewById<Button>(R.id.saveMemo) //사용자 메모 수정 후 버튼을 이용해서 저장할 수 있음.(자동으로 같은 팀 한정으로 팀 메모가 업데이트 됨)
        val itemButton = findViewById<ImageButton>(R.id.itemImage) //물품 이미지
        val accountButton = findViewById<ImageButton>(R.id.accountingImage) //회계 이미지

        val neededItemsTextView  = findViewById<TextView>(R.id.things) //구매할 물픔 표시
        val neededItems = intent.getStringExtra("NEEDED_ITEMS") ?: "구매할 물품이 없습니다."

        neededItemsTextView.text = "구매할 물품: $neededItems"

        val teamName = intent.getStringExtra("TEAM_NAME") ?: return  //인텐트를 통해..Login에서 받아온 팀 이름.
        val initialNickname = intent.getStringExtra("NICKNAME") ?: return //인텐트를 통해..Login에서 받아온 팀 닉네임..

        teamNameTextView.text = teamName //팀이름을 통해, 팀에 있는 팀원이 있는 경우 불러옴.

        // 🔹 닉네임 컨테이너를 가로 방향으로 설정 (XML에서 이미 설정됨), 같은 팀원끼리 일직선으로 배치하기 위해서 수평적 배치
        nicknameContainer.orientation = LinearLayout.HORIZONTAL

        // 🔹 로그인한 사용자의 메모 가져오기
        getExistingMemo(teamName, initialNickname, memoEditText)

        // 🔹 팀원 목록 불러오기 (로그인한 사용자는 맨 앞)
        getTeamMembers(teamName, initialNickname, nicknameContainer)

        addNicknameButton.setOnClickListener {
            showAddFriendDialog(teamName, nicknameContainer) //팀 닉네임을 추가하는 다이어로그, 다이어로그xml은 새로 만들었음(dialog.xml)
        }

        saveMemoButton.setOnClickListener {
            val memoText = memoEditText.text.toString().trim()
            if (memoText.isNotEmpty()) {
                updateMemo(teamName, memoText) //메모를 저장하겠다 = 새로운 메모를 작성하겠다는 의미. 업데이트를 함.
            } else {
                Toast.makeText(this, "메모를 입력하세요!", Toast.LENGTH_SHORT).show()
            }
        }
       //물품 페이지로 이동...
        itemButton.setOnClickListener {
            val intent = Intent(this@Home, ItemMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("TEAM_NAME", teamName) // 팀 이름을 추가
            intent.putExtra("NICKNAME", initialNickname) // 닉네임을 추가
            startActivity(intent)
        }
        accountButton.setOnClickListener {
            val intent = Intent(this@Home, AccountMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("TEAM_NAME", teamName) // 팀 이름을 추가
            intent.putExtra("NICKNAME", initialNickname) // 닉네임을 추가
            startActivity(intent)
        }


        //회계 페이지로 이동...

        /*accountButton.setOnClickListener {
            val intent = Intent(this@Home, AccountMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("TEAM_NAME", teamName) // 팀 이름을 추가
            intent.putExtra("NICKNAME", initialNickname) // 닉네임을 추가
            startActivity(intent)
        }*/
       /* accountButton.setOnClickListener {
            val fragment = AccountingFragment()
            val bundle = Bundle().apply {
                putString("TEAM_NAME", teamName)
                putString("NICKNAME", initialNickname)
            }
            fragment.arguments = bundle

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }*/

    }
 //메모 업데이트를 하는 함수
    private fun updateMemo(teamName: String, newMemo: String) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                // MySQL 드라이버 로드
                Class.forName("com.mysql.jdbc.Driver")
                val connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)

                // team_name과 team_mate는 변경되지 않도록, team_memo만 수정!
                val sql =
                    "UPDATE check_list SET team_memo = ? WHERE team_name = ? AND team_memo IS NOT NULL"
                //같은 팀이면, 한 명이 업데이트 했다면, 다 업데이트 될 수 있도록 함.
                val statement = connection.prepareStatement(sql)
                statement.setString(1, newMemo)
                statement.setString(2, teamName)

                val rowsUpdated = statement.executeUpdate()

                runOnUiThread {
                    if (rowsUpdated > 0) {
                        Toast.makeText(this@Home, "메모가 업데이트 되었습니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Home, "메모 업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                statement.close()
                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@Home, "DB 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 🔹 로그인한 사용자의 기존 메모 가져오기
    private fun getExistingMemo(teamName: String, teamMate: String, memoEditText: EditText) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                    val sql = "SELECT team_memo FROM check_list WHERE team_name = ? AND team_mate = ?"
                    val statement = connection.prepareStatement(sql)
                    statement.setString(1, teamName)
                    statement.setString(2, teamMate)

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        existingMemo = resultSet.getString("team_memo") ?: ""
                        runOnUiThread { memoEditText.setText(existingMemo) }
                    }

                    resultSet.close()
                    statement.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@Home, "DB 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 🔹 같은 팀의 모든 멤버 가져오기
  private fun getTeamMembers(teamName: String, loggedInUser: String, container: LinearLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                    val sql = "SELECT team_mate FROM check_list WHERE team_name = ?"
                    //팀 이름이 같은 팀원 모두 불러옴.
                    val statement = connection.prepareStatement(sql)
                    statement.setString(1, teamName)

                    val resultSet = statement.executeQuery()
                    val teamMembers = mutableListOf<String>()

                    while (resultSet.next()) {
                        val member = resultSet.getString("team_mate")
                        teamMembers.add(member)
                    }

                    resultSet.close()
                    statement.close()

                    runOnUiThread {
                        container.removeAllViews() // 기존 UI 초기화

                        // 🔹 로그인한 사용자를 가장 앞에 추가
                        addNicknameToView(loggedInUser, container, isCurrentUser = true)

                        // 🔹 나머지 팀원 추가 (로그인한 사용자 제외)
                        teamMembers.filter { it != loggedInUser }.forEach { member ->
                            addNicknameToView(member, container, isCurrentUser = false)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@Home, "팀원 목록을 불러오는 중 오류 발생!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


//추가한 친구의 닉네임을 xml 화면에 띄우기 위해 필요함.
    private fun showAddFriendDialog(teamName: String?, nicknameContainer: LinearLayout) {
        val dialogView = layoutInflater.inflate(R.layout.dialog, null) //새롭게 만든 다이어로그 xml
        val editText = dialogView.findViewById<EditText>(R.id.editTextNickname)
        val yesButton = dialogView.findViewById<Button>(R.id.yesButton)
        val noButton = dialogView.findViewById<Button>(R.id.noButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        //새로운 친구 추가를 하는 경우
        yesButton.setOnClickListener {
            val newNickname = editText.text.toString().trim()
            if (newNickname.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val isSaved = saveNicknameToDatabase(teamName, newNickname, existingMemo)
                    runOnUiThread {
                        if (isSaved) {
                            Toast.makeText(this@Home, "친구 추가: $newNickname", Toast.LENGTH_SHORT).show()
                            addNicknameToView(newNickname, nicknameContainer, isCurrentUser = false)
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
        dialog.show()
    }
   //새로운 친구의 닉네임, 팀이름, 팀 메모를 데이터베이스에 저장하기 위한 코드...
    private suspend fun saveNicknameToDatabase(teamName: String?, nickname: String, teamMemo: String?): Boolean {
        val finalMemo = teamMemo ?: ""
        if (teamName != null && nickname.isNotBlank()) {
            return try {
                Class.forName("com.mysql.jdbc.Driver")
                DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                    val query = "INSERT INTO check_list (team_name, team_mate, team_memo, created_at) VALUES (?, ?, ?, NOW())"
                    val statement = connection.prepareStatement(query)
                    statement.setString(1, teamName)
                    statement.setString(2, nickname)
                    statement.setString(3, finalMemo)

                    statement.executeUpdate() > 0
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        return false
    }

   //닉네임을 보여주는 코드
    private fun addNicknameToView(nickname: String, container: LinearLayout, isCurrentUser: Boolean) {
        if (nickname.isNotBlank()) {
            val button = Button(this).apply {
                text = nickname
                textSize = 16f
                setTextColor(android.graphics.Color.parseColor("#FF7043")) // Set the color to #FF7043
                setBackgroundResource(R.drawable.button_white_round) // Use the shape background
                isClickable = false // Make sure the button is non-clickable
                isFocusable = false // Disable focus as well
            }

            // 고정된 버튼 크기 (73dp)
            val buttonWidth = (75 * resources.displayMetrics.density).toInt()  // 73dp를 픽셀로 변환
            val buttonHeight = LinearLayout.LayoutParams.WRAP_CONTENT  // 높이는 내용에 맞게 자동 조정

            // 버튼의 레이아웃 파라미터 설정
            val params = LinearLayout.LayoutParams(buttonWidth, buttonHeight).apply {
                setMargins(10, 16, 10, 16) // 10dp 좌우, 16dp 상하 마진
            }

            button.layoutParams = params

            // 컨테이너의 첫 번째 위치에 버튼 추가
            container.addView(button, 0)
        }
    }
}




