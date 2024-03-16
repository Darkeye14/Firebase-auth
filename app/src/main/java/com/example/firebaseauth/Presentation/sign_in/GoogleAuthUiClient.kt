package com.example.firebaseauth.Presentation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.firebaseauth.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.concurrent.CancellationException


class GoogleAuthUiClient(
    private val context : Context,
    private val oneTapClient:SignInClient
) {
    private val auth = com.google.firebase.ktx.Firebase.auth
    suspend fun signIn() : IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignRequest()
            ).await()
        }catch (e : Exception){
            e.printStackTrace()
            if (e is CancellationException){
                throw e
            }
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent : Intent): SignInResult{
        val credentials = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdTokens = credentials.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdTokens,null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            return SignInResult(
                data = user?.run {
                                UserData(
                                    userId = uid,
                                    userName = displayName,
                                    profilePic = photoUrl?.toString()

                                )
                },
                errorMessage = null
            )
        }catch (e: Exception){
            e.printStackTrace()
            if (e is CancellationException){
                throw e
            }
            SignInResult(null,e.message)
        }
    }

    suspend fun signOut(){
        try {
            oneTapClient.signOut()
        }catch (e:Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    fun getSignedUser() : UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            userName = displayName,
            profilePic = photoUrl?.toString()

        )
    }

    private fun buildSignRequest() : BeginSignInRequest{
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString( R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }


}