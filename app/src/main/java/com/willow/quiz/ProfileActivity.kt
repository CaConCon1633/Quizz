package com.willow.quiz

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Data.UserPreferences
import com.willow.quiz.Models.UserInfor
import com.willow.quiz.Models.UserResponse
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar


@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var myCalendar: Calendar = Calendar.getInstance()
    @SuppressLint("SimpleDateFormat")
    var dateFormat: SimpleDateFormat? = SimpleDateFormat("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getUser()
        saveUserInfor()
        binding.btnBack.setOnClickListener {
            updateUI(this, MeActivity::class.java)
            finish()
        }


    }
    private fun getUser() {

        val userInfor = UserPreferences(this).getUserInfo()

        binding.editName.setText(userInfor["name"])

        userInfor["dob"]?.let {
            if (it != "null") binding.editDob.setText(it)
        }

        userInfor["phone"]?.let {
            if (it != "null") binding.editPhone.setText(it)
        }
        userInfor["gender"]?.toInt()?.let { gender ->
            when (gender) {
                0 -> binding.gender.check(R.id.male)
                1 -> binding.gender.check(R.id.female)
            }
        }
        val d = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.editDob.setText(dateFormat?.format(myCalendar.time))
        }
        binding.dob.setOnClickListener{

            DatePickerDialog(
                this, d,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

    }
    private fun saveUserInfor(){

        val token = SharedPrefManager.getToken(this).toString()
        val name = binding.editName.text.toString().trim()
        val dob = binding.editDob.text.toString()
        val email = UserPreferences(this).getUserInfo()["email"].toString()
        val phone = binding.editPhone.text.toString()
        var gender: Int = 0
        binding.gender.setOnCheckedChangeListener { group, checkedId ->
            gender = when (checkedId) {
                R.id.male -> 0
                R.id.female -> 1
                else -> 0
            }
        }
        binding.saveButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val userInfor = UserInfor(name, email, phone, dob, gender)
            Log.e(TAG, "saveUserInfor: $userInfor" )

            ApiClient.getRetrofitInstance().create(ApiSevices::class.java).putUser("Bearer $token",userInfor)
                .enqueue(object : Callback<UserResponse>{
                    override fun onResponse(p0: Call<UserResponse>, p1: Response<UserResponse>) {
                        if (p1.isSuccessful){
                            binding.progressBar.visibility = View.GONE
                            val data = p1.body()
                            Toast.makeText(this@ProfileActivity, data?.message, Toast.LENGTH_SHORT).show()
                            UserPreferences(this@ProfileActivity)
                                .updateUserInfo(
                                    name = name,
                                    email = email,
                                    dob = dob,
                                    phone = phone,
                                    gender = gender.toString())

                        }else{
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@ProfileActivity, "Update is not successful", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(p0: Call<UserResponse>, p1: Throwable) {
                        Toast.makeText(this@ProfileActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }
     fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        updateUI(this, MeActivity::class.java)
        finish()
    }
}