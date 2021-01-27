package com.example.awscognito.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.example.awscognito.R
import com.example.awscognito.aws.ATTR_NAME
import com.example.awscognito.aws.ATTR_PREFERRED_USERNAME
import com.example.awscognito.aws.CognitoSettings
import com.example.awscognito.utilities.KEY_USERNAME
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.isOnline
import com.example.jdrodi.utilities.isValidPassword
import com.example.jdrodi.utilities.showSnackbar
import kotlinx.android.synthetic.main.activity_signup.*

// the activity initialization parameters
private val TAG = SignupActivity::class.java.simpleName

class SignupActivity : BaseActivity() {


    companion object {
        fun getIntent(mContext: Context, userName: String): Intent {
            val intent = Intent(mContext, SignupActivity::class.java)
            intent.putExtra(KEY_USERNAME, userName)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
    }

    override fun getContext(): Activity {
        return this@SignupActivity
    }

    override fun initData() {
        if (intent != null) {
            val userName: String? = intent.getStringExtra(KEY_USERNAME)
            if (userName != null && userName.isNotBlank()) {
                etUserName.setText(userName)
                etUserName.setSelection(userName.length)
            }
        }
    }

    override fun initActions() {
        ivBack.setOnClickListener(this)
        btnSignUp.setOnClickListener(this)
        tvVerifyEmail.setOnClickListener(this)
        tvSignin.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        super.onClick(view)

        val username = etUserName.text.toString()

        when (view) {
            ivBack -> {
                onBackPressed()
            }
            btnSignUp -> {
                register()
            }
            tvVerifyEmail -> {
                startActivity(ConfirmEmailActivity.getIntent(mContext, username))
            }
            tvSignin -> {
                startActivity(LoginActivity.getIntent(mContext, username))
            }
        }
    }

    private fun register() {

        hideKeyboard()

        val name = etName.text.toString()
        //  val email = etEmail.text.toString()
        val username = etUserName.text.toString()
        val password = etPassword.text.toString()

        if (name.isBlank()) {
            etName.error = getString(R.string.write_name)
            return
        }

        /* if (email.isBlank()) {
             etEmail.error = getString(R.string.write_email)
             return
         }

         if (!isValidEmail(email)) {
             etEmail.error = getString(R.string.invalid_email)
             return
         }*/

        if (username.isBlank()) {
            etUserName.error = getString(R.string.write_username)
            return
        }

        if (username.length < 5) {
            etUserName.error = getString(R.string.invalid_username)
            return
        }

        if (password.isBlank()) {
            etPassword.error = getString(R.string.write_password)
            return
        }

        if (!password.isValidPassword()) {
            etPassword.error = getString(R.string.invalid_password)
            return
        }

        if (!isOnline()) {
            showSnackbar(getString(R.string.your_offline))
            return
        }

        val userAttributes = CognitoUserAttributes()
        userAttributes.addAttribute(ATTR_NAME, name)
        userAttributes.addAttribute(ATTR_PREFERRED_USERNAME, username)

        jpShow()

        val signUpHandler: SignUpHandler = object : SignUpHandler {
            override fun onSuccess(user: CognitoUser?, signUpConfirmationState: Boolean, cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails?) {
                jpDismiss()
                Log.i(TAG, "onSuccess")
                if (!signUpConfirmationState) {
                    startActivity(ConfirmEmailActivity.getIntent(mContext, username))
                    Log.i(TAG, "onSuccess -> But not confirmed")
                } else {
                    startActivity(LoginActivity.getIntent(mContext, username))
                    Log.i(TAG, "onSuccess -> Confirmed")
                }


            }

            override fun onFailure(exception: Exception?) {
                jpDismiss()
                val errorMsg = exception!!.localizedMessage.toString()
                Log.e(TAG, "onFailure -> $errorMsg")
                if (errorMsg.contains(".")) {
                    val formattedErrorMsg: String = errorMsg.substring(0, errorMsg.indexOf(".") + 1)
                    etUserName.error = formattedErrorMsg
                } else {
                    showSnackbar(getString(R.string.something_wrong))
                }

            }
        }

        val cognitoSettings = CognitoSettings(mContext)
        cognitoSettings.userPool.signUpInBackground(username, password, userAttributes, null, signUpHandler)

    }
}
