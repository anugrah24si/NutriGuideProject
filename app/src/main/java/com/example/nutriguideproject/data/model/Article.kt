package com.example.nutriguideproject.data.model

import org.json.JSONObject

/**
 * Artikel edukasi (master) — selaras dengan tabel public.articles.
 * status: "draft" atau "published".
 */
data class Article(
    val id: String? = null,
    val title: String,
    val category: String,
    val content: String,
    val readTime: String,
    val author: String,
    val status: String,
    val createdBy: String? = null
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
            .put("title", title)
            .put("category", category)
            .put("content", content)
            .put("read_time", readTime)
            .put("author", author)
            .put("status", status)
        if (createdBy != null) o.put("created_by", createdBy)
        return o
    }

    val isPublished: Boolean get() = status == "published"

    companion object {
        fun fromJson(o: JSONObject): Article = Article(
            id = o.optString("id").ifBlank { null },
            title = o.optString("title"),
            category = o.optString("category"),
            content = o.optString("content"),
            readTime = o.optString("read_time"),
            author = o.optString("author"),
            status = o.optString("status", "draft"),
            createdBy = o.optString("created_by").ifBlank { null }
        )
    }
}
