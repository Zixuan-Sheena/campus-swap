package click.sheena.campusswap.data

import android.content.Context
import android.content.SharedPreferences
import click.sheena.campusswap.model.User

object UserSession {
    private const val USER_ID = "current_user_id"
    private lateinit var preferences: SharedPreferences

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }

    fun getCurrentUser(): User? =
        preferences.getString(USER_ID, null)?.let(UserRepository::getUserById)

    fun setCurrentUser(user: User) {
        preferences.edit().putString(USER_ID, user.id).apply()
    }

    fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    fun logout() {
        preferences.edit().remove(USER_ID).apply()
    }
}