package com.example.guru2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.guru2.ItemMain
import com.example.guru2.R

class ItemMainActivity : AppCompatActivity() {

    private lateinit var buttonFragment: Button
    private lateinit var buttonItemMain: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonFragment = findViewById(R.id.btn_manage_items) //버튼 ID
        buttonFragment.setOnClickListener {
            val fragment = ItemFragment()//프래그먼트 생성
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment) //컨테이너에 프래먼트 추가
                .addToBackStack(null) // BackStack에 추가하여 뒤로가기가 가능하도록 설정
                .commit()
        }


        buttonItemMain = findViewById(R.id.btn_open_item_page)
        buttonItemMain.setOnClickListener {
            val intent = Intent(this, ItemMain::class.java)
            startActivity(intent)
        }
    }
}