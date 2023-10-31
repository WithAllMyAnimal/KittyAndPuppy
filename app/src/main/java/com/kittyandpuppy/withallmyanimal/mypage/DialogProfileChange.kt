package com.kittyandpuppy.withallmyanimal.mypage

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.databinding.FragmentDialogProfilechangeBinding
import com.kittyandpuppy.withallmyanimal.util.Constants
import java.util.Calendar

class DialogProfileChange : DialogFragment() {
    private lateinit var binding: FragmentDialogProfilechangeBinding
    private lateinit var userRef: DatabaseReference
    private val GALLERY_REQUEST_CODE = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogProfilechangeBinding.inflate(inflater, container, false)

        val database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val storage = Firebase.storage
        val storageRef = storage.reference

        if (userId != null) {
            val imageRef = storageRef.child("$userId.png")
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                binding.ivCircleMy.load(uri)
            }

            binding.ivCircleMy.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, GALLERY_REQUEST_CODE)
            }

            userRef.child(userId).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val profileMap = dataSnapshot.getValue() as? Map<String, Any>

                        val retrievedUserIdname = profileMap?.get("userIdname") as? String
                        val retrievedPetName = profileMap?.get("petName") as? String
                        val retrievedBirth = profileMap?.get("birth") as? String
//                        val retrievedStatusMessage = profileMap?.get("statusMessage") as? String

                        binding.etProfilechangeNicknametext.text =
                            if (retrievedUserIdname != null) Editable.Factory.getInstance()
                                .newEditable(retrievedUserIdname) else null
                        binding.etProfilechangePetname.text =
                            if (retrievedPetName != null) Editable.Factory.getInstance()
                                .newEditable(retrievedPetName) else null
                        // 생일 DatePicker
                        binding.etProfilechangePetbirthday.setOnClickListener {
                            val c = Calendar.getInstance()

                            val year = c.get(Calendar.YEAR)
                            val month = c.get(Calendar.MONTH)
                            val day = c.get(Calendar.DAY_OF_MONTH)

                            val datePickerDialog = DatePickerDialog(
                                requireContext(),
                                { view, selectedYear, selectedMonthOfYear, selectedDayOfMonth ->
                                    val selectedDate =
                                        "$selectedYear.${selectedMonthOfYear + 1}.$selectedDayOfMonth"
                                    binding.etProfilechangePetbirthday.setText(selectedDate)
                                },
                                year,
                                month,
                                day
                            )
                            datePickerDialog.show()
//                        binding.etProfilechangeOnelinefeeling.text =
//                            if (retrievedStatusMessage != null) Editable.Factory.getInstance()
//                                .newEditable(retrievedStatusMessage) else null
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
        }

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
        val selectedImage = arguments?.getParcelable<Uri>("selectedImage")
        if (selectedImage != null) {
            binding.ivCircleMy.setImageURI(selectedImage)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val selectedImage = data.data

                if (selectedImage != null) {
                    binding.ivCircleMy.setImageURI(selectedImage)
                    uploadImageToFirebase(selectedImage)
                }
            }
        }
    }

    private fun checkDuplicateId() {
        val userIdname = binding.etProfilechangeNicknametext.text.toString()

        userRef.orderByChild("profile/userIdname").equalTo(userIdname)
            .addListenerForSingleValueEvent(object :
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
        params?.height = (size.y * 0.7).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    private fun saveUserInfoToDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userIdname = binding.etProfilechangeNicknametext.text.toString()
            val petName = binding.etProfilechangePetname.text.toString()
            val birth = binding.etProfilechangePetbirthday.text.toString()
//            val statusMessage = binding.etProfilechangeOnelinefeeling.text.toString()

            userRef.child(userId).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val profileMap = dataSnapshot.getValue() as? Map<String, Any>

                        val existingProfileMap: MutableMap<String, Any> =
                            profileMap?.toMutableMap() ?: mutableMapOf()

                        existingProfileMap["userIdname"] = userIdname
                        existingProfileMap["petName"] = petName
                        existingProfileMap["birth"] = birth
//                        existingProfileMap["statusMessage"] = statusMessage

                        userRef.child(userId).child("profile").setValue(existingProfileMap)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
            val userProfileImage = Firebase.storage.reference.child("profileImages")
                .child("${Constants.currentUserUid}.png")
            userProfileImage.downloadUrl.addOnSuccessListener { uri ->
                binding.ivCircleMy.load(uri.toString()) {
                    crossfade(true)
                }
            }
        } else {
            Toast.makeText(context, "유저 ID 없음", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(uri: Uri?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null && uri != null) {
            val storageRef = Firebase.storage.reference.child("profileImages/$userId.png")

            val uploadTask = storageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                Constants.currentUserProfileImg = uri
                binding.ivCircleMy.load(uri)
                storageRef.downloadUrl.addOnSuccessListener {
                    binding.ivCircleMy.load(it)
                }
            }.addOnFailureListener {
            }
        }
    }
}