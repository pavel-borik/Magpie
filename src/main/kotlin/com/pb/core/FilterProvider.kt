package com.pb.core

import com.pb.core.data.Filter
import com.pb.core.filters.Lillie

class FilterProvider {

    fun getFilters(): List<Filter> {
        return listOf(
            Lillie()
        )
    }
}