package api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class RepositoryDataClass(
    val id: String?,
    val name: String?,
    val full_name: String?,
    val description: String?,
    val stargazers_count: Long?
) : Parcelable {

}


