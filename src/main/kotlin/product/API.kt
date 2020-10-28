package product

import util.opennlp.SupportedLanguage
import java.io.InputStream

data class Category(val id: String, val name: String, val stems: Collection<String>)

interface CategoryService {
    fun xmlToCategories(xml: InputStream): Collection<Category>
    fun zipToCategories(zip: InputStream): Collection<Category>

    companion object {
        val DEFAULT_INSTANCE = GPCCategoryServiceImpl(SupportedLanguage.EN) as CategoryService

        fun instance(iso639Dash1: String): CategoryService {
            return GPCCategoryServiceImpl(iso639Dash1)
        }
    }
}