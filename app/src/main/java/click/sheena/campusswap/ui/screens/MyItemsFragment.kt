package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import click.sheena.campusswap.R
import click.sheena.campusswap.ui.NavigationConfig

class MyItemsFragment : Fragment(R.layout.fragment_my_items) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.myItemsToolbar).setupWithNavController(
            findNavController(),
            NavigationConfig.appBarConfiguration
        )
    }
}