package com.example.guru2

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Intent

class ItemMain : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private val itemList = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_main)

        recyclerView = findViewById(R.id.recyclerView)
        val fab: FloatingActionButton = findViewById(R.id.fab_add_item)
        val homeButton = findViewById<ImageButton>(R.id.homeImage) //물품 이미지
        val accountButton = findViewById<ImageButton>(R.id.accountingImage) //회계 이미지

        // Intent로부터 teamName과 nickname을 받는다
        val teamName = intent.getStringExtra("TEAM_NAME") ?: return
        val nickname = intent.getStringExtra("NICKNAME") ?: return

        // dbhelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 데이터베이스에서 items 불러오기
        itemList.addAll(databaseHelper.getItems())


        // 홈 버튼 클릭 시, 구매해야 할 물품들만 필터링해서 전달
        homeButton.setOnClickListener {
            val neededItems = itemList.filter { !it.isCompleted }  // 구매 필요한 항목 필터링
            val neededItemNames = neededItems.joinToString(", ") { it.name } // 아이템 이름만 추출해서 문자열로 변환

            val homeIntent = Intent(this, Home::class.java)
            homeIntent.putExtra("TEAM_NAME", teamName)
            homeIntent.putExtra("NICKNAME", nickname)
            homeIntent.putExtra("NEEDED_ITEMS", neededItemNames) // 구매 필요한 물품 전달
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(homeIntent)
        }

        //회계 페이지로 이동...
        accountButton.setOnClickListener {
            val intent = Intent(this@ItemMain, AccountMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("TEAM_NAME", teamName) // 팀 이름을 추가
            intent.putExtra("NICKNAME", nickname) // 닉네임을 추가
            startActivity(intent)
        }


        // 플로팅 액션 버튼 클릭 시 아이템 추가 다이얼로그 표시
        fab.setOnClickListener {
            showAddItemDialog()
        }

        // 어댑터 설정 및 RecyclerView 초기화
        adapter = ItemAdapter(itemList, databaseHelper)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 카테고리 버튼 클릭 시 필터링
        val btnAll = findViewById<ImageButton>(R.id.btn_all)
        val btnFood = findViewById<ImageButton>(R.id.btn_food)
        val btnDaily = findViewById<ImageButton>(R.id.btn_daily)
        val btnClean = findViewById<ImageButton>(R.id.btn_clean)
        val btnKitchen = findViewById<ImageButton>(R.id.btn_kitchen)

        // 각 카테고리 버튼에 클릭 리스너 추가
        btnAll.setOnClickListener {
            filterItems("전체")
        }
        btnFood.setOnClickListener {
            filterItems("식품")
        }
        btnDaily.setOnClickListener {
            filterItems("생활용품")
        }
        btnClean.setOnClickListener {
            filterItems("청소용품")
        }
        btnKitchen.setOnClickListener {
            filterItems("주방용품")
        }
    }

    // 카테고리별로 아이템을 필터링하는 함수
    private fun filterItems(category: String) {
        val filteredList = if (category == "전체") {
            databaseHelper.getItems()   // 전체 아이템 조회
        } else {
            databaseHelper.getItemsByCategory(category)   // 특정 카테고리 아이템 조회
        }
        itemList.clear() // 기존 아이템 리스트를 지우고
        itemList.addAll(filteredList) // 필터링된 아이템을 추가
        adapter.notifyDataSetChanged()   //RecyclerView 갱신
    }

    // 아이템 추가 다이얼로그 표시 함수
    private fun showAddItemDialog() {
        // 다이얼로그에 표시할 레이아웃을 인플레이션하여 가져옴
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        // 다이얼로그에서 입력받을 항목들을 각각 변수에 저장
        val itemName = dialogView.findViewById<EditText>(R.id.et_item_name)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val quantity = dialogView.findViewById<EditText>(R.id.et_quantity)
        val memo = dialogView.findViewById<EditText>(R.id.et_memo)
        val addButton = dialogView.findViewById<Button>(R.id.btn_add)

        // 카테고리 스피너 설정 (카테고리 목록을 드롭다운 리스트로 표시)
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array, // res/values/strings.xml에 정의된 카테고리 목록
            android.R.layout.simple_spinner_item // 드롭다운 리스트의 항목 스타일
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // 드롭다운 항목 스타일
            categorySpinner.adapter = adapter // 스피너에 어댑터 적용
        }

        // 다이얼로그 설정
        val dialog = AlertDialog.Builder(this)
            .setTitle("물품 추가") // 다이얼로그 제목
            .setView(dialogView) // 다이얼로그에 표시할 뷰 설정
            .create()

        // "등록" 버튼 클릭 시 아이템 추가
        addButton.setOnClickListener {
            val name = itemName.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val quantityValue = quantity.text.toString().toIntOrNull() ?: 0
            val memoText = memo.text.toString()

            // 아이템 이름이 비어있지 않으면 아이템 추가
            if (name.isNotEmpty()) {
                // 새 아이템 생성
                val newItem = Item(name = name, category = category, quantity = quantityValue, memo = memoText)

                // 데이터베이스에 새 아이템 추가
                databaseHelper.addItem(newItem)

                // 새 아이템을 itemList에 추가하고 RecyclerView에 갱신 사항을 알림
                itemList.add(newItem)
                adapter.notifyItemInserted(itemList.size - 1)  // 새 아이템이 추가되었음을 어댑터에 알림

                dialog.dismiss() // 다이얼로그 닫기
            } else {
                // 아이템 이름이 비어있을 경우 사용자에게 알림
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show() // 다이얼로그 표시
    }

}