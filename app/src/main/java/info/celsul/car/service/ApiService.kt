package info.celsul.car.service

import info.celsul.car.model.CarItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("car")
    suspend fun getItems(): List<CarItem>

    @GET("car/{id}")
    suspend fun getItem(@Path("id") id: String): CarItem

    @DELETE("car/{id}")
    suspend fun deleteItem(@Path("id") id: String)

    @POST("car")
    suspend fun addItem(@Body item: CarItem): CarItem

    @PATCH("car/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: CarItem): CarItem
}