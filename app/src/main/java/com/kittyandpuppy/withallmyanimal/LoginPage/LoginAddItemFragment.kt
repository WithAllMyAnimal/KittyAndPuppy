package com.kittyandpuppy.withallmyanimal.LoginPage

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kittyandpuppy.withallmyanimal.MainActivity
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentLoginAddItemBinding

class LoginAddItemFragment : DialogFragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentLoginAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        params?.width = (deviceWidth * 0.9).toInt()
        params?.height = (deviceHeight * 0.7).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        binding.saveDialogBtn.setOnClickListener {
            var isGoToJoin = true

            val email = binding.etIdHint.text.toString()
            val password1 = binding.etPwHint.text.toString()
            val password2 = binding.etPwHintCheck.text.toString()

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
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            val user = auth.currentUser
                        } else {
                            Toast.makeText(context, "실패", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

    }
}