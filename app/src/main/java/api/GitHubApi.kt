package api

import retrofit2.http.GET

interface GitHubApi {

    @GET("repositories?q=android+language:kotlin+language:java&sort=stars&order=desc")
    fun getPopularAndroidRepo()
}