package com.kittyandpuppy.withallmyanimal.setting

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentSettingPassworddialogBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef

class SettingPasswordDialogFragment : DialogFragment() {

    private var _binding : FragmentSettingPassworddialogBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingPassworddialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPassworddialogDoublecheckbutton.setOnClickListener {
            val user = Firebase.auth.currentUser
            val currentPassword = binding.etPassworddialogEdittext1.text.toString()

            val email = user?.email
            val credential = email?.let { EmailAuthProvider.getCredential(it, currentPassword) }

            credential?.let {
                user.reauthenticate(it).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.saveDialogBtn.isEnabled = true
                        Toast.makeText(context, "현재 비밀번호가 확인되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.saveDialogBtn.isEnabled = false
                        Toast.makeText(context, "현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.saveDialogBtn.setOnClickListener {
            val newPassword = binding.etPassworddialogEdittext1.text.toString()
            val user = Firebase.auth.currentUser

            user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(context, "비밀번호 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.saveDialogBtn.isEnabled = false
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        params?.height = (deviceHeight * 0.6).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}
