package click.sheena.campusswap.data

import click.sheena.campusswap.model.User

object UserSession {
    private var currentUserId: String? = null

    fun getCurrentUser(): User? {
        return currentUserId?.let { UserRepository.getUserById(it) }
    }

    fun setCurrentUser(user: User) {
        currentUserId = user.id
    }

    fun isUserLoggedIn(): Boolean {
        return currentUserId != null
    }

    fun logout() {
        currentUserId = null
    }
}