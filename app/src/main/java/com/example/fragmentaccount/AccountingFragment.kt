package com.example.fragmentaccount

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import android.content.Context

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountingFragment : Fragment(R.layout.fragment_accounting) {

    lateinit var addItemButton: FloatingActionButton
    lateinit var itemsContainer: LinearLayout
    lateinit var totalAmountTextView: TextView
    lateinit var amountPerPersonTextView: TextView
    lateinit var yearMonthTextView: TextView

    private var totalAmount = 0.0

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accounting, container, false)
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addItemButton = view.findViewById(R.id.addItemButton)
        itemsContainer = view.findViewById(R.id.itemsContainer)
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView)
        amountPerPersonTextView = view.findViewById(R.id.amountPerPersonTextView)
        yearMonthTextView = view.findViewById(R.id.yearMonthTextView)

        totalAmountTextView.text = "총액: 0원"

        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH) + 1
        yearMonthTextView.text = "${year}년 ${month}월 정산 내역이에요."

        addItemButton.setOnClickListener { addNewItem() }
    }

    private fun addNewItem() {
        val itemLayout = LinearLayout(requireContext())
        itemLayout.orientation = LinearLayout.HORIZONTAL
        itemLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        itemLayout.weightSum = 3f

        val nameEditText = EditText(requireContext())
        nameEditText.hint = "항목"
        nameEditText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val amountEditText = EditText(requireContext())
        amountEditText.hint = "금액"
        amountEditText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val deleteButton = Button(requireContext())
        deleteButton.text = "삭제"
        deleteButton.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        // 금액 입력란에서 값이 변경될 때마다 총액 갱신
        amountEditText.addTextChangedListener { text ->
            updateTotalAmount()  // 금액이 바뀔 때마다 총액 갱신
        }

        deleteButton.setOnClickListener {
            // 금액을 가져와서 총액에서 뺀다.
            val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
            totalAmount -= amount
            totalAmountTextView.text = "총액: ${totalAmount}원"

            // 항목 삭제
            itemsContainer.removeView(itemLayout)

        }

        itemLayout.addView(nameEditText)
        itemLayout.addView(amountEditText)
        itemLayout.addView(deleteButton)

        itemsContainer.addView(itemLayout)

    }

    private fun updateTotalAmount() {
        totalAmount = 0.0  // 초기화
        // 모든 항목을 반복하며 금액을 더함
        for (i in 0 until itemsContainer.childCount) {
            val child = itemsContainer.getChildAt(i)

            // child가 LinearLayout인지 확인
            if (child is LinearLayout) {
                val amountEditText = child.getChildAt(1) as? EditText  // 두 번째 자식 뷰가 금액 입력란

                // amountEditText가 null이 아닌지 확인한 후 값을 가져옴
                val amount = amountEditText?.text.toString().toDoubleOrNull() ?: 0.0
                totalAmount += amount  // 금액 더하기
            }
        }

        // 총액 표시
        totalAmountTextView.text = "총액: ${totalAmount}원"
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}