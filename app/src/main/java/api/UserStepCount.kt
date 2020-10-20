package api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//{ “email”: “:email”, “step_count”: “:step_count” }
@Parcelize
data class UserStepCount(val email: String, val step_count: Int) : Parcelable {
}