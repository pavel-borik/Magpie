package com.pb.http.data

sealed class ApiOperationResult<out T> {
    data class Success<T>(val value: T) : ApiOperationResult<T>()
    object NotFound : ApiOperationResult<Nothing>()
    object Error : ApiOperationResult<Nothing>()
}