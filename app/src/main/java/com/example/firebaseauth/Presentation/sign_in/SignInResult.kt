package com.example.firebaseauth.Presentation.sign_in

data class SignInResult(
    val data : UserData?,
    val errorMessage : String?
)

data class UserData(
    val userId : String,
    val userName : String?,
    val profilePic : String?
)