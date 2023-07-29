package com.pb.core

import com.pb.core.data.Filter

class FilterRegistrationService {
    private val filters = mutableListOf<Filter>()

    fun getFilters(): List<Filter> = filters

    fun registerFilters(vararg filters: Filter) {
        this.filters.addAll(filters)
    }
}