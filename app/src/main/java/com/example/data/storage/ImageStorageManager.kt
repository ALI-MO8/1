package com.example.data.storage

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageManager {

    private const val IMAGES_DIR = "app_images"
    private const val PRODUCTS_SUBDIR = "products"
    private const val COLORS_SUBDIR = "colors"

    fun getProductsDir(context: Context): File {
        val dir = File(File(context.filesDir, IMAGES_DIR), PRODUCTS_SUBDIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getColorsDir(context: Context): File {
        val dir = File(File(context.filesDir, IMAGES_DIR), COLORS_SUBDIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getAllImagesDir(context: Context): File {
        val dir = File(context.filesDir, IMAGES_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * حفظ صورة الموديل محلياً في الذاكرة الدائمة للتطبيق
     */
    fun saveProductImage(context: Context, sourceUri: Uri): String? {
        return copyUriToFile(context, sourceUri, getProductsDir(context))
    }

    /**
     * حفظ صورة اللون محلياً في الذاكرة الدائمة للتطبيق
     */
    fun saveColorImage(context: Context, sourceUri: Uri): String? {
        return copyUriToFile(context, sourceUri, getColorsDir(context))
    }

    private fun copyUriToFile(context: Context, sourceUri: Uri, targetDir: File): String? {
        return try {
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val targetFile = File(targetDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(targetFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * التحقق من وجود الصورة وحجمها
     */
    fun getLocalFileFromUri(uriString: String?): File? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val path = Uri.parse(uriString).path ?: return null
            val file = File(path)
            if (file.exists()) file else null
        } catch (e: Exception) {
            null
        }
    }

    fun countStoredImages(context: Context): Int {
        val pCount = getProductsDir(context).listFiles()?.size ?: 0
        val cCount = getColorsDir(context).listFiles()?.size ?: 0
        return pCount + cCount
    }
}
