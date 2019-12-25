package api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.*

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
const val BASE_URL = "https://api.github.com/"
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface GitHubApi {

    //Add the various actions here .
    @GET("repositories?q=android+language:kotlin+language:java&sort=stars&order=desc")
    fun getPopularAndroidRepoAsync():Deferred<List<RepositoryDataClass>>
}

object GitHubApiService {
    val retrofitService : GitHubApi by lazy { retrofit.create(GitHubApi::class.java) }
}