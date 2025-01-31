package com.example.guru2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class AccountingFragment : Fragment(R.layout.fragment_accounting) {

    lateinit var addItemButton: FloatingActionButton // 항목 추가 버튼
    lateinit var itemsContainer: LinearLayout // 항목들이 추가될 컨테이너
    lateinit var totalAmountTextView: TextView // 총액을 표시하는 TextView
    lateinit var amountPerPersonTextView: TextView // 1인당 금액을 표시할 TextView
    lateinit var yearMonthTextView: TextView // 현재의 연도와 월을 표시하는 TextView

    private var totalAmount = 0.0 // 총액을 저장하는 변수

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addItemButton = view.findViewById(R.id.addItemButton)
        itemsContainer = view.findViewById(R.id.itemsContainer)
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView)
        amountPerPersonTextView = view.findViewById(R.id.amountPerPersonTextView)
        yearMonthTextView = view.findViewById(R.id.yearMonthTextView)

        totalAmountTextView.text = "총액: 0원"

        // 현재 연도와 월을 가져와서 화면에 표시
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH) + 1
        yearMonthTextView.text = "${year}년 ${month}월 정산 내역이에요."

        // 항목 추가 버튼 클릭 시 새로운 항목 추가 함수 호출
        addItemButton.setOnClickListener { addNewItem() }
    }

    // 새로운 항목을 추가하는 함수
    private fun addNewItem() {
        // 새로운 항목을 위한 레이아웃을 생성
        val itemLayout = LinearLayout(requireContext())
        itemLayout.orientation = LinearLayout.HORIZONTAL
        itemLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        itemLayout.weightSum = 3f

        // 항목 이름 입력란
        val nameEditText = EditText(requireContext())
        nameEditText.hint = "항목"
        nameEditText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        // 금액 입력란
        val amountEditText = EditText(requireContext())
        amountEditText.hint = "금액"
        amountEditText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        // 항목 삭제 버튼
        val deleteButton = Button(requireContext())
        deleteButton.text = "삭제"
        deleteButton.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

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
    }

}