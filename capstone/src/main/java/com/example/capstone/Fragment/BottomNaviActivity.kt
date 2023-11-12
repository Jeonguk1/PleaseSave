package com.example.capstone.Fragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.capstone.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNaviActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navi)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 앱 실행 시 초기 프래그먼트를 표시
        if (savedInstanceState == null) {
            val initialFragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, initialFragment)
                .commit()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            val fragment: Fragment = when (menuItem.itemId) {
                R.id.navigation_home -> HomeFragment()
                R.id.navigation_map -> MapFragment()
                R.id.navigation_list -> ListFragment()
                R.id.navigation_video -> VideoFragment()
                else -> HomeFragment() // 기본적으로 HomeFragment를 표시
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, fragment)
                .commit()
            true
        }
    }
}