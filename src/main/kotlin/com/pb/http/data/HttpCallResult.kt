package com.pb.http.data

sealed class HttpCallResult<out T> {
    data class Success<T>(val value: T) : HttpCallResult<T>()
    object NotFound : HttpCallResult<Nothing>()
    object Error : HttpCallResult<Nothing>()
}