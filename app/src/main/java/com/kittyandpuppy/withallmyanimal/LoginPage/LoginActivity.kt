package com.kittyandpuppy.withallmyanimal.LoginPage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.kittyandpuppy.withallmyanimal.MainActivity
import com.kittyandpuppy.withallmyanimal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()

            database.getReference("users").child(userId).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userIdname = dataSnapshot.child("userIdname").getValue(String::class.java)
                        val petName = dataSnapshot.child("petName").getValue(String::class.java)
                        val birth = dataSnapshot.child("birth").getValue(String::class.java)
                        val dogCat = dataSnapshot.child("dogcat").getValue(String::class.java)

                        if (userIdname != null && petName != null && birth != null && dogCat != null) {
                            startActivity(
                                Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                            finish()
                        } else {
                            startActivity(
                                Intent(this@LoginActivity, DogAndCatAddActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        } else {
            setContentView(binding.root)

            binding.btnLoginSignup.setOnClickListener {
                val signupFragment = LoginAddItemFragment()
                signupFragment.show(supportFragmentManager, "signUpDialog")
            }
            binding.btnLoginResetPassword.setOnClickListener {
                val findPasswordFragment = FindPwFragment()
                findPasswordFragment.show(supportFragmentManager, "findPasswordDialog")
            }

            binding.btnLoginLogin.setOnClickListener {
                val email = binding.etLoginId.text.toString()
                val password = binding.etLoginPassword.text.toString()

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                            val database = FirebaseDatabase.getInstance()

                            database.getReference("users").child(userId).child("profile")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val userIdname = dataSnapshot.child("userIdname")
                                            .getValue(String::class.java)
                                        val petName = dataSnapshot.child("petName")
                                            .getValue(String::class.java)
                                        val birth =
                                            dataSnapshot.child("birth").getValue(String::class.java)
                                        val dogCat = dataSnapshot.child("dogcat")
                                            .getValue(String::class.java)

                                        if (userIdname != null && petName != null && birth != null && dogCat != null) {
                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    MainActivity::class.java
                                                ).apply {
                                                    flags =
                                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                            )
                                            Toast.makeText(
                                                this@LoginActivity,
                                                "로그인 성공",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    DogAndCatAddActivity::class.java
                                                ).apply {
                                                    flags =
                                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                            )
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                        } else {
                            Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
