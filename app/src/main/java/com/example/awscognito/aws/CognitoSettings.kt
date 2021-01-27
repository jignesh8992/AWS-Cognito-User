package com.example.awscognito.aws

import android.content.Context

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.regions.Regions


const val ATTR_NAME = "given_name"
const val ATTR_EMAIL = "email"
const val ATTR_PREFERRED_USERNAME = "preferred_username"


class CognitoSettings(private val mContext: Context) {

    private val userPoolId = "ap-south-1_eQiUjvUFJ"
    private val clientId = "67qbg0k5vjq5she8hbnbuv75ra"
    private val clientSecretId = "7bmjuvo0ovqdtbnbqvb3ltl6m91bmlb1emd9jj6blhuprep0rjq"


    private val cognitoRegion = Regions.AP_SOUTH_1

    val userPool: CognitoUserPool
        get() = CognitoUserPool(mContext, userPoolId, clientId, clientSecretId, cognitoRegion)


}
