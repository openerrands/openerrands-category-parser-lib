import product.CategoryService
import java.io.FileInputStream

fun main(args: Array<String>) {
    val iso639Dash1 = args[0]
    val filename = args[1]
    val service = CategoryService.instance(iso639Dash1)

    val categories = when {
        filename.endsWith(".zip", true) -> {
            FileInputStream(filename).use {
                service.zipToCategories(it)
            }
        }
        filename.endsWith(".xml", true) -> {
            FileInputStream(filename).use {
                service.xmlToCategories(it)
            }
        }
        else -> {
            throw IllegalArgumentException("Unsupported file extension for $filename")
        }
    }
    println(categories)
}
