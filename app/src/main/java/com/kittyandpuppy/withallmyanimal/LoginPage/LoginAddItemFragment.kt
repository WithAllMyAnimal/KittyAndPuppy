package com.kittyandpuppy.withallmyanimal.LoginPage

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kittyandpuppy.withallmyanimal.MainActivity
import com.kittyandpuppy.withallmyanimal.databinding.FragmentLoginAddItemBinding

class LoginAddItemFragment : DialogFragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentLoginAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        //다이얼로그 size
        val windowManager =
            requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        val deviceWidth = size.x
        val deviceHeight = size.y
        params?.width = (deviceWidth * 1.0).toInt()
        params?.height = (deviceHeight * 0.65).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        binding.btnSaveDialog.setOnClickListener {
            var isGoToJoin = true

            val email = binding.etLoginAddItemIdHint.text.toString()
            val password1 = binding.etLoginAddItemPwHint.text.toString()
            val password2 = binding.etLoginAddItemPwHintCheck.text.toString()

            if (email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
                Toast.makeText(context, "이메일 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if (password1 != password2) {
                Toast.makeText(context, "비밀번호를 똑같이 입력해주세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if (password1.length < 6) {
                Toast.makeText(context, "비밀번호를 6자리 이상으로 입력해주세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if (isGoToJoin) {
                auth.createUserWithEmailAndPassword(email, password1)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "성공", Toast.LENGTH_LONG).show()

                            val userId = auth.currentUser?.uid

                            val database = FirebaseDatabase.getInstance()

                            userId?.let {
                                database.getReference("users").child(it).child("email")
                                    .setValue(email)
                                    .addOnSuccessListener { Log.d(TAG, "이메일 저장!") }
                                    .addOnFailureListener { e -> Log.e(TAG, "이메일 저장 실패!", e) }


                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            dismiss()
                        }
                        }
                    }
            }
        }

        binding.btnLoginSignup.setOnClickListener {
            val email = binding.etLoginAddItemIdHint.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(context, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = Firebase.database
            val usersRef = database.getReference("users")
            val emailRef = usersRef.orderByChild("email").equalTo(email)

            emailRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(context, "이미 사용 중인 이메일입니다", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "사용 가능한 이메일입니다", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Database Error: ${databaseError.toException()}")
                    Toast.makeText(context, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }


}