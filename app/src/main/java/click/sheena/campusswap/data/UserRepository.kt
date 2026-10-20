package click.sheena.campusswap.data

import click.sheena.campusswap.model.User

object UserRepository {
    private val users = SampleData.users.map { it.copy() }.toMutableList()

    fun getUsers(): List<User> = users

    fun getUserById(userId: String): User? {
        return users.find { it.id == userId }
    }
}