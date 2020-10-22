package api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */

//http://10.02.2:3000 -> use this when testing on emulator
//In case testing on actual device add the ip of local machine
const val BASE_URL = "http://192.168.0.103:3000"
private val httpLoggingInterceptor = HttpLoggingInterceptor()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .client(
        OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    )
    .build()


/*POST /sessions/new { “email”: “:email”, “password”: “:password”} -> { “userid”: “:userid”, “token”: “:token” }
• GET /users/:userid -> { “email”: “:email”, “step_count”: “:step_count” }
• POST /users/:userid/steps { “step_count”: “:step_count” } ->  { “step_count”: “:step_count” }*/

interface GitHubApi {

    //Add the various actions here .
    @GET("repositories?q=android+language:kotlin+language:java&sort=stars&order=desc")
    fun getPopularAndroidRepoAsync(): Deferred<List<RepositoryDataClass>>

    @POST("/session/new")
    fun loginUserAsync(@Body loginRequest: LoginRequest): Deferred<LoginResponse>

    @GET("/users/{userid}")
    fun getUserAsync(
        @Header("Bearer") token: String,
        @Path("userid") userId: String
    ): Deferred<UserStepCount>

    @POST("/users/{userid}/steps")
    fun updateUserStepsAsync(
        @Header("Bearer") token: String,
        @Path("userid") userId: String,
        @Body stepCount: StepCount
    ): Deferred<StepCount>
}

object StepCountApiService {
    val retrofitService: GitHubApi by lazy { retrofit.create(GitHubApi::class.java) }
}