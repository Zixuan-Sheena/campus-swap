package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import click.sheena.campusswap.R
import click.sheena.campusswap.data.UserRepository
import click.sheena.campusswap.data.UserSession

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.myItemsButton)
            .setOnClickListener {
                findNavController().navigate(R.id.myItemsFragment)
            }

        view.findViewById<Button>(R.id.myFavouritesButton)
            .setOnClickListener {
                findNavController().navigate(R.id.myFavouritesFragment)
            }

        view.findViewById<Button>(R.id.logoutButton)
            .setOnClickListener {
                UserSession.logout()
                refreshProfile()
            }

        view.findViewById<Button>(R.id.switchUserButton)
            .setOnClickListener {
                showUserPicker()
            }

        view.findViewById<Button>(R.id.aboutButton)
            .setOnClickListener {
                findNavController().navigate(R.id.aboutFragment)
            }

    }

    override fun onResume() {
        super.onResume()
        refreshProfile()
    }

    private fun refreshProfile() {
        val root = view ?: return
        val user = UserSession.getCurrentUser()

        root.findViewById<TextView>(R.id.userNameTextView).text =
            user?.name ?: getString(R.string.profile_not_logged_in)

        root.findViewById<TextView>(R.id.userIdTextView).text =
            if (user != null) {
                getString(R.string.profile_user_id, user.id)
            } else {
                getString(R.string.profile_select_user_hint)
            }

        val loggedIn: Boolean = user != null

        root.findViewById<Button>(R.id.myItemsButton).isEnabled = loggedIn
        root.findViewById<Button>(R.id.myFavouritesButton).isEnabled = loggedIn
        root.findViewById<Button>(R.id.logoutButton).isEnabled = loggedIn
    }

    private fun showUserPicker() {
        val users = UserRepository.getUsers()

        if (users.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_no_users_hint)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        val labels = users.map {
            "${it.name} (${it.id})"
        }.toTypedArray()

        val currentUserId = UserSession.getCurrentUser()?.id
        val selectedIndex = users.indexOfFirst { it.id == currentUserId }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.profile_select_user_hint)
            .setSingleChoiceItems(labels, selectedIndex) { dialog, index ->
                UserSession.setCurrentUser(users[index])
                refreshProfile()
                dialog.dismiss()
            }
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}