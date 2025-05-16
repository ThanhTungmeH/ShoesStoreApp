package com.example.shoesstoreapp.controller

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CloudinaryHelp() {
    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
        "cloud_name", "your cloudname",
        "api_key", "your api key",
        "api_secret", "your api secret"
    ))

    suspend fun uploadImage(imagePath: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val result = cloudinary.uploader().upload(imagePath, ObjectUtils.emptyMap())
                result["url"] as? String
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
