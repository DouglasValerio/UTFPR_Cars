package info.celsul.car.model

import java.util.Date

data class CarItem(
    val id: String,
    val name: String,
    val year: String,
    val imageUrl: String,
    val licence: String,
    val place: CarLocation?,
)

//data class CarItemValue(
//    val id: String,
//    val name: String,
//    val year: String,
//    val imageUrl: String,
//    val licence: String,
//    val place: CarLocation?,
//)

data class CarLocation(
    val latitude: Double,
    val longitude: Double
)