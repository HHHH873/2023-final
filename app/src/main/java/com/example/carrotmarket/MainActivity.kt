package com.example.carrotmarket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.carrotmarket.chatlist.ChatListFragment
import com.example.carrotmarket.home.HomeFragment
import com.example.carrotmarket.mypage.MyPageFragment
import com.facebook.login.widget.LoginButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        replaceFragment(MyPageFragment())  // 기본으로 MyPageFragment를 표시

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home, R.id.chatList -> {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        // 로그인하지 않았을 경우 스낵바 표시
                        showSnackbar("로그인 후 사용가능합니다")
                        false // 이벤트 처리를 여기서 종료 (페이지 전환 방지)
                    } else {
                        // 로그인했을 경우 해당 프래그먼트로 전환
                        replaceFragment(
                            when (it.itemId) {
                                R.id.home -> HomeFragment()
                                R.id.chatList -> ChatListFragment()
                                else -> MyPageFragment() // 예비 처리
                            }
                        )
                        true // 이벤트 처리 계속 (페이지 전환 허용)
                    }
                }
                R.id.myPage -> {
                    // 마이페이지는 로그인 상태와 관계없이 항상 전환
                    replaceFragment(MyPageFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.fragment_container), message, Snackbar.LENGTH_SHORT).show()
    }
}
