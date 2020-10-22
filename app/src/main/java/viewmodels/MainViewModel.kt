package viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import api.*
import com.example.nayandemo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

enum class GitApiStatus { LOADING, ERROR, DONE }
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _result = MutableLiveData<List<RepositoryDataClass>>()
    val result: LiveData<List<RepositoryDataClass>> get() = _result

    private val _status = MutableLiveData<GitApiStatus>()
    val status: LiveData<GitApiStatus> get() = _status

    private val _loginResult = MutableLiveData<LoginResponse>()
    val loginResult: LiveData<LoginResponse> get() = _loginResult

    private val _stepCount = MutableLiveData<StepCount>()
    val stepCount: LiveData<StepCount> get() = _stepCount

    private val _userStepCount = MutableLiveData<UserStepCount>()
    val userStepCount: LiveData<UserStepCount> get() = _userStepCount

    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    lateinit var sharedPreferences: SharedPreferences

    init {
        sharedPreferences =
            getApplication<Application>().getSharedPreferences("myPref", Context.MODE_PRIVATE)
    }


    fun loginUser(email: String, password: String) {
        coroutineScope.launch {
            val deferred =
                StepCountApiService.retrofitService.loginUserAsync(LoginRequest(email, password))
            try {
                _status.value = GitApiStatus.LOADING
                _loginResult.value = deferred.await()
                _status.value = GitApiStatus.DONE

            } catch (ex: Exception) {
                onApiError(ex)
            }
        }
    }

    fun fetchStepCount() {
        val app = getApplication<Application>()
        coroutineScope.launch {
            val deferred = sharedPreferences.getString(
                app.getString(R.string.userid), ""
            )?.let {
                Log.d("MainViewMode user id ", it)
                StepCountApiService.retrofitService.getUserAsync(
                    sharedPreferences.getString(
                        app.getString(R.string.token), ""
                    )!!, it
                )
            }
            try {
                _status.value = GitApiStatus.LOADING
                _userStepCount.value = deferred?.await()
                _status.value = GitApiStatus.DONE

            } catch (ex: Exception) {
                onApiError(ex)
            }
        }
    }

    fun updateStepCount(stepCount: Int) {
        val app = getApplication<Application>()
        coroutineScope.launch {
            val deferred = sharedPreferences.getString(
                app.getString(R.string.userid), ""
            )?.let {
                Log.d("MainViewMode user id ", it)
                StepCountApiService.retrofitService.updateUserStepsAsync(
                    sharedPreferences.getString(
                        app.getString(R.string.token), ""
                    )!!, it, StepCount(stepCount.toString())
                )
            }
            try {
                _status.value = GitApiStatus.LOADING
                _stepCount.value = deferred?.await()
                _status.value = GitApiStatus.DONE

            } catch (ex: Exception) {
                onApiError(ex)
            }
        }
    }

    private fun onApiError(ex: Exception) {
        _status.value = GitApiStatus.ERROR
        Log.e("MainViewModel", "Failure: " + ex.localizedMessage)
    }

    fun saveInPref(userid: String?, token: String?): Unit {
        val app = getApplication<Application>();

        sharedPreferences.edit {
            putString(app.getString(R.string.userid), userid)
            putString(app.getString(R.string.token), token)
            apply()
        }
    }


    fun getUserId(): String? {
        val app = getApplication<Application>();
        val sharedPref = app.getSharedPreferences("myPref", Context.MODE_PRIVATE)
        return sharedPref.getString(app.getString(R.string.userid), "")
    }


}