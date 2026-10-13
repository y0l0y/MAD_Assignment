package com.example.mad_assignment.model.other

import android.content.Context
import com.example.mad_assignment.model.entity.LocalBookEntity
import com.example.mad_assignment.model.entity.LocalCategoryEntity
import org.json.JSONObject

/* reads the book list that ships inside the app: assets/catalog_seed.json */
class CatalogSeed(private val context: Context) {
    data class Data(
        val version: Long,
        val categories: List<LocalCategoryEntity>,
        val books: List<LocalBookEntity>
    )

    /* null = file missing or unreadable */
    fun read(): Data? = try {
        val text = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
        parse(JSONObject(text))
    } catch (_: Exception) {
        null
    }

    /* rows that break the app rules are skipped, so a messy file cannot break the app */
    private fun parse(root: JSONObject): Data {
        val categories = LinkedHashMap<String, LocalCategoryEntity>()
        val categoryArray = root.optJSONArray("categories")
        if (categoryArray != null) {
            for (i in 0 until categoryArray.length()) {
                val name = categoryArray.optString(i).trim()
                if (BookRules.validateCategoryName(name) != null) continue
                val id = categoryId(name)
                if (!categories.containsKey(id)) categories[id] = LocalCategoryEntity(id, name)
            }
        }
        val books = LinkedHashMap<String, LocalBookEntity>()
        val bookArray = root.optJSONArray("books")
        if (bookArray != null) {
            for (i in 0 until bookArray.length()) {
                val item = bookArray.optJSONObject(i) ?: continue
                val title = item.optString("title").trim()
                val author = item.optString("author").trim()
                val isbn = BookRules.normaliseIsbn(item.optString("isbn"))
                if (BookRules.validateTitle(title) != null) continue
                if (BookRules.validateAuthor(author) != null) continue
                if (BookRules.validateIsbn(isbn) != null) continue
                val id = "book-$isbn"
                if (books.containsKey(id)) continue
                val category = categoryId(item.optString("category").trim())
                books[id] = LocalBookEntity(
                    id = id,
                    title = title,
                    author = author,
                    isbn = isbn,
                    categoryId = if (categories.containsKey(category)) category else null,
                    coverBase64 = null,
                    coverUrl = "https://covers.openlibrary.org/b/isbn/$isbn-M.jpg"
                )
            }
        }
        return Data(root.optLong("version", 1L), categories.values.toList(), books.values.toList())
    }

    /* same name always gives the same id, so every phone and firestore agree */
    private fun categoryId(name: String): String =
        "cat-" + name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private companion object {
        const val FILE_NAME = "catalog_seed.json"
    }
}
