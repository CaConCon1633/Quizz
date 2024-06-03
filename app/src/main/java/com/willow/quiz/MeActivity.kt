package com.willow.quiz

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Data.UserPreferences
import com.willow.quiz.Models.ShortId
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityMeBinding
import com.willow.quiz.databinding.InforExamLayoutBinding
import com.willow.quiz.databinding.ResultExamLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class MeActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMeBinding
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranter: Boolean ->
            if (isGranter) {
                showCamera()
            } else {
                Toast.makeText(this@MeActivity, "An error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            run {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    setResult(result.contents)
                }
            }
        }
    var examId = ""

    private fun setResult(link: String) {
        if (link.length > 5) {
            val codeExam = link.split("/")[4]
            Log.e(TAG, codeExam)
            getExam(codeExam)

        } else {
            Toast.makeText(this, "Error link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCamera() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR code")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        scanLauncher.launch(options)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClickMenu()
        setActivity()
        binding.txtScan.setOnClickListener {
            checkPermissionCamera(this)

        }
        setOnClickMenu()
        getUser()

    }

    private fun getUser() {

        val userPrefs = UserPreferences(this)
        val userInfor = userPrefs.getUserInfo()

        binding.loading.visibility = View.GONE
        binding.userName.visibility = View.VISIBLE
        binding.userEmail.visibility = View.VISIBLE

        binding.userName.text = userInfor["name"]
        binding.userEmail.text = userInfor["email"]
    }

    private fun checkPermissionCamera(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showCamera()
        } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun setOnClickMenu() {
        binding.bottomNavigationView.setItemSelected(R.id.me, true)
        binding.bottomNavigationView.setOnItemSelectedListener {
            if (it == R.id.about) {
                updateUI(this, AboutActivity::class.java)
                finish()
            } else if (it == R.id.home) {
                updateUI(this, MainActivity::class.java)
                finish()
            }
        }
    }

    private fun setActivity() {
        binding.txtProfile.setOnClickListener {
            updateUI(this, ProfileActivity::class.java)
        }
        binding.txtJoin.setOnClickListener {
            updateUI(this, JoinActivity::class.java)
        }
        binding.txtAdd.setOnClickListener {
            updateUI(this, CreateExamActivity::class.java)
        }
        binding.txtExams.setOnClickListener {
            updateUI(this, ExamsActivity::class.java)
        }
        binding.txtLogout.setOnClickListener {
            SharedPrefManager.deleteToken(this)
            UserPreferences(this).deleteUserInfo()
            updateUI(this, MainActivity::class.java)
        }
    }

    private fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        updateUI(this, MainActivity::class.java)
        finish()
    }

    private fun getExam(codeExam: String) {

        val token = SharedPrefManager.getToken(this)


        Log.e(TAG, "joinExam: $codeExam")


        binding.progressBar.visibility = View.VISIBLE

        ApiClient.getRetrofitInstance().create(ApiSevices::class.java).getExamId(
            "Bearer $token",
            codeExam
        )
            .enqueue(object : Callback<ShortId> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(p0: Call<ShortId>, p1: Response<ShortId>) {
                    if (p1.isSuccessful) {
                        binding.progressBar.visibility = View.GONE
                        binding.scrollView2.visibility = View.VISIBLE
                        val data = p1.body()
                        examId = data?.examId.toString()
                        Log.d(TAG, "examId: $examId")
                        if (data?.result != null) {

                            //If the user participates in the exam, the exam results will be displayed
                            val builder = Dialog(
                                this@MeActivity,
                                android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen
                            )
                            builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            val binding = ResultExamLayoutBinding.inflate(layoutInflater)
                            builder.setContentView(binding.root)

                            binding.txtCountQuestions.text =
                                "You get: " + data.countCorrect.toString() + "/" + data.countQuestions.toString()
                            binding.txtResult.text = "Score: " + data.result.toString()

                            binding.btnCancel.setOnClickListener {
                                builder.dismiss()
                            }
                            builder.show()
                        }
                        if (data?.examId != null) {

                            val totalSecs = data.duration!!.toInt()
                            val hours = totalSecs / 3600
                            val minutes = (totalSecs % 3600) / 60
                            val seconds = totalSecs % 60

                            val builder = Dialog(
                                this@MeActivity,
                                android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen
                            )

                            builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            val time =
                                hours.toString() + "h " + minutes.toString() + "m " + seconds.toString() + "s"

                            val binding: InforExamLayoutBinding =
                                InforExamLayoutBinding.inflate(layoutInflater)
                            builder.setContentView(binding.root)

                            binding.txtExam.text = "Exam: " + data.title.toString()
                            if (data.description == null) {
                                binding.txtDescription.text = "Description: "
                            } else {
                                binding.txtDescription.text =
                                    "Description: " + data.description.toString()
                            }
                            binding.txtDuration.text = "Duration: $time"
                            binding.txtQuestion.text =
                                "Question: " + data.questionsCount.toString()
                            if(SharedPrefManager.getToken(this@MeActivity) == null){
                                binding.txtExaminee.text = "The result only saves if you logged"
                                binding.txtExaminee.setTextColor(getColor(R.color.dark_red))
                            }else {
                                binding.txtExaminee.text = "Examinee: " + UserPreferences(this@MeActivity).getUserInfo()["name"]
                            }

                            binding.btnJoin.setOnClickListener {
                                val intent = Intent(this@MeActivity, ExamActivity::class.java)
                                intent.putExtra("getExamId", examId)
                                intent.putExtra("getDuration", data.duration)
                                intent.putExtra("getQuestionsCount", data.questionsCount)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NO_HISTORY
                                startActivity(intent)
                            }

                            binding.btnCancel.setOnClickListener {
                                builder.dismiss()
                            }

                            builder.show()
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.scrollView2.visibility = View.VISIBLE
                        Toast.makeText(this@MeActivity, "Exam not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(p0: Call<ShortId>, p1: Throwable) {
                    Log.e(TAG, "onFailure: $p1")
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView2.visibility = View.VISIBLE
                    Toast.makeText(this@MeActivity, "An error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}
