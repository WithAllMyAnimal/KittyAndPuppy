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
    private lateinit var mDbRef: DatabaseReference
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
        // 다이얼로그 크기 조정
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
        mDbRef = FirebaseDatabase.getInstance().reference

        binding.btnLoginSignup.setOnClickListener {
            val email = binding.etFindPwIdHint.text.toString()
            if (email.isNotEmpty()) {
                checkEmailDuplicate(email)
            } else {
                Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkEmailDuplicate(email: String) {
        mDbRef.child("emails").orderByValue().equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(context, "중복된 이메일입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "이메일 중복 확인에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}