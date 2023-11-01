package com.kittyandpuppy.withallmyanimal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kittyandpuppy.withallmyanimal.databinding.FragmentResignationBinding

class ResignationFragment : Fragment() {

    private lateinit var binding : FragmentResignationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResignationBinding.inflate(inflater, container, false)
        val view = binding.root


        val checkBox = binding.cbResignationCheckbox
        val resignButton = binding.btnResignationResignbutton
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            // 체크 박스가 선택되었을 때 버튼을 활성화

            resignButton.isEnabled = isChecked
        }

        // 초기 상태에서 버튼 비활성화
        resignButton.isEnabled = false

        resignButton.setOnClickListener {
            val resignDialogFragment = ResignDialogFragment()
            resignDialogFragment.show(requireFragmentManager(), "ResignDialog")
        }

        binding.btnResignationCanclebutton.setOnClickListener{
            val fragmentManager = requireFragmentManager()
            val transaction = fragmentManager.beginTransaction()
            transaction.remove(this)
            transaction.commit()
        }


        return view
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            ResignationFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}