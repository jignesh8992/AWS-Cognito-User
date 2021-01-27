package com.example.awscognito.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
import com.example.awscognito.R
import com.example.awscognito.aws.CognitoSettings
import com.example.awscognito.utilities.KEY_USERNAME
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.isOnline
import com.example.jdrodi.utilities.isValidPassword
import com.example.jdrodi.utilities.showSnackbar
import com.printres.customize.mobile.cover.tshirt.print.design.aws.*
import kotlinx.android.synthetic.main.activity_forget_password.*

// the activity initialization parameters
private val TAG = ForgetPasswordActivity::class.java.simpleName

class ForgetPasswordActivity : BaseActivity() {

    private lateinit var resultContinuation: ForgotPasswordContinuation

    companion object {
        fun getIntent(mContext: Context, userName: String): Intent {
            val intent = Intent(mContext, ForgetPasswordActivity::class.java)
            intent.putExtra(KEY_USERNAME, userName)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
    }

    override fun getContext(): Activity {
        return this@ForgetPasswordActivity
    }


    override fun initData() {
        if (intent != null) {
            val userName: String? = intent.getStringExtra(KEY_USERNAME)
            if (userName != null && !userName.isBlank()) {
                etUserName.setText(userName)
                etUserName.setSelection(userName.length)
            }
        }
    }

    override fun initActions() {
        ivBack.setOnClickListener(this)
        btnGetVerification.setOnClickListener(this)
        btnReset.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        super.onClick(view)
        when (view) {
            ivBack -> onBackPressed()
            btnGetVerification -> getVerificationCode()
            btnReset -> resetPassword()
        }
    }


    private fun getVerificationCode() {
        hideKeyboard()
        val userName = etUserName.text.toString()
        if (userName.isBlank()) {
            etUserName.error = getString(R.string.write_username)
            return
        }
        if (userName.length < 5) {
            tilUserName.error = getString(R.string.invalid_username)
            return
        }

        if (!isOnline()) {
            showSnackbar(getString(R.string.your_offline))
            return
        }

        val forgetPasswordHandler = object : ForgotPasswordHandler {
            override fun onSuccess() {
                jpDismiss()
                Log.e(TAG, "onSuccess : Forget Password changed successfully..")
                startActivity(LoginActivity.getIntent(mContext, userName))
            }

            override fun onFailure(exception: java.lang.Exception?) {
                jpDismiss()
                val errorMsg = exception!!.localizedMessage.toString()
                Log.e(TAG, "onFailure $errorMsg")
                when {
                    errorMsg.contains(clientIdNotFound) -> showSnackbar(clientIdNotFoundMessage)
                    errorMsg.contains(invalidUserNamePassword) -> showSnackbar(
                        invalidUserNamePasswordMessage
                    )
                    errorMsg.contains(userNotConfirmed) -> showSnackbar(userNotConfirmedMessage)
                    errorMsg.contains(invalidVerification) -> {
                        otpText.error = invalidVerificationMessage
                        showSnackbar(invalidVerificationMessage)
                    }
                    else -> showSnackbar(getString(R.string.something_wrong))
                }
            }

            override fun getResetCode(continuation: ForgotPasswordContinuation?) {
                Log.i(TAG, "getResetCode")
                jpDismiss()
                val cognitoUserDetails = continuation!!.parameters
                val email = cognitoUserDetails!!.destination
                showSnackbar("Verification code successfully sent to the $email.")
                Log.i(TAG, "Verification code successfully sent to the $email.")
                resultContinuation = continuation
            }

        }

        jpShow()
        val cognitoSettings = CognitoSettings(mContext)
        val user = cognitoSettings.userPool.getUser(userName)
        user.forgotPasswordInBackground(forgetPasswordHandler)
    }

    private fun resetPassword() {
        hideKeyboard()
        val password = etPassword.text.toString()
        val verificationCode = otpText.text.toString()

        if (!password.isValidPassword()) {
            etPassword.error = getString(R.string.invalid_password)
            return
        }

        if (verificationCode.isBlank()) {
            otpText.error = getString(R.string.write_otp)
            showSnackbar(getString(R.string.write_otp))
            return
        }

        if (password.isBlank()) {
            etPassword.error = getString(R.string.write_password)
            return
        }



        if (verificationCode.length < 6) {
            otpText.error = getString(R.string.write_otp_length)
            showSnackbar(getString(R.string.write_otp_length))
            return
        }


        jpShow()
        resultContinuation.setPassword(password)
        resultContinuation.setVerificationCode(verificationCode)
        resultContinuation.continueTask()

    }


}
