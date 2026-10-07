/**
 * Практическая работа: аналитика покупок в магазине.
 * Коллекции (List, Set, Map), функции коллекций и generic-функция findMax.
 */

// ---------- 1. Data-классы ----------

data class User(val id: Int, val name: String, val age: Int)

data class Product(val id: Int, val name: String, val price: Int)

data class Purchase(val userId: Int, val productId: Int, val quantity: Int)

// ---------- 2. Исходные данные ----------

fun createUsers(): List<User> = listOf(
    User(1, "Анна", 25),
    User(2, "Борис", 17),
    User(3, "Клара", 34),
    User(4, "Дмитрий", 42),
    User(5, "Елена", 19),
    User(6, "Фёдор", 65)
)

fun createProducts(): List<Product> = listOf(
    Product(1, "Ноутбук", 80000),
    Product(2, "Смартфон", 50000),
    Product(3, "Наушники", 3000),
    Product(4, "Клавиатура", 4500),
    Product(5, "Мышь", 1500),
    Product(6, "Монитор", 25000),
    Product(7, "Книга", 800),
    Product(8, "Веб-камера", 3500)
)

fun createPurchases(): List<Purchase> = listOf(
    Purchase(1, 1, 1),
    Purchase(1, 3, 2),
    Purchase(1, 7, 1),
    Purchase(2, 5, 1),
    Purchase(2, 7, 3),
    Purchase(3, 2, 1),
    Purchase(3, 6, 1),
    Purchase(3, 3, 1),
    Purchase(4, 1, 1),
    Purchase(4, 4, 2),
    Purchase(5, 7, 2),
    Purchase(5, 5, 1)
)

// ---------- 3. Аналитические функции ----------

/** 3.1. Пользователи старше заданного возраста (filter). */
fun findUsersOlderThan(users: List<User>, age: Int): List<User> =
    users.filter { it.age > age }

/** 3.2. Самые дорогие товары (sortedByDescending + take). */
fun findMostExpensive(products: List<Product>, count: Int): List<Product> =
    products.sortedByDescending { it.price }.take(count)

/** 3.4. Покупки, сгруппированные по пользователю: userId -> список покупок. */
fun groupPurchasesByUser(purchases: List<Purchase>): Map<Int, List<Purchase>> =
    purchases.groupBy { it.userId }

/** 3.5. Покупки, сгруппированные по товару: productId -> список покупок. */
fun groupPurchasesByProduct(purchases: List<Purchase>): Map<Int, List<Purchase>> =
    purchases.groupBy { it.productId }

/** 3.3. Покупал ли пользователь товар (Map + any). */
fun hasUserBoughtProduct(
    purchasesByUser: Map<Int, List<Purchase>>,
    userId: Int,
    productId: Int
): Boolean = purchasesByUser[userId]?.any { it.productId == productId } ?: false

/** 3.6. Сколько потратил каждый пользователь (у кого нет покупок, будет 0). */
fun calculateSpending(
    users: List<User>,
    products: List<Product>,
    purchases: List<Purchase>
): Map<User, Int> {
    val priceById = products.associate { it.id to it.price }
    val purchasesByUser = groupPurchasesByUser(purchases)

    return users.associateWith { user ->
        purchasesByUser[user.id].orEmpty().sumOf { purchase ->
            (priceById[purchase.productId] ?: 0) * purchase.quantity
        }
    }
}

// ---------- 4. Generics (вариант А: generic-функция findMax) ----------

/** Максимум из списка сравниваемых элементов. Для пустого списка вернёт null. */
fun <T : Comparable<T>> findMax(items: List<T>): T? {
    if (items.isEmpty()) {
        return null
    }
    var best = items[0]
    for (item in items) {
        if (item > best) {
            best = item
        }
    }
    return best
}

/** Максимум по любому признаку: findMax(users) { it.age }. Для пустого списка вернёт null. */
fun <T, R : Comparable<R>> findMax(items: List<T>, selector: (T) -> R): T? {
    if (items.isEmpty()) {
        return null
    }
    var best = items[0]
    var bestValue = selector(best)
    for (item in items) {
        val value = selector(item)
        if (value > bestValue) {
            best = item
            bestValue = value
        }
    }
    return best
}

// ---------- 5. Вывод отчёта ----------

fun printSection(title: String) {
    println()
    println("=== $title ===")
}

fun main() {
    val users = createUsers()
    val products = createProducts()
    val purchases = createPurchases()

    val userById = users.associateBy { it.id }
    val productById = products.associateBy { it.id }
    val purchasesByUser = groupPurchasesByUser(purchases)
    val purchasesByProduct = groupPurchasesByProduct(purchases)
    val spending = calculateSpending(users, products, purchases)

    printSection("1. Все пользователи")
    for (user in users) {
        println("${user.id}. ${user.name}, ${user.age} лет")
    }

    val minAge = 30
    printSection("2. Пользователи старше $minAge лет")
    val olderUsers = findUsersOlderThan(users, minAge)
    if (olderUsers.isEmpty()) {
        println("Таких пользователей нет")
    } else {
        for (user in olderUsers) {
            println("${user.name}, ${user.age} лет")
        }
    }

    printSection("3. Топ-3 самых дорогих товара")
    val topProducts = findMostExpensive(products, 3)
    for ((index, product) in topProducts.withIndex()) {
        println("${index + 1}. ${product.name} — ${product.price} руб.")
    }

    printSection("4. Кто что покупал")
    val checks = listOf(1 to 1, 2 to 1, 6 to 3)   // пары (id пользователя, id товара)
    for ((userId, productId) in checks) {
        val bought = hasUserBoughtProduct(purchasesByUser, userId, productId)
        val answer = if (bought) "да" else "нет"
        println("${userById[userId]?.name} покупал(а) «${productById[productId]?.name}»? $answer")
    }

    printSection("5. Покупки по пользователям")
    for (user in users) {
        val count = purchasesByUser[user.id]?.size ?: 0
        println("${user.name}: покупок $count")
    }

    printSection("6. Покупки по товарам (продано штук)")
    for ((productId, list) in purchasesByProduct.toSortedMap()) {
        val units = list.sumOf { it.quantity }
        println("${productById[productId]?.name}: $units шт. в ${list.size} покупках")
    }

    printSection("7. Траты каждого пользователя")
    for ((user, total) in spending) {
        println("${user.name}: $total руб.")
    }

    printSection("8. Топ-покупатель")
    val topBuyer = findMax(spending.toList()) { it.second }
    if (topBuyer != null) {
        println("${topBuyer.first.name} — потратил(а) ${topBuyer.second} руб.")
    } else {
        println("Покупок нет")
    }

    printSection("9. Generic-функция findMax")
    println("Максимум из чисел 7, 42, 15: ${findMax(listOf(7, 42, 15))}")
    println("Максимум из строк (по алфавиту): ${findMax(listOf("яблоко", "арбуз", "вишня"))}")
    println("Самый старший пользователь: ${findMax(users) { it.age }?.name}")
    println("Самый дорогой товар: ${findMax(products) { it.price }?.name}")
    println("Максимум пустого списка: ${findMax(emptyList<Int>())}")

    printSection("10. Товары, которые никто не купил (Set)")
    val boughtIds: Set<Int> = purchases.map { it.productId }.toSet()
    val unsold = products.filter { it.id !in boughtIds }
    for (product in unsold) {
        println("${product.name} — ${product.price} руб.")
    }
}