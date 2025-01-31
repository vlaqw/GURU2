package com.example.guru2
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.os.Bundle
import android.view.View

class ItemsFragment : Fragment(R.layout.activity_item_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ImageButton 클릭 시 itemMainFragment로 이동
        val navController = findNavController()
        val buttonHomeImage = view.findViewById<ImageButton>(R.id.homeImage)

        // 키보드 숨기기
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)

        buttonHomeImage.setOnClickListener {
            navController.navigate(R.id.action_itemsMainFragment_to_HomeFragment)
        }
        //action_itemsMainFragment_to_homeFragment
    }
}