package io.codingarts.jumiotestapp

import retrofit2.Call
import retrofit2.http.POST

interface RestApiTestInterface {
    @POST("https://dev.maslife.com/apiv2/idVerificationTokenTest")
    fun jumioTest() : Call<IdVerificationTokenResponse>
}