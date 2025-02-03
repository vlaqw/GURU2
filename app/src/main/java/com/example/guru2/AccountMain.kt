package com.example.guru2

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.DriverManager
import java.util.Calendar

class AccountMain: AppCompatActivity(){

    private val jdbcUrl = "jdbc:mysql://192.168.219.108:3306/check_list_db"
    private val dbUser = "root"
    private val dbPassword = "123456"

    lateinit var addItemButton: FloatingActionButton // 항목 추가 버튼
    lateinit var itemsContainer: LinearLayout // 항목들이 추가될 컨테이너
    lateinit var totalAmountTextView: TextView // 총액을 표시하는 TextView
    lateinit var amountPerPersonTextView: TextView // 1인당 금액을 표시할 TextView
    lateinit var yearMonthTextView: TextView // 현재의 연도와 월을 표시하는 TextView

    private var totalAmount = 0.0 // 총액을 저장하는 변수
    private var teamMemberCount = 0 // 팀원 수를 저장하는 변수 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_main)

        val homeButton = findViewById<ImageButton>(R.id.homeImage) //물품 이미지
        val itemButton = findViewById<ImageButton>(R.id.itemImage) //물품 이미지
        val sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE)
        val teamName = intent.getStringExtra("TEAM_NAME") ?: return  //인텐트를 통해..Login에서 받아온 팀 이름.
        val initialNickname = intent.getStringExtra("NICKNAME") ?: return //인텐트를 통해..Login에서 받아온 팀 닉네임..

        //home으로 받은 팀 닉네임, 팀이름 저장.
        sharedPreferences.edit().apply {
            putString("TEAM_NAME", teamName)
            putString("NICKNAME", initialNickname)
            apply()
        }

        addItemButton = findViewById(R.id.addItemButton)
        itemsContainer = findViewById(R.id.itemsContainer)
        totalAmountTextView = findViewById(R.id.totalAmountTextView)
        amountPerPersonTextView = findViewById(R.id.amountPerPersonTextView)
        yearMonthTextView = findViewById(R.id.yearMonthTextView)

        totalAmountTextView.text = "총액: 0원"

        // 현재 연도와 월을 가져와서 화면에 표시
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH) + 1
        yearMonthTextView.text = "${year}년 ${month}월 정산 내역이에요"

        // 항목 추가 버튼 클릭 시 새로운 항목 추가 함수 호출
        addItemButton.setOnClickListener { addNewItem() }

        //홈버튼을 눌렀을 때,
        homeButton.setOnClickListener {
            /*val homeIntent = Intent(this@AccountMain, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("TEAM_NAME", teamName) // 팀 이름을 추가
            intent.putExtra("NICKNAME", initialNickname) // 닉네임을 추가
            startActivity(homeIntent)*/
            val homeIntent = Intent(this@AccountMain, Home::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT  // 기존 액티비티 유지
            startActivity(homeIntent)
        }

        //물품 페이지로 이동...
        itemButton.setOnClickListener {
            val intent = Intent(this@AccountMain, ItemMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("TEAM_NAME", teamName) // 팀 이름을 추가
            intent.putExtra("NICKNAME", initialNickname) // 닉네임을 추가
            startActivity(intent)
        }

        // 팀원 수를 구한 후 인당 금액을 계산 (첫 로드 시에만 호출)
        getTeamMemberCount(teamName) { count ->
            teamMemberCount = count // 팀원 수 저장
            updateAmountPerPerson() // 팀원 수를 구한 후, 금액을 계산
        }
    }

    // 새로운 항목을 추가하는 함수
    private fun addNewItem() {
        // 새로운 항목을 위한 레이아웃을 생성
        val itemLayout = LinearLayout(this)
        itemLayout.orientation = LinearLayout.HORIZONTAL

        // 항목 간의 여백을 설정 (아이템 간 간격)
        val itemLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val heightInPx = (80 * resources.displayMetrics.density).toInt()  // 80dp를 픽셀로 변환
        itemLayoutParams.height = heightInPx

        itemLayoutParams.bottomMargin = 8 // 항목 레이아웃 간의 간격 (8dp)
        itemLayoutParams.leftMargin = 16  // 항목 레이아웃 왼쪽 여백 (16dp)
        itemLayoutParams.rightMargin = 16 // 항목 레이아웃 오른쪽 여백 (16dp)
        itemLayout.layoutParams = itemLayoutParams

        // 항목 레이아웃의 모서리 둥글게 설정
        val itemLayoutDrawable = GradientDrawable()
        itemLayoutDrawable.shape = GradientDrawable.RECTANGLE
        itemLayoutDrawable.cornerRadius = 16f  // 모서리 둥글게 설정 (16dp)
        itemLayoutDrawable.setColor(Color.WHITE)  // 배경 색상 흰색으로 설정

        // 항목 레이아웃에 배경 설정
        itemLayout.background = itemLayoutDrawable

        itemLayout.gravity = Gravity.CENTER

        itemLayout.weightSum = 5f

        // 항목 이름 입력란
        val nameEditText = EditText(this)
        nameEditText.hint = "항목을 입력하세요"
        nameEditText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)

        nameEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        nameEditText.gravity = Gravity.CENTER
        nameEditText.setPadding(16, 16, 16, 32)
        val nameParams = nameEditText.layoutParams as LinearLayout.LayoutParams
        nameParams.leftMargin = 8  // 왼쪽 여백 추가
        nameParams.rightMargin = 8 // 오른쪽 여백 추가

        val nameDrawable = GradientDrawable()
        nameDrawable.shape = GradientDrawable.RECTANGLE
        nameDrawable.cornerRadius = 16f  // 모서리 둥글게 설정 (16dp)
        nameDrawable.setColor(Color.WHITE)  // 배경 색상 흰색으로 설정
        nameEditText.background = nameDrawable

        // 금액 입력란
        val amountEditText = EditText(this)
        amountEditText.hint = "금액을 입력하세요"
        amountEditText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)

        amountEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        amountEditText.gravity = Gravity.CENTER
        amountEditText.setPadding(16, 16, 16, 32)
        val amountParams = amountEditText.layoutParams as LinearLayout.LayoutParams
        amountParams.leftMargin = 8  // 왼쪽 여백 추가
        amountParams.rightMargin = 8 // 오른쪽 여백 추가

        val amountDrawable = GradientDrawable()
        amountDrawable.shape = GradientDrawable.RECTANGLE
        amountDrawable.cornerRadius = 16f  // 모서리 둥글게 설정 (16dp)
        amountDrawable.setColor(Color.WHITE)  // 배경 색상 흰색으로 설정
        amountEditText.background = amountDrawable

        // 항목 삭제 버튼
        val deleteButton = Button(this)
        deleteButton.text = "삭제"
        deleteButton.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        deleteButton.background = ContextCompat.getDrawable(this, R.drawable.button_round)

        deleteButton.setTextColor(Color.WHITE)

        deleteButton.setPadding(16, 16, 16, 16)
        val buttonParams = deleteButton.layoutParams as LinearLayout.LayoutParams
        buttonParams.leftMargin = 8  // 왼쪽 여백 추가
        buttonParams.rightMargin = 8 // 오른쪽 여백 추가

        // 금액이 변경될 때마다 총액 갱신
        amountEditText.addTextChangedListener { text ->
            updateTotalAmount()  // 금액이 바뀔 때마다 총액을 갱신하는 함수 호출
        }

        // 삭제 버튼 클릭 시 항목 삭제 및 총액 계산
        deleteButton.setOnClickListener {
            // 삭제하려는 항목의 금액을 가져와서 총액에서 차감
            val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
            totalAmount -= amount
            totalAmountTextView.text = "총액: ${totalAmount}원"

            // 항목 삭제
            itemsContainer.removeView(itemLayout)
            // 총액 갱신 후 인당 금액 계산
            updateTotalAmount()  // 금액이 바뀔 때마다 총액을 갱신하는 함수 호출

        }

        // 항목 레이아웃에 각 뷰 추가
        itemLayout.addView(nameEditText)
        itemLayout.addView(amountEditText)
        itemLayout.addView(deleteButton)

        // 항목을 화면에 추가
        itemsContainer.addView(itemLayout)

    }

    // 총액을 갱신하는 함수
    private fun updateTotalAmount() {
        totalAmount = 0.0  // 총액 초기화
        // 모든 항목을 순회하며 금액을 더함
        for (i in 0 until itemsContainer.childCount) {
            val child = itemsContainer.getChildAt(i)

            // child가 LinearLayout인지 확인
            if (child is LinearLayout) {
                val amountEditText = child.getChildAt(1) as? EditText  // 두 번째 자식 뷰가 금액 입력란

                // 금액 입력란이 비어 있지 않으면 값을 가져와서 더함
                val amount = amountEditText?.text.toString().toDoubleOrNull() ?: 0.0
                totalAmount += amount  // 총액에 더하기
            }
        }

        // 총액 표시
        totalAmountTextView.text = "총액: ${totalAmount}원"

        // 인당 금액 업데이트 (팀원 수가 이미 구해졌다면 바로 처리)
        updateAmountPerPerson()
    }

    // 팀원 수에 맞춰 인당 금액을 업데이트하는 함수
    private fun updateAmountPerPerson() {
        if (teamMemberCount > 0) {
            val amountPerPerson = totalAmount / teamMemberCount
            amountPerPersonTextView.text = "${amountPerPerson}원"
        } else {
            amountPerPersonTextView.text = "0원" // 팀원이 없을 경우
        }
    }

    private fun getTeamMemberCount(teamName: String, callback: (Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // MySQL 연결 및 팀원 수 조회
                Class.forName("com.mysql.jdbc.Driver")
                DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                    val sql = "SELECT COUNT(*) AS team_member_count FROM check_list WHERE team_name = ?"
                    val statement = connection.prepareStatement(sql)
                    statement.setString(1, teamName)

                    val resultSet = statement.executeQuery()
                    var memberCount = 0
                    if (resultSet.next()) {
                        memberCount = resultSet.getInt("team_member_count")
                    }

                    resultSet.close()
                    statement.close()

                    // UI 업데이트는 메인 스레드에서
                    withContext(Dispatchers.Main) {
                        callback(memberCount)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AccountMain, "팀원 수를 불러오는 중 오류 발생!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}