package com.pb.messages

import com.pb.messages.data.Filter
import com.pb.messages.filters.Lillie

class FilterProvider {

    fun getFilters(): List<Filter> {
        return listOf(
            Lillie()
        )
    }
}