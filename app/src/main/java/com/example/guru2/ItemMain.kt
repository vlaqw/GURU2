package com.example.guru2

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ItemMain : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private val itemList = mutableListOf<Item>()
    private lateinit var chk : CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_main)

        recyclerView = findViewById(R.id.recyclerView)
        val fab: FloatingActionButton = findViewById(R.id.fab_add_item)

        // dbhelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 데이터베이스에서 items 불러오기
        itemList.addAll(databaseHelper.getItems())


        // 플로팅액션버튼
        fab.setOnClickListener {
            showAddItemDialog()
        }

       chk = findViewById(R.id.checkBox)
        chk.setOnCheckedChangeListener(){ compoundButton: CompoundButton, b: Boolean ->

            if(chk.isChecked == true ) {
                recyclerView.visibility = android.view.View.VISIBLE
            }
        }

        adapter = ItemAdapter(itemList, databaseHelper)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

   /* private fun loadItemsFromDatabase() {
        // 데이터베이스에서 아이템을 불러와서 목록에 추가
        itemList.addAll(databaseHelper.getItems())  // 데이터베이스에서 불러온 데이터로 목록 초기화
        adapter.notifyDataSetChanged()  // 데이터가 변경되었음을 알림
    }*/

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
            .setNegativeButton("취소", null)
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