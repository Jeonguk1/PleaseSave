package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.Fragment.BottomNaviActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class SignupActivity : AppCompatActivity() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText
    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var nicknameEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val signUpButton: Button = findViewById(R.id.signup_button)
        emailEditText = findViewById(R.id.ID_textField_editText)
        passwordEditText = findViewById(R.id.PW_textField_editText)
        confirmPasswordEditText = findViewById(R.id.PW_Re_textField_editText)
        nameEditText = findViewById(R.id.username_editText)
        phoneNumberEditText = findViewById(R.id.user_phone_number_editText)
        nicknameEditText = findViewById(R.id.user_nickname_editText)

        signUpButton.setOnClickListener {
            if (validateForm()) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val name = nameEditText.text.toString()
                val phoneNumber = phoneNumberEditText.text.toString()
                val username = nicknameEditText.text.toString()

                val url ="http://54.180.124.108:3306/signup3" //3306 or 3000
                val json = JsonObject().apply {
                    addProperty("email", email)
                    addProperty("password", password)
                    addProperty("phone_number", phoneNumber)
                    addProperty("name", name)
                    addProperty("username", username)
                }
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = json.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                // Use CoroutineScope for asynchronous network request
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response: Response = withContext(Dispatchers.IO) {
                            OkHttpClient().newCall(request).execute()
                        }

                        if (response.isSuccessful) {
                            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                            startActivity(intent)
                            runOnUiThread {
                                Toast.makeText(this@SignupActivity, "회원가입 성공입니다!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@SignupActivity, "회원가입 실패입니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@SignupActivity, "오류 발생입니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val name = nameEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        val username = nicknameEditText.text.toString()
        var valid = true

        if (!isValidEmail(email)) {
            emailEditText.error = "올바른 이메일을 입력하세요."
            valid = false
        } else {
            emailEditText.error = null
        }

        if (password.length < 7) {
            passwordEditText.error = "비밀번호는 7자이상 입력해주세요."
            valid = false
        } else {
            passwordEditText.error = null
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.error = "비밀번호가 일치하지 않습니다."
            valid = false
        } else {
            confirmPasswordEditText.error = null
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            phoneNumberEditText.error = "올바른 핸드폰 번호를 입력하세요."
            valid = false
        } else {
            phoneNumberEditText.error = null
        }

        if (name.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "빈칸을 입력하세요.", Toast.LENGTH_SHORT).show()
            valid = false
        }

        return valid
    }


    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}"
        return email.matches(emailPattern.toRegex())
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneNumberPattern = "^01[0-9]{8,9}$"
        return phoneNumber.matches(phoneNumberPattern.toRegex())
    }
}