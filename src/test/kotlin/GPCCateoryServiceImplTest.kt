import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import product.GPCCategoryServiceImpl


class GPCCategoryServiceImplTest {
    private val service = GPCCategoryServiceImpl("en")

    private val groundCoffeeStems = listOf(
        "whole", "product", "ground", "bean", "coffe", "user", "consumpt", "pure", "blend",
        "differ", "type", "prepar", "addit", "water", "milk", "varieti", "such", "arabica",
        "robusta", "various", "countri", "columbia", "ethiopia", "costa", "rica",
        "confectioneri", "instant", "substitut"
    )
    private val categoryStems = listOf("beverag", "tea") + groundCoffeeStems

    @Test(dataProvider = "nounAdjectiveData")
    fun testNounsAdjectivesWithData(text: String, expected: List<String>) {
        val actual = service.toNounsAdjectives(text).toTypedArray()
        assertEqualsNoOrder(actual, expected.toTypedArray())
    }

    @DataProvider(name = "nounAdjectiveData")
    fun nounAdjectiveData(): Iterator<Array<out Any>> {
        return listOf(
            arrayOf("This is a TEST", listOf("test")),
            arrayOf("black beans", listOf("black", "bean")),
            arrayOf("Jim Jones", listOf("jim", "jone")),
            /*
                FAIR USE: The following is extracted from GS1's GPC-EN data set, published at:
                    https://www.gs1.org/standards/gpc
                The full data set is free to download, but is only available for use by license. Such a
                small excerpt should fall under the United States fair use doctrine.
             */
            arrayOf(
                "Includes any products that can be described/observed as a whole/ground bean of" +
                        " coffee. Whole beans must be ground by the user prior to consumption and can be" +
                        " used pure or blended with different coffee bean types, whereas ground coffee" +
                        " is prepared for consumption by the addition of water and/or milk. Includes all" +
                        " varieties of coffee beans such as arabica and robusta, and those from various" +
                        " countries such as Columbia, Ethiopia and Costa Rica Excludes products such as" +
                        " Coffee Beans that are consumed as confectionery and Instant Coffee. Also" +
                        " excludes Coffee Substitutes.",
                groundCoffeeStems
            )
        ).iterator()
    }

    @Test
    fun testXmlToCategories() {
        GPCCategoryServiceImplTest::class.java.classLoader.getResourceAsStream("GPC-sample.xml"
        ).use {
            val categories = service.xmlToCategories(it)
            assertEquals(1, categories.size)

            val category = categories.first()
            assertEquals("Beverages", category.name)
            assertEquals("gpc-50200000", category.id)
            assertEqualsNoOrder(category.stems.toTypedArray(), categoryStems.toTypedArray())
        }
    }

    @Test
    fun testZipToCategories() {
        GPCCategoryServiceImplTest::class.java.classLoader.getResourceAsStream("GPC-sample.zip"
        ).use {
            val categories = service.zipToCategories(it)
            assertEquals(1, categories.size)

            val category = categories.first()
            assertEquals("Beverages", category.name)
            assertEquals("gpc-50200000", category.id)
            assertEqualsNoOrder(category.stems.toTypedArray(), categoryStems.toTypedArray())
        }
    }
}
