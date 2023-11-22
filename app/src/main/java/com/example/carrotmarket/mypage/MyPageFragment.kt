package com.example.carrotmarket.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.carrotmarket.R
import com.example.carrotmarket.SignupActivity
import com.example.carrotmarket.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyPageFragment: Fragment(R.layout.fragment_mypage){

    private var binding: FragmentMypageBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentMypageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMypageBinding

        fragmentMypageBinding.emailEditText.addTextChangedListener {
            updateSignInButtonState()
        }

        fragmentMypageBinding.passwordEditText.addTextChangedListener {
            updateSignInButtonState()
        }

        fragmentMypageBinding.signInOutButton.setOnClickListener {
            if (auth.currentUser == null) {
                // 로그인 처리
                performLogin()
            } else {
                // 로그아웃 처리
                auth.signOut()
                updateUIForLoggedOut()
            }
        }


        fragmentMypageBinding.signUpButton.setOnClickListener {
            val intent = Intent(activity, SignupActivity::class.java)
            startActivity(intent)
        }

        fragmentMypageBinding.emailEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
                binding.signInOutButton.isEnabled = enable
                //binding.signUpButton.isEnabled = enable
            }
        }

        fragmentMypageBinding.passwordEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
                binding.signUpButton.isEnabled = enable
                //binding.signInOutButton.isEnabled = enable
            }
        }
    }

    private fun performLogin() {
        // 로그인 로직 구현
        binding?.let { binding ->
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        successSignIn()
                    } else {
                        Toast.makeText(context, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser == null) {
            binding?.let { binding ->
                binding.emailEditText.text.clear()
                binding.emailEditText.isEnabled = true
                binding.passwordEditText.text.clear()
                binding.passwordEditText.isEnabled = true

                binding.signInOutButton.text = "로그인"
                binding.signInOutButton.isEnabled = false
                binding.signUpButton.isEnabled = true  // 로그인하지 않은 상태에서는 회원가입 버튼 활성화
            }
        } else {
            binding?.let { binding ->
                binding.emailEditText.setText(auth.currentUser?.email)
                binding.passwordEditText.setText("**********")
                binding.emailEditText.isEnabled = false
                binding.passwordEditText.isEnabled = false

                binding.signInOutButton.text = "로그아웃"
                binding.signInOutButton.isEnabled = true
                binding.signUpButton.isEnabled = false  // 로그인된 상태에서는 회원가입 버튼 비활성화
            }
        }
    }

    private fun successSignIn() {
        if (auth.currentUser == null) {
            Toast.makeText(context, "로그인에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        binding?.emailEditText?.isEnabled = false
        binding?.passwordEditText?.isEnabled = false
        binding?.signUpButton?.isEnabled = false
        binding?.signInOutButton?.text = "로그아웃"
        Toast.makeText(context, "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show()

    }

    private fun updateUIForLoggedOut() {
        binding?.let { binding ->
            binding.emailEditText.text.clear()
            binding.emailEditText.isEnabled = true
            binding.passwordEditText.text.clear()
            binding.passwordEditText.isEnabled = true
            binding.signInOutButton.text = "로그인"
            binding.signUpButton.isEnabled = true
            Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSignInButtonState() {
        binding?.let { binding ->
            val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
            binding.signInOutButton.isEnabled = enable
        }
    }

}