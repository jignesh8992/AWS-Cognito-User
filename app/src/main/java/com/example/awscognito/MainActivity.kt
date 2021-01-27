package com.example.awscognito

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.awscognito.utilities.KEY_USERNAME
import com.example.jdrodi.BaseActivity

class MainActivity : BaseActivity() {


    companion object {
        fun getIntent(mContext: Context, userName: String): Intent {
            val intent = Intent(mContext, MainActivity::class.java)
            intent.putExtra(KEY_USERNAME, userName)
            return intent
        }
    }

    override fun getContext(): Activity {
        return this@MainActivity
    }

    override fun initActions() {

    }

    override fun initData() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}