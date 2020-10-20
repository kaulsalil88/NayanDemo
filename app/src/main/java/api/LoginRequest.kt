package api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//{ “email”: “:email”, “password”: “:password”}
@Parcelize
data class
LoginRequest(val email: String, val password: String) : Parcelable {

}