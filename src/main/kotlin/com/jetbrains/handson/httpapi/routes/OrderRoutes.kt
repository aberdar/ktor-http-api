package com.jetbrains.handson.httpapi.routes

import com.jetbrains.handson.httpapi.models.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

// для перечисления всех заказов
fun Route.listOrdersRoute() {
    get("/order") {
        if (orderStorage.isNotEmpty()) {
            call.respond(orderStorage)
        }
    }
}

// индивидуальные заказы
fun Route.getOrderRoute() {
    get("/order/{id}") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find {it.number == id} ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
        call.respond(order)
    }
}

// общая сумма заказа
fun Route.totalizeOrderRoute() {
    get("/order/{id}/total") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find {it.number == id} ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
        val total = order.contents.map {it.price * it.amount}.sum()
        call.respond(total)
    }
}

// регистрация маршрутов
fun Application.registerOrderRoutes() {
    routing {
        listOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()
    }
}
