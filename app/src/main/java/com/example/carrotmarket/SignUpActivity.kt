package com.example.carrotmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val createAccountButton = findViewById<Button>(R.id.createAccount)

        createAccountButton.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val name = findViewById<EditText>(R.id.userName).text.toString()
        val birthdate = findViewById<EditText>(R.id.birthdate).text.toString()
        val email = findViewById<EditText>(R.id.signupEmail).text.toString()
        val password = findViewById<EditText>(R.id.signupPassword).text.toString()

        // Firebase Authentication을 사용하여 사용자를 생성
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 사용자 계정 생성 성공, 필요하다면 추가 정보 저장 로직 구현
                    // 예를 들면, Firestore에 사용자 이름과 생년월일 저장

                    // 로그인 상태로 메인 액티비티로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // 에러 처리
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
