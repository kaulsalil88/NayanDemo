package api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


//{ “step_count”: “:step_count” }*/
@Parcelize
data class StepCount(val step_count: String) : Parcelable {
}