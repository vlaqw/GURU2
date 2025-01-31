package com.example.fragmentaccount

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // 메뉴 파일 연결
        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu)

        // BottomNavigationView의 아이템 선택 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_items -> {
                    replaceFragment(ItemFragment())
                    true
                }
                R.id.nav_accounting -> {
                    replaceFragment(AccountingFragment())
                    true
                }
                else -> false
            }
        }

        // 기본 프래그먼트 표시
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())  // 기본적으로 홈 화면을 표시
        }
    }

    private fun replaceFragment(fragment: Fragment) { // 프래그먼트를 교체하는 함수
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // 지정된 컨테이너에 새로운 프래그먼트로 교체
            .commit()
    }
}