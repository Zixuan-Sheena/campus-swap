package click.sheena.campusswap.ui.screens

import java.io.File
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ItemRepository
import click.sheena.campusswap.data.UserSession
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.ItemCategory
import click.sheena.campusswap.ui.NavigationConfig
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import click.sheena.campusswap.data.ItemImageStorage
import click.sheena.campusswap.ui.loadItemImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListItemFragment : Fragment(R.layout.fragment_list_item) {

    companion object {
        const val ARG_ITEM_ID = "itemId"

    }

    private val categories = ItemCategory.values().toList()

    private var selectedImagePath = ""
    private var isCopyingImage = false

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val appContext = requireContext().applicationContext

            isCopyingImage = true
            updateImageButtons()

            lifecycleScope.launch {
                try {
                    val copiedPath = withContext(Dispatchers.IO) {
                        ItemImageStorage.copyImage(appContext, uri)
                    }

                    selectedImagePath = copiedPath

                    view?.findViewById<ImageView>(
                        R.id.itemImagePreview
                    )?.apply {
                        isVisible = true
                        loadItemImage(selectedImagePath, fallbackRes = 0)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    if (isAdded) {
                        showMessage(R.string.item_form_image_failed)
                    }
                } finally {
                    isCopyingImage = false
                    updateImageButtons()
                }
            }
        }
    }

    private fun updateImageButtons() {
        val root = view ?: return

        root.findViewById<Button>(
            R.id.selectItemImageButton
        ).apply {
            isEnabled = !isCopyingImage

            setText(
                if (isCopyingImage) {
                    R.string.item_form_image_copying
                } else {
                    R.string.item_form_select_image
                }
            )
        }

        root.findViewById<Button>(
            R.id.saveItemButton
        ).isEnabled = !isCopyingImage
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // This form hides the app's bottom navigation, so it handles the
        // system navigation bar and keyboard space itself.
        val initialBottomPadding = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { form, insets ->
            val bottomInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.ime()
            )
            form.updatePadding(
                bottom = initialBottomPadding + bottomInsets.bottom
            )
            insets
        }
        view.doOnAttach { ViewCompat.requestApplyInsets(it) }

        val toolbar = view.findViewById<Toolbar>(
            R.id.listItemToolbar
        )
        val titleInput = view.findViewById<EditText>(
            R.id.itemTitleInput
        )
        val descriptionInput = view.findViewById<EditText>(
            R.id.itemDescriptionInput
        )
        val categorySpinner = view.findViewById<Spinner>(
            R.id.itemCategorySpinner
        )
        val wantedInput = view.findViewById<EditText>(
            R.id.itemWantedInput
        )
        val imagePreview = view.findViewById<ImageView>(
            R.id.itemImagePreview
        )
        val selectImageButton = view.findViewById<Button>(
            R.id.selectItemImageButton
        )
        val saveButton = view.findViewById<Button>(
            R.id.saveItemButton
        )

        toolbar.setupWithNavController(
            findNavController(),
            NavigationConfig.appBarConfiguration
        )

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories.map { it.name }
        )

        categoryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        categorySpinner.adapter = categoryAdapter

        val itemId = arguments?.getString(ARG_ITEM_ID)
        val isEditing = itemId != null

        toolbar.setTitle(
            if (isEditing) {
                R.string.item_form_edit
            } else {
                R.string.nav_list_item
            }
        )

        saveButton.setText(
            if (isEditing) {
                R.string.item_form_save_changes
            } else {
                R.string.item_form_publish
            }
        )

        val currentUser = UserSession.getCurrentUser()

        if (currentUser == null) {
            showMessage(R.string.item_form_login_required)
            findNavController().popBackStack()
            return
        }

        val formUserId = currentUser.id

        val existingItem = if (itemId != null) {
            ItemRepository.getItemById(itemId)
        } else {
            null
        }

        if (
            isEditing &&
            (existingItem == null ||
                    !existingItem.canBeEditedBy(formUserId))
        ) {
            showMessage(R.string.item_form_unavailable)
            findNavController().popBackStack()
            return
        }

        selectedImagePath = existingItem?.imagePath.orEmpty()

        if (existingItem != null) {
            titleInput.setText(existingItem.title)
            descriptionInput.setText(existingItem.description)
            wantedInput.setText(existingItem.wantedDescription)

            categorySpinner.setSelection(
                categories.indexOf(existingItem.category)
                    .coerceAtLeast(0)
            )
        }

        imagePreview.isVisible = selectedImagePath.isNotBlank() &&
            File(selectedImagePath).isFile
        if (imagePreview.isVisible) {
            imagePreview.loadItemImage(selectedImagePath, fallbackRes = 0)
        }

        selectImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        updateImageButtons()

        saveButton.setOnClickListener {
            if (isCopyingImage) {
                return@setOnClickListener
            }

            val user = UserSession.getCurrentUser()

            if (user == null || user.id != formUserId) {
                showMessage(R.string.item_form_login_required)
                return@setOnClickListener
            }

            if (!validateRequired(titleInput)) {
                return@setOnClickListener
            }

            if (!validateRequired(descriptionInput)) {
                return@setOnClickListener
            }

            if (!validateRequired(wantedInput)) {
                return@setOnClickListener
            }

            if (
                selectedImagePath.isBlank() ||
                !File(selectedImagePath).isFile
            ) {
                showMessage(R.string.item_form_image_required)
                selectImageButton.requestFocus()
                return@setOnClickListener
            }

            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text
                .toString()
                .trim()
            val wantedDescription = wantedInput.text
                .toString()
                .trim()
            val imagePath = selectedImagePath
            val category = categories[
                categorySpinner.selectedItemPosition
            ]

            val savedItem = if (itemId == null) {
                ItemRepository.addItem(
                    Item(
                        ownerId = user.id,
                        title = title,
                        description = description,
                        category = category,
                        wantedDescription = wantedDescription,
                        imagePath = imagePath
                    )
                )
            } else {
                ItemRepository.updateItem(
                    itemId = itemId,
                    userId = user.id,
                    title = title,
                    description = description,
                    category = category,
                    wantedDescription = wantedDescription,
                    imagePath = imagePath
                )
            }

            if (savedItem == null) {
                showMessage(R.string.item_form_save_failed)
                return@setOnClickListener
            }

            showMessage(
                if (isEditing) {
                    R.string.item_form_updated
                } else {
                    R.string.item_form_published
                }
            )

            findNavController().popBackStack()
        }
    }

    private fun validateRequired(input: EditText): Boolean {
        if (input.text.toString().isBlank()) {
            input.error = getString(R.string.item_form_required)
            input.requestFocus()
            return false
        }

        input.error = null
        return true
    }

    private fun showMessage(messageRes: Int) {
        Toast.makeText(
            requireContext(),
            messageRes,
            Toast.LENGTH_SHORT
        ).show()
    }

}
