package com.example.guru2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ItemFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private val itemList = mutableListOf<Item>()
    private lateinit var chkNeedeed: CheckBox
    private lateinit var chkCompleted: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트 레이아웃을 inflate
        return inflater.inflate(R.layout.fragment_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화
        recyclerView = view.findViewById(R.id.recyclerView)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_item)
        chkNeedeed = view.findViewById(R.id.checkBox_needeed)
        chkCompleted = view.findViewById(R.id.checkBox_completed)

        // DatabaseHelper 초기화
        databaseHelper = DatabaseHelper(requireContext())

        // 데이터베이스에서 items 불러오기
        itemList.addAll(databaseHelper.getItems())


        // 플로팅액션버튼 클릭 리스너
        fab.setOnClickListener {
            showAddItemDialog()
        }

        // 체크박스 리스너 설정
        chkNeedeed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 구매 필요 목록 필터링
                filterItemsByCompletion(false)
                chkCompleted.isChecked = false  // 구매 완료 체크박스 해제
            } else {
                // 체크박스가 해제되면 모든 아이템을 다시 보여줌
                adapter.updateList(itemList)
            }
        }

        chkCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 구매 완료 목록 필터링
                filterItemsByCompletion(true)
                chkNeedeed.isChecked = false  // 구매 필요 체크박스 해제
            } else {
                // 체크박스가 해제되면 모든 아이템을 다시 보여줌
                adapter.updateList(itemList)
            }
        }

        // RecyclerView 설정
        adapter = ItemAdapter(itemList, databaseHelper)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 카테고리 버튼 리스너 설정
        val btnAll = view.findViewById<Button>(R.id.btn_all)
        val btnFood = view.findViewById<Button>(R.id.btn_food)
        val btnDaily = view.findViewById<Button>(R.id.btn_daily)
        val btnClean = view.findViewById<Button>(R.id.btn_clean)
        val btnKitchen = view.findViewById<Button>(R.id.btn_kitchen)

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

    //카테고리 필터링
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

    //체크박스 필터링
    private fun filterItemsByCompletion(isCompleted: Boolean) {
        val filteredList = itemList.filter { it.isCompleted == isCompleted }
        adapter.updateList(filteredList.toMutableList())  // 필터링된 리스트로 어댑터 갱신
    }

    //물품 추가 다이얼로그
    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)
        val itemName = dialogView.findViewById<EditText>(R.id.et_item_name)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val quantity = dialogView.findViewById<EditText>(R.id.et_quantity)
        val memo = dialogView.findViewById<EditText>(R.id.et_memo)
        val addButton = dialogView.findViewById<Button>(R.id.btn_add)

        // 스피너 설정
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories_array, // Define categories in res/values/strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        val dialog = AlertDialog.Builder(requireContext())
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
                val newItem = Item(
                    name = name,
                    category = category,
                    quantity = quantityValue,
                    memo = memoText
                )
                // Add new item to database
                databaseHelper.addItem(newItem)

                // Add the new item to the itemList and notify the adapter
                itemList.add(newItem)
                adapter.notifyItemInserted(itemList.size - 1)  // Notify adapter of the new item

                //if (isAdded) dialog.dismiss() // Fragment에서 안전하게 dismiss()
                dialog.dismiss() // Close dialog after adding item
            } else {
                Toast.makeText(requireContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

}