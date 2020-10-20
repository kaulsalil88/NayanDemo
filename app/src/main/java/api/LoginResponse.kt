package api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//{ “userid”: “:userid”, “token”: “:token” }
@Parcelize
data class LoginResponse(val userid: String, val token: String) : Parcelable {
}