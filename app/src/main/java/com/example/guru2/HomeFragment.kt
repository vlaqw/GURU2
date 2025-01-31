package com.example.guru2
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.os.Bundle
import android.view.View

class HomeFragment : Fragment(R.layout.homelayout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 키보드 숨기기
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)

        // ImageButton 클릭 시 아이템 프래그먼트로 이동
        val navController = findNavController()
        val buttonHomeImage = view.findViewById<ImageButton>(R.id.homeImage)

        buttonHomeImage.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_itemMainFragment)
        }
    }
}