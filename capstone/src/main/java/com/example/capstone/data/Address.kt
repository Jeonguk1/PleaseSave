package com.example.capstone.data

data class AddressResponse(
    val documents: List<Document>
)

data class Document(
    val address: Address
)

data class Address(
    val region_1depth_name: String,
    val region_2depth_name: String,
)


