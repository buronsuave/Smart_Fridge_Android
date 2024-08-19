package com.example.smartfridge.models

data class IngredientModel(
    var ingId: String? = null,
    var ingName: String? = null,
    var ingCategory: String? = null,
    var ingStock: String? = null,
    var ingImageUrl: String? = null,
    var ingDate: String? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to ingName,
            "category" to ingCategory,
            "stock" to ingStock,
            "imageUrl" to ingImageUrl,
            "date" to ingDate
        )
    }

    fun fromMap(map: Map<String, Any>, id:String){
        ingId = id
        ingName = map["name"].toString()
        ingCategory = map["category"].toString()
        ingStock = map["stock"].toString()
        ingImageUrl = map["imageUrl"].toString()
        ingDate = map["date"].toString()
    }
}