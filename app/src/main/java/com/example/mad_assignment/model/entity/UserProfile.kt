package com.example.mad_assignment.model.entity

/* one document per account in users/{uid} */
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val role: String = Roles.STUDENT
)

object Roles {
    const val STUDENT = "student"
    const val ADMIN = "admin"
}
