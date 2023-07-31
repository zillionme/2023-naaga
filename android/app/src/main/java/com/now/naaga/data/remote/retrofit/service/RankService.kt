package com.now.naaga.data.remote.retrofit.service

import com.now.naaga.data.remote.dto.third.RankDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RankService {
    @GET("/rank")
    fun getAllRank(
        @Query("sort-by") sortBy: String,
        @Query("order") order: String,
    ): Call<List<RankDto>>

    @GET("/ranks/my")
    fun getMyRank(): Call<RankDto>
}
