package com.kittyandpuppy.withallmyanimal

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.kittyandpuppy.withallmyanimal.LoginPage.LoginActivity
import com.kittyandpuppy.withallmyanimal.databinding.FragmentSettingLogoutBinding
import com.kittyandpuppy.withallmyanimal.home.HomeFragment

class SettingLogoutFragment : DialogFragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentSettingLogoutBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingLogoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.btnSettinglogoutCancelbutton.setOnClickListener{
            dismiss()
        }
        binding.btnSettinglogoutCheckbutton.setOnClickListener{
            auth.signOut()
            val intent: Intent = if (auth.currentUser == null) {
                Intent(requireContext(), LoginActivity::class.java)
            } else {
                Intent(requireContext(), HomeFragment::class.java)
            }

            // 기존 액티비티 새로운 액티비티를 시작
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dismiss()
        }
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
        params?.height = (deviceHeight * 0.35).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}
