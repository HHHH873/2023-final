package com.example.carrotmarket.home

data class ArticleModel(
    val sellerId: String,
    val title: String,
    val createdAt: Long,
    val price: String,
    val imageURL: String,
    val detailDescription: String,
    val onsale: Boolean = true
) {
    constructor(): this("", "", 0, "", "", "", true)
}
