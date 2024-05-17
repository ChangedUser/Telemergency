package com.example.kotlinmessenger.icdapi

import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat.startActivityForResult

private class OnTokenAcquired: AccountManagerCallback<Bundle> {
    override fun run(result: AccountManagerFuture<Bundle>) {
        // Get the result of the operation from the AccountManagerFuture.
        val bundle: Bundle = result.getResult()

        // The token is a named value in the bundle. The name of the value
        // is stored in the constant AccountManager.KEY_AUTHTOKEN.
        val token: String? = bundle.getString(AccountManager.KEY_AUTHTOKEN)

        // val launch: Intent? = result.getResult().get(AccountManager.KEY_INTENT) as? Intent
        // if (launch != null) {
        //     startActivityForResult(launch, 0)

    }

}