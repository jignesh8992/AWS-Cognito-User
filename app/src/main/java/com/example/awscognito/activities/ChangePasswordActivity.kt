package com.example.awscognito.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.example.awscognito.R
import com.example.awscognito.aws.CognitoSettings
import com.example.awscognito.utilities.KEY_USERNAME
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.isOnline
import com.example.jdrodi.utilities.isValidPassword
import com.example.jdrodi.utilities.showSnackbar
import com.printres.customize.mobile.cover.tshirt.print.design.aws.*
import kotlinx.android.synthetic.main.activity_change_password.*

// the activity initialization parameters
private val TAG = ChangePasswordActivity::class.java.simpleName

class ChangePasswordActivity : BaseActivity() {


    companion object {
        fun getIntent(mContext: Context, userName: String): Intent {
            val intent = Intent(mContext, ChangePasswordActivity::class.java)
            intent.putExtra(KEY_USERNAME, userName)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
    }


    override fun getContext(): Activity {
        return this@ChangePasswordActivity
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
        btnChangePassword.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        super.onClick(view)
        when (view) {
            ivBack -> onBackPressed()
            btnChangePassword -> changePassword()
        }
    }


    private fun changePassword() {
        hideKeyboard()
        val userName = etUserName.text.toString()
        val oldPassword = etOldPassword.text.toString()
        val newPassword = etNewPassword.text.toString()

        if (userName.isBlank()) {
            etUserName.error = getString(R.string.write_username)
            return
        }

        if (userName.length < 5) {
            etUserName.error = getString(R.string.invalid_username)
            return
        }

        if (oldPassword.isBlank()) {
            etOldPassword.error = getString(R.string.write_old_password)
            return
        }

        if (!oldPassword.isValidPassword()) {
            etOldPassword.error = getString(R.string.invalid_password)
            return
        }

        if (newPassword.isBlank()) {
            etNewPassword.error = getString(R.string.write_new_password)
            return
        }

        if (!newPassword.isValidPassword()) {
            etNewPassword.error = getString(R.string.invalid_password)
            return
        }

        if (!isOnline()) {
            showSnackbar(getString(R.string.your_offline))
            return
        }


        val genericHandler = object : GenericHandler {
            override fun onSuccess() {
                jpDismiss()
                showSnackbar(getString(R.string.password_changed))
                Log.i(TAG, getString(R.string.password_changed))

            }

            override fun onFailure(exception: Exception) {
                jpDismiss()
                Log.e(TAG, exception.localizedMessage!!)
                val errorMsg = exception.localizedMessage!!.toString()
                when {
                    errorMsg.contains(clientNotAuthorized) -> etUserName.error =
                        clientIdNotFoundMessage
                    errorMsg.contains(invalidOldPassword) -> etOldPassword.error =
                        invalidOldPasswordMessage
                    else -> showSnackbar(getString(R.string.something_wrong))
                }
            }
        }


        jpShow()
        val cognitoSettings = CognitoSettings(mContext)
        val user = cognitoSettings.userPool.getUser(userName)
        user.changePasswordInBackground(oldPassword, newPassword, genericHandler)

    }


}
