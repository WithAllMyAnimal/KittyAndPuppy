package com.kittyandpuppy.withallmyanimal.LoginPage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kittyandpuppy.withallmyanimal.MainActivity
import com.kittyandpuppy.withallmyanimal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        setContentView(binding.root)

        binding.btnLoginLogin.setOnClickListener {
            val email = binding.etLoginId.text.toString()
            val password = binding.etLoginPassword.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }

                }
        }
        binding.btnLoginSignup.setOnClickListener {
            val signupFragment = LoginAddItemFragment()
            signupFragment.show(supportFragmentManager, "signUpDialog")
        }
        binding.btnLoginResetPassword.setOnClickListener {
            val findPasswordFragment = FindPwFragment()
            findPasswordFragment.show(supportFragmentManager, "findPasswordDialog")

        }
    }
}