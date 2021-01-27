package com.example.awscognito.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.example.awscognito.MainActivity
import com.example.awscognito.R
import com.example.awscognito.aws.CognitoSettings
import com.example.awscognito.utilities.KEY_USERNAME
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.isOnline
import com.example.jdrodi.utilities.isValidPassword
import com.example.jdrodi.utilities.showSnackbar
import com.google.gson.GsonBuilder
import com.printres.customize.mobile.cover.tshirt.print.design.aws.*
import kotlinx.android.synthetic.main.activity_login.*


// the activity initialization parameters
private val TAG = LoginActivity::class.java.simpleName

class LoginActivity : BaseActivity() {



    companion object {
        fun getIntent(mContext: Context, userName: String): Intent {
            val intent = Intent(mContext, LoginActivity::class.java)
            intent.putExtra(KEY_USERNAME, userName)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun getContext(): Activity {
        return this@LoginActivity
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

        tvForgetPassword.setOnClickListener(this)
        btnSignIn.setOnClickListener(this)
        tvSignUp.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        super.onClick(view)
        val username = etUserName.text.toString()
        when (view) {

            tvForgetPassword -> {
                startActivity(ForgetPasswordActivity.getIntent(mContext, username))
            }
            btnSignIn -> {
                loginUser()
            }
            tvSignUp -> {
                startActivity(SignupActivity.getIntent(mContext, username))
            }
        }
    }


    private fun loginUser() {
        hideKeyboard()
        val userName = etUserName.text.toString()
        val password = etPassword.text.toString()

        if (userName.isBlank()) {
            etUserName.error = getString(R.string.write_username)
            return
        }

        if (userName.length < 5) {
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


        val authenticationHandler: AuthenticationHandler = object : AuthenticationHandler {
            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                Log.i(TAG, "Login successfully..")
                jpDismiss()

                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(userSession)
                Log.i(TAG, "Login Session : \n $json")

                sp.save(KEY_USERNAME, userName)

                val intent = MainActivity.getIntent(mContext, userName)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            }

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation?, userId: String?) {
                Log.i(TAG, "Authentication require..")
                Log.i(TAG, "UserName: $userName")
                Log.i(TAG, "UserID: $userId")
                Log.i(TAG, "Password: $password")
                val authenticationDetails = AuthenticationDetails(userId, password, null)
                authenticationContinuation!!.setAuthenticationDetails(authenticationDetails)
                authenticationContinuation.continueTask()
            }

            override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                Log.i(TAG, "getMFACode")
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                Log.i(TAG, "authenticationChallenge:" + continuation!!.challengeName)
             // continuation.continueTask()
            }

            override fun onFailure(exception: Exception?) {
                jpDismiss()
                val errorMsg = exception!!.localizedMessage.toString()
                Log.e(TAG, "onFailure $errorMsg")
                when {
                    errorMsg.contains(clientIdNotFound) -> showSnackbar(clientIdNotFoundMessage)
                    errorMsg.contains(invalidUserNamePassword) -> showSnackbar(invalidUserNamePasswordMessage)
                    errorMsg.contains(userNotConfirmed) -> showSnackbar(userNotConfirmedMessage)
                    else -> showSnackbar(getString(R.string.something_wrong))
                }
            }
        }


        jpShow()
        val cognitoSettings = CognitoSettings(mContext)
        val user = cognitoSettings.userPool.getUser(userName)
        user.getSessionInBackground(authenticationHandler)

    }


}
