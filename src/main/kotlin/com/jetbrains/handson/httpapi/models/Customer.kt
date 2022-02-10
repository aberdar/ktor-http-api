package com.jetbrains.handson.httpapi.models

// Вместе с интеграцией Ktor это позволит нам автоматически генерировать представление JSON, необходимое для наших ответов API
import kotlinx.serialization.Serializable

// Хранилище в памяти
val customerStorage = mutableListOf<Customer>()

@Serializable
data class Customer(val id: String, val firstName: String, val lastName: String, val email: String) {

}