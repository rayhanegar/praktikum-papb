package com.rayhanegar.papbm2

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApiService {
    @GET("users/{username}")
    suspend fun getGithubProfile(@Path("username") username: String): GithubProfile
}