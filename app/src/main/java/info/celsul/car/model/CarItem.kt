package info.celsul.car.model

data class CarItem(
    val id: String,
    val name: String,
    val year: String,
    val imageUrl: String,
    val licence: String,
    val place: CarLocation?,
)

data class CarItemDetail(
    val id: String,
    val value: CarItem,
)


data class CarLocation(
    val lat: Double,
    val long: Double
)