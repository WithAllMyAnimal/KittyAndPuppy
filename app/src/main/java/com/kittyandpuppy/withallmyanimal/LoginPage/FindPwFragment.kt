package com.kittyandpuppy.withallmyanimal.LoginPage

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.databinding.FragmentFindPwBinding

class FindPwFragment : DialogFragment() {

    private lateinit var binding: FragmentFindPwBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        binding = FragmentFindPwBinding.inflate(inflater)

        builder.setView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnFindPwSaveDialog.setOnClickListener {
            val email = binding.etLoginAddItemIdHint.text.toString()
            val passwordHintQuestion = binding.etLoginAddItemPwHintQuestion.text.toString()
            val newPassword = binding.etLoginAddItemPwHint.text.toString()

            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (!task.result?.signInMethods.isNullOrEmpty()) {
                            val query = FirebaseDatabase.getInstance().getReference("users")
                                .orderByChild("email").equalTo(email)

                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    var isHintMatched = false

                                    for (snapshot in dataSnapshot.children) {
                                        val userPasswordHint =
                                            snapshot.child("password_hint").value as? String

                                        if (passwordHintQuestion == userPasswordHint) {
                                            isHintMatched = true
                                            break
                                        }
                                    }

                                    if (isHintMatched) {
                                        auth.sendPasswordResetEmail(email)
                                            .addOnCompleteListener { resetTask ->
                                                if (resetTask.isSuccessful) {
                                                    showSuccessMessage()
                                                    moveToLoginPage()
                                                } else {
                                                    showErrorMessage(resetTask.exception?.message)
                                                }
                                            }
                                    } else {
                                        showErrorMessage("힌트가 일치하지 않습니다.")
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                        } else {
                            showErrorMessage("해당 이메일 사용자가 없습니다.")
                        }
                    } else {
                        showErrorMessage(task.exception?.message)
                    }
                }
        }

        return builder.create()
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
        params?.height = (size.y * 0.7).toInt()

        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    private fun showSuccessMessage() {
        Toast.makeText(context, "비밀번호 변경 성공!", Toast.LENGTH_LONG).show()
    }

    private fun showErrorMessage(errorMessage: String?) {
        Toast.makeText(context, errorMessage ?: "비밀번호 변경 실패!", Toast.LENGTH_LONG).show()
    }

    // 비밀번호 변경 성공 시 로그인 페이지로 이동
    private fun moveToLoginPage() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}