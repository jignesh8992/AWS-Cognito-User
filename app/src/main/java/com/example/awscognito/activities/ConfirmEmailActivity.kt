package com.example.awscognito.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler
import com.example.awscognito.R
import com.example.awscognito.aws.CognitoSettings
import com.example.awscognito.utilities.KEY_USERNAME
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.isOnline
import com.example.jdrodi.utilities.showSnackbar
import com.printres.customize.mobile.cover.tshirt.print.design.aws.*
import kotlinx.android.synthetic.main.activity_confirm_email.*

// the activity initialization parameters
private val TAG = ConfirmEmailActivity::class.java.simpleName

class ConfirmEmailActivity : BaseActivity() {


    companion object {
        fun getIntent(mContext: Context, userName: String): Intent {
            val intent = Intent(mContext, ConfirmEmailActivity::class.java)
            intent.putExtra(KEY_USERNAME, userName)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_email)
    }

    override fun getContext(): Activity {
        return this@ConfirmEmailActivity
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
        btnVerify.setOnClickListener(this)
        btnRequestNew.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        super.onClick(view)

        when (view) {
            ivBack -> {
                onBackPressed()
            }
            btnVerify -> {
                verifyUser()
            }
            btnRequestNew -> {
                reqNewCode()
            }

        }
    }

    private fun reqNewCode() {
        hideKeyboard()
        val userName = etUserName.text.toString()
        if (userName.isBlank()) {
            etUserName.error = getString(R.string.write_username)
            return
        }


        val verificationHandler = object : VerificationHandler {
            override fun onSuccess(verificationCodeDeliveryMedium: CognitoUserCodeDeliveryDetails?) {
                jpDismiss()
                Log.e(TAG, "onSuccess")
                val email = verificationCodeDeliveryMedium!!.destination
                showSnackbar("Verification code successfully sent to the $email.")

            }

            override fun onFailure(exception: java.lang.Exception?) {
                jpDismiss()
                Log.e(TAG, "onFailure -> ${exception!!.localizedMessage}")
                val errorMsg = exception.localizedMessage.toString()
                when {
                    errorMsg.contains(clientIdNotFound) -> etUserName.error = clientIdNotFoundMessage
                    else -> showSnackbar(getString(R.string.something_wrong))
                }
            }

        }

        jpShow()
        val cognitoSettings = CognitoSettings(mContext)
        val user = cognitoSettings.userPool.getUser(userName)
        user.resendConfirmationCodeInBackground(verificationHandler)

    }

    private fun verifyUser() {
        hideKeyboard()
        val userName = etUserName.text.toString()
        val verificationCode = otpText.text.toString()

        if (userName.isBlank()) {
            etUserName.error = getString(R.string.write_username)
            return
        }

        if (verificationCode.isBlank()) {
            otpText.error = getString(R.string.write_otp)
            showSnackbar(getString(R.string.write_otp))
            return
        }

        if (verificationCode.length < 6) {
            otpText.error = getString(R.string.write_otp_length)
            showSnackbar(getString(R.string.write_otp_length))
            return
        }

        if (!isOnline()) {
            showSnackbar(getString(R.string.your_offline))
            return
        }


        val genericHandler = object : GenericHandler {
            override fun onSuccess() {
                jpDismiss()
                startActivity(LoginActivity.getIntent(mContext, userName))
                Log.i(TAG, "onSuccess -> Confirmed")
            }

            override fun onFailure(exception: Exception?) {
                jpDismiss()
                Log.e(TAG, "onFailure -> ${exception!!.localizedMessage}")
                val errorMsg = exception.localizedMessage.toString()
                when {
                    errorMsg.contains(clientIdNotFound) -> etUserName.error = clientIdNotFoundMessage
                    errorMsg.contains(invalidVerification) -> {
                        otpText.error = invalidVerificationMessage
                        showSnackbar(invalidVerificationMessage)
                    }
                    else -> showSnackbar(exception.localizedMessage)
                }
            }
        }
        jpShow()
        val cognitoSettings = CognitoSettings(mContext)
        val user = cognitoSettings.userPool.getUser(userName)
        user.confirmSignUpInBackground(verificationCode, false, genericHandler)
    }


}
