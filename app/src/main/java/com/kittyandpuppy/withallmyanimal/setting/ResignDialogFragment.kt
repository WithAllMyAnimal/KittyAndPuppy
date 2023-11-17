package com.kittyandpuppy.withallmyanimal.setting

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kittyandpuppy.withallmyanimal.LoginPage.LoginActivity
import com.kittyandpuppy.withallmyanimal.databinding.FragmentResignDialogBinding


class ResignDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentResignDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResignDialogBinding.inflate(inflater, container,false)


        // 취소 버튼
        binding.btnCanceldialogCancelbutton.setOnClickListener{
            dismiss()
        }

        // 회원탈퇴 버튼
        binding.btnCanceldialogCheckbutton.setOnClickListener {
            val user = Firebase.auth.currentUser
            user?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val databaseRef = Firebase.database.reference
                        val userRef = databaseRef.child("users").child(user.uid.orEmpty())
                        userRef.removeValue()

                        Firebase.auth.signOut()
                        dismiss()
                        Toast.makeText(requireContext(), "탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(),"탈퇴에 실패했습니다.",Toast.LENGTH_SHORT).show()
                    }
                }
        }

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
        params?.width = (deviceWidth * 0.95).toInt()
        params?.height = (deviceHeight * 0.35).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
    companion object {

        fun newInstance(param1: String, param2: String) =
            ResignDialogFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}