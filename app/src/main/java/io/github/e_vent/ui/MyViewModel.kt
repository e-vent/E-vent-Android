package io.github.e_vent.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import io.github.e_vent.repo.EventPostRepo

class MyViewModel(private val repository: EventPostRepo) : ViewModel() {
    private val dummy = MutableLiveData<Void>()
    private val repoResult = map(dummy) {
        repository.posts(30)
    }
    val posts = switchMap(repoResult, { it.pagedList })!!
    val networkState = switchMap(repoResult, { it.networkState })!!
    val refreshState = switchMap(repoResult, { it.refreshState })!!

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun show() {
        dummy.value = null
    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }
}
