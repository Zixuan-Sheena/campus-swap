package click.sheena.campusswap.ui

import androidx.navigation.ui.AppBarConfiguration
import click.sheena.campusswap.R

object NavigationConfig {

    val appBarConfiguration = AppBarConfiguration(
        setOf(
            R.id.browseFragment,
            R.id.searchFragment,
            R.id.listItemFragment,
            R.id.chatsFragment,
            R.id.profileFragment
        )
    )
}