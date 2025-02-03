package com.example.guru2

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
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


        //홈버튼이 눌렀을 때,

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




        // 플로팅액션버튼
        fab.setOnClickListener {
            showAddItemDialog()
        }

        adapter = ItemAdapter(itemList, databaseHelper)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val btnAll = findViewById<ImageButton>(R.id.btn_all)
        val btnFood = findViewById<ImageButton>(R.id.btn_food)
        val btnDaily = findViewById<ImageButton>(R.id.btn_daily)
        val btnClean = findViewById<ImageButton>(R.id.btn_clean)
        val btnKitchen = findViewById<ImageButton>(R.id.btn_kitchen)

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

   /* private fun loadItemsFromDatabase() {
        // 데이터베이스에서 아이템을 불러와서 목록에 추가
        itemList.addAll(databaseHelper.getItems())  // 데이터베이스에서 불러온 데이터로 목록 초기화
        adapter.notifyDataSetChanged()  // 데이터가 변경되었음을 알림
    }*/

    private fun filterItems(category: String) {
        val filteredList = if (category == "전체") {
            databaseHelper.getItems()   //전체 아이템 조회
        } else {
            databaseHelper.getItemsByCategory(category)   //특정 카테고리 아이템 조회
        }
        itemList.clear()
        itemList.addAll(filteredList)
        adapter.notifyDataSetChanged()   //RecyclerView 갱신
    }


    private fun filterItemsByCompletion(isCompleted: Boolean) {
        val filteredList = itemList.filter { it.isCompleted == isCompleted }
        adapter.updateList(filteredList.toMutableList())  // 필터링된 리스트로 어댑터 갱신
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val itemName = dialogView.findViewById<EditText>(R.id.et_item_name)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val quantity = dialogView.findViewById<EditText>(R.id.et_quantity)
        val memo = dialogView.findViewById<EditText>(R.id.et_memo)
        val addButton = dialogView.findViewById<Button>(R.id.btn_add)

        // 스피너 설정
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array, // Define categories in res/values/strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("물품 추가")
            .setView(dialogView)
            .create()

        // Set click listener for "등록" button inside dialog
        addButton.setOnClickListener {
            val name = itemName.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val quantityValue = quantity.text.toString().toIntOrNull() ?: 0
            val memoText = memo.text.toString()

            if (name.isNotEmpty()) {
                val newItem = Item(name = name, category = category, quantity = quantityValue, memo = memoText)
                // Add new item to database
                databaseHelper.addItem(newItem)

                // Add the new item to the itemList and notify the adapter
                itemList.add(newItem)
                adapter.notifyItemInserted(itemList.size - 1)  // Notify adapter of the new item

                dialog.dismiss() // Close dialog after adding item
            } else {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


}