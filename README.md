[![official JetBrains project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

# Creating HTTP API with Ktor
## Содержание

  * [Зависимости](#зависимости)
  * [Конфигурации: application.conf и logback.xml](#конфигурации-applicationconf-и-logbackxml)
  * [Точка входа](#точка-входа)
  * [Маршруты клиентов](#маршруты-клиентов)
  * [Заказ маршрутов](#заказ-маршрутов)
  * [Тестирование](#тестирование)

____
## Зависимости

Gradle:
```kotlin
dependencies {
    implementation "io.ktor:ktor-server-core:$ktor_version"
    implementation "io.ktor:ktor-server-netty:$ktor_version"
    implementation "ch.qos.logback:logback-classic:$logback_version"
    implementation "io.ktor:ktor-serialization:$ktor_version"

    testImplementation "io.ktor:ktor-server-test-host:$ktor_version"
    testImplementation "org.jetbrains.kotlin:kotlin-test"
}
```
* ```ktor-server-core``` добавляет основные компоненты Ktor в наш проект.
* ```ktor-server-netty``` добавляет движок Netty в наш проект, позволяя нам использовать функциональность сервера без необходимости полагаться на внешний контейнер приложения.
* ```logback-classic``` предоставляет реализацию [SLF4J](https://www.slf4j.org/), позволяющую нам видеть красиво отформатированные журналы в нашей консоли.
* ```ktor-serialization``` обеспечивает удобный механизм преобразования объектов Kotlin в сериализованную форму, такую как JSON, и наоборот.
* ```tor-server-test-host``` позволяет нам тестировать части нашего приложения Ktor без необходимости использовать весь стек HTTP в процессе.

## Конфигурации: application.conf и logback.xml
Репозиторий также включает в себя базовый ```application.conf``` в формате HOCON, расположенный в resourcesпапке. Ktor использует этот файл для определения порта, на котором он должен работать, а также определяет точку входа нашего приложения.

Также в ту же папку включен ```logback.xml``` файл, который устанавливает базовую структуру ведения журнала для нашего сервера. 

## Точка входа
```kotlin
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

}
```
Точка входа в наше приложение важна, потому что мы устанавливаем плагины Ktor и определяем маршрутизацию для нашего API

File: [Application.kt](https://github.com/aberdar/ktor-http-api/blob/main/src/main/kotlin/com/jetbrains/handson/httpapi/Application.kt)

## Маршруты клиентов
### Модель и хранение клиентов

Модель клиентов:
```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class Customer(val id: String, val firstName: String, val lastName: String, val email: String)
```
Хранение клиентов:
```kotlin
val customerStorage = mutableListOf<Customer>()
```
File: [Customer.kt](https://github.com/aberdar/ktor-http-api/blob/main/src/main/kotlin/com/jetbrains/handson/httpapi/models/Customer.kt)

### Определение маршрута для клиентов
```kotlin
import io.ktor.routing.*

fun Route.customerRouting() {
    route("/customer") {
        get {

        }
        get("{id}") {

        }
        post {

        }
        delete("{id}") {

        }
    }
}
```
File: [CustomerRoutes.kt](https://github.com/aberdar/ktor-http-api/blob/main/src/main/kotlin/com/jetbrains/handson/httpapi/routes/CustomerRoutes.kt)

### Регистрация маршрутов
```kotlin
fun Application.registerCustomerRoutes() {
    routing {
        customerRouting()
    }
}
```

## Заказ маршрутов

### Определение модели

Классы данных:
```kotlin
@Serializable
data class Order(val number: String, val contents: List<OrderItem>)

@Serializable
data class OrderItem(val item: String, val amount: Int, val price: Double)
```
Образцы заказов:
```kotlin
val orderStorage = listOf(Order(
    "2020-04-06-01", listOf(
        OrderItem("Ham Sandwich", 2, 5.50),
        OrderItem("Water", 1, 1.50),
        OrderItem("Beer", 3, 2.30),
        OrderItem("Cheesecake", 1, 3.75)
    )),
    Order("2020-04-03-01", listOf(
        OrderItem("Cheeseburger", 1, 8.50),
        OrderItem("Water", 2, 1.50),
        OrderItem("Coke", 2, 1.76),
        OrderItem("Ice Cream", 1, 2.35)
    ))
)
```
File: [Order.kt](https://github.com/aberdar/ktor-http-api/blob/main/src/main/kotlin/com/jetbrains/handson/httpapi/models/Order.kt)

### Определение маршрутов заказов

Перечисление всех и индивидуальных заказов:
```kotlin
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.listOrdersRoute() {
    get("/order") {
        if (orderStorage.isNotEmpty()) {
            call.respond(orderStorage)
        }
    }
}
```

```kotlin
fun Route.getOrderRoute() {
    get("/order/{id}") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        call.respond(order)
    }
}
```

Подведение итогов по заказу:
```kotlin
fun Route.totalizeOrderRoute() {
    get("/order/{id}/total") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        val total = order.contents.map { it.price * it.amount }.sum()
        call.respond(total)
    }
}
```

Регистрация маршрутов:
```kotlin
fun Application.registerOrderRoutes() {
    routing {
        listOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()
    }
}
```

File: [OrderRoutes.kt](https://github.com/aberdar/ktor-http-api/blob/main/src/main/kotlin/com/jetbrains/handson/httpapi/routes/OrderRoutes.kt)

## Тестирование

### Ручное тестирование
```http
POST http://127.0.0.1:8080/customer
Content-Type: application/json

{
  "id": "100",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@company.com"
}


###
POST http://127.0.0.1:8080/customer
Content-Type: application/json

{
  "id": "200",
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@company.com"
}

###
POST http://127.0.0.1:8080/customer
Content-Type: application/json

{
  "id": "300",
  "firstName": "Mary",
  "lastName": "Smith",
  "email": "mary.smith@company.com"
}


###
GET http://127.0.0.1:8080/customer
Accept: application/json

###
GET http://127.0.0.1:8080/customer/200

###
GET http://127.0.0.1:8080/customer/500

###
DELETE http://127.0.0.1:8080/customer/100

###
DELETE http://127.0.0.1:8080/customer/500
```
File: [CustomerTest.http](https://github.com/aberdar/ktor-http-api/blob/main/src/test/CustomerTest.http) and [OrderTest.http](https://github.com/aberdar/ktor-http-api/blob/main/src/test/OrderTest.http)

### Автоматизированное тестирование
```kotlin
import com.jetbrains.handson.httpapi.module
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals

class OrderRouteTests {
    @Test
    fun testGetOrder() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order/2020-04-06-01").apply {
                assertEquals(
                    """{"number":"2020-04-06-01","contents":[{"item":"Ham Sandwich","amount":2,"price":5.5},{"item":"Water","amount":1,"price":1.5},{"item":"Beer","amount":3,"price":2.3},{"item":"Cheesecake","amount":1,"price":3.75}]}""",
                    response.content
                )
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
```
File: [OrderRouteTests.kt](https://github.com/aberdar/ktor-http-api/blob/main/src/test/kotlin/OrderRouteTests.kt)
____

This repository is the code corresponding to the hands-on lab [Creating HTTP APIs](https://ktor.io/docs/creating-http-apis.html). 
