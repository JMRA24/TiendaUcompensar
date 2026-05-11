package com.project.store.utils

import com.project.store.models.Category
import com.project.store.models.Order
import com.project.store.models.OrderItem
import com.project.store.models.OrderStatus
import com.project.store.models.Product
import com.project.store.models.User
import com.project.store.models.UserRole

object MockRepository {

    val categories = listOf(
        Category(
            id = 1,
            name = "Tecnologia",
            description = "Dispositivos electronicos, accesorios y gadgets.",
            iconName = "ic_category_technology"
        ),
        Category(
            id = 2,
            name = "Hogar",
            description = "Articulos para cocina, decoracion y organizacion.",
            iconName = "ic_category_home"
        ),
        Category(
            id = 3,
            name = "Moda",
            description = "Ropa, calzado y accesorios de uso diario.",
            iconName = "ic_category_fashion"
        )
    )

    val users = listOf(
        User(
            id = 1,
            fullName = "Laura Martinez",
            email = "laura.admin@tienda.com",
            password = "admin123",
            role = UserRole.ADMIN,
            phone = "3001112233"
        ),
        User(
            id = 2,
            fullName = "Carlos Rojas",
            email = "carlos.admin@tienda.com",
            password = "admin123",
            role = UserRole.ADMIN,
            phone = "3001112244"
        ),
        User(
            id = 3,
            fullName = "Diana Torres",
            email = "diana.admin@tienda.com",
            password = "admin123",
            role = UserRole.ADMIN,
            phone = "3001112255"
        ),
        User(
            id = 4,
            fullName = "Andres Gomez",
            email = "andres.seller@tienda.com",
            password = "seller123",
            role = UserRole.SELLER,
            phone = "3102223344"
        ),
        User(
            id = 5,
            fullName = "Natalia Perez",
            email = "natalia.seller@tienda.com",
            password = "seller123",
            role = UserRole.SELLER,
            phone = "3102223355"
        ),
        User(
            id = 6,
            fullName = "Mateo Rodriguez",
            email = "mateo.seller@tienda.com",
            password = "seller123",
            role = UserRole.SELLER,
            phone = "3102223366"
        ),
        User(
            id = 7,
            fullName = "Valentina Castro",
            email = "valentina.buyer@tienda.com",
            password = "buyer123",
            role = UserRole.BUYER,
            phone = "3203334455"
        ),
        User(
            id = 8,
            fullName = "Sebastian Ruiz",
            email = "sebastian.buyer@tienda.com",
            password = "buyer123",
            role = UserRole.BUYER,
            phone = "3203334466"
        ),
        User(
            id = 9,
            fullName = "Camila Herrera",
            email = "camila.buyer@tienda.com",
            password = "buyer123",
            role = UserRole.BUYER,
            phone = "3203334477"
        )
    )

    val products = listOf(
        Product(
            id = 1,
            name = "Audifonos Bluetooth",
            description = "Audifonos inalambricos con cancelacion de ruido y estuche de carga.",
            price = 159900.0,
            stock = 18,
            categoryId = 1,
            sellerId = 4,
            imageName = "ic_placeholder_product"
        ),
        Product(
            id = 2,
            name = "Teclado Mecanico",
            description = "Teclado compacto con retroiluminacion y switches azules.",
            price = 239900.0,
            stock = 12,
            categoryId = 1,
            sellerId = 4,
            imageName = "ic_placeholder_product"
        ),
        Product(
            id = 3,
            name = "Smartwatch Deportivo",
            description = "Reloj inteligente con monitor cardiaco, GPS y resistencia al agua.",
            price = 329900.0,
            stock = 9,
            categoryId = 1,
            sellerId = 6,
            imageName = "ic_placeholder_product"
        ),
        Product(
            id = 4,
            name = "Licuadora 5 Velocidades",
            description = "Licuadora familiar con vaso de vidrio y cuchillas en acero inoxidable.",
            price = 189900.0,
            stock = 15,
            categoryId = 2,
            sellerId = 5,
            imageName = "ic_placeholder_product"
        ),
        Product(
            id = 5,
            name = "Set de Ollas Antiadherentes",
            description = "Juego de cinco piezas para cocina con recubrimiento antiadherente.",
            price = 279900.0,
            stock = 7,
            categoryId = 2,
            sellerId = 5,
            imageName = "ic_placeholder_product"
        ),
        Product(
            id = 6,
            name = "Lampara de Escritorio LED",
            description = "Lampara ajustable con tres tonos de luz y puerto USB.",
            price = 89900.0,
            stock = 25,
            categoryId = 2,
            sellerId = 6,
            imageName = "ic_placeholder_product"
        ),
        Product(
            id = 7,
            name = "Chaqueta Impermeable",
            description = "Chaqueta ligera con capota ajustable para clima frio o lluvioso.",
            price = 219900.0,
            stock = 11,
            categoryId = 3,
            sellerId = 5,
            imageName = "ic_placeholder_product"
        ),
        Product(
            id = 8,
            name = "Tenis Urbanos",
            description = "Calzado casual con suela antideslizante y plantilla acolchada.",
            price = 199900.0,
            stock = 20,
            categoryId = 3,
            sellerId = 4,
            imageName = "ic_placeholder_product"
        )
    )

    val orders = listOf(
        Order(
            id = 1,
            buyerId = 7,
            products = listOf(
                OrderItem(productId = 1, quantity = 1, unitPrice = 159900.0),
                OrderItem(productId = 6, quantity = 2, unitPrice = 89900.0)
            ),
            status = OrderStatus.DELIVERED,
            orderDate = "2026-04-20",
            shippingAddress = "Calle 45 #12-30, Bogota"
        ),
        Order(
            id = 2,
            buyerId = 8,
            products = listOf(
                OrderItem(productId = 2, quantity = 1, unitPrice = 239900.0)
            ),
            status = OrderStatus.SHIPPED,
            orderDate = "2026-04-28",
            shippingAddress = "Carrera 10 #80-15, Bogota"
        ),
        Order(
            id = 3,
            buyerId = 9,
            products = listOf(
                OrderItem(productId = 4, quantity = 1, unitPrice = 189900.0),
                OrderItem(productId = 5, quantity = 1, unitPrice = 279900.0)
            ),
            status = OrderStatus.PROCESSING,
            orderDate = "2026-05-02",
            shippingAddress = "Avenida Siempre Viva #742, Medellin"
        ),
        Order(
            id = 4,
            buyerId = 7,
            products = listOf(
                OrderItem(productId = 7, quantity = 1, unitPrice = 219900.0),
                OrderItem(productId = 8, quantity = 1, unitPrice = 199900.0)
            ),
            status = OrderStatus.PENDING,
            orderDate = "2026-05-06",
            shippingAddress = "Transversal 18 #55-40, Cali"
        ),
        Order(
            id = 5,
            buyerId = 8,
            products = listOf(
                OrderItem(productId = 3, quantity = 1, unitPrice = 329900.0)
            ),
            status = OrderStatus.CANCELLED,
            orderDate = "2026-05-08",
            shippingAddress = "Calle 70 #23-18, Barranquilla"
        )
    )

    fun getUsersByRole(role: UserRole): List<User> = users.filter { it.role == role }

    fun getProductsByCategory(categoryId: Int): List<Product> =
        products.filter { it.categoryId == categoryId }

    fun getProductsBySeller(sellerId: Int): List<Product> =
        products.filter { it.sellerId == sellerId }

    fun getOrdersByBuyer(buyerId: Int): List<Order> =
        orders.filter { it.buyerId == buyerId }

    fun findUserByEmail(email: String): User? =
        users.firstOrNull { it.email.equals(email, ignoreCase = true) }

    fun findProductById(productId: Int): Product? =
        products.firstOrNull { it.id == productId }
}
