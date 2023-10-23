package com.kittyandpuppy.withallmyanimal

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.databinding.FragmentDialogProfilechangeBinding

class DialogProfileChange : DialogFragment() {
    private lateinit var binding: FragmentDialogProfilechangeBinding
    private lateinit var userRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogProfilechangeBinding.inflate(inflater, container, false)

        val database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")

        // 아이디 중복 확인 버튼
        binding.btnProfilechangeDoublecheckbutton.setOnClickListener {
            checkDuplicateId()
        }

        // 변경하기 버튼
        binding.btnSettinglogoutCheckbutton.setOnClickListener {
            saveUserInfoToDatabase()
            dismiss()
        }

        // 취소 버튼
        binding.btnSettinglogoutCancelbutton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }
    private fun checkDuplicateId() {
        val userIdname = binding.etProfilechangeNicknametext.text.toString()

        userRef.orderByChild("profile/userIdname").equalTo(userIdname).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(requireContext(), "중복된 아이디입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "사용가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val windowManager =
            requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (size.x * 0.9).toInt()
        params?.height = (size.y * 0.8).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams

    }

    private fun saveUserInfoToDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userIdname = binding.etProfilechangeNicknametext.text.toString()
            val petName = binding.etProfilechangePetname.text.toString()
            val birth = binding.etProfilechangePetbirthday.text.toString()
            val statusMessage = binding.etProfilechangeOnelinefeeling.text.toString()

            userRef.child(userId).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val profileMap = dataSnapshot.getValue() as? Map<String, Any>

                        val existingProfileMap: MutableMap<String, Any> =
                            profileMap?.toMutableMap() ?: mutableMapOf()

                        existingProfileMap["userIdname"] = userIdname
                        existingProfileMap["petName"] = petName
                        existingProfileMap["birth"] = birth
                        existingProfileMap["statusMessage"] = statusMessage

                        userRef.child(userId).child("profile").setValue(existingProfileMap)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
        } else {
            Toast.makeText(context, "유저 ID 없음", Toast.LENGTH_SHORT).show()
        }
    }
}