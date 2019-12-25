package viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import api.GitHubApiService
import api.RepositoryDataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

enum class GitApiStatus { LOADING, ERROR, DONE }
class MainViewModel : ViewModel() {

    private val _result = MutableLiveData<List<RepositoryDataClass>>()
    val result: LiveData<List<RepositoryDataClass>> get() = _result

    private val _status = MutableLiveData<GitApiStatus>()
    val status: LiveData<GitApiStatus> get() = _status

    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun getRepositories() {
        coroutineScope.launch {
            val deffered = GitHubApiService.retrofitService.getPopularAndroidRepoAsync()
            try {
                _status.value = GitApiStatus.LOADING
                _result.value = deffered.await()
                _status.value = GitApiStatus.DONE

            } catch (ex: Exception) {
                _status.value = GitApiStatus.ERROR
            }

        }
    }
}