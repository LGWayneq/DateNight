package com.example.datenightv3.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.R
import com.example.datenightv3.data.classes.Category
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.databinding.IdeaChoiceFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class IdeaChoiceFragment : Fragment() {

    private var _binding: IdeaChoiceFragmentBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: IdeaChoiceFragmentArgs by navArgs()
    private lateinit var category: Category
    private val viewModel: AppViewModel by activityViewModels {
        AppViewModelFactory(
            (activity?.application as DatabaseApplication).categoryDatabase.categoryDao(),
            (activity?.application as DatabaseApplication).ideaDatabase.ideaDao()
        )
    }
    private var requireLocation = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = IdeaChoiceFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCategory(navigationArgs.categoryId).observe(this.viewLifecycleOwner) { category ->
            this.category = category
            lifecycle.coroutineScope.launch {
                requireLocation = viewModel.requireLocation(navigationArgs.categoryId)
                bind(category)
            }
        }
    }

    private fun bind(category: Category) {
        binding.apply {
            ideaListButton.setOnClickListener {
                val action = IdeaChoiceFragmentDirections.actionIdeaChoiceFragmentToIdeasFragment(
                    navigationArgs.categoryName,
                    requireLocation
                )
                findNavController().navigate(action)
            }

            if (viewModel.doesIdeaWithCategoryExist(navigationArgs.categoryId)) {
                this.suggestionButton.setOnClickListener {
                    val action =
                        IdeaChoiceFragmentDirections.actionIdeaChoiceFragmentToSuggestionFragment(
                            navigationArgs.categoryId,
                            navigationArgs.categoryName,
                            requireLocation
                        )
                    findNavController().navigate(action)
                }
            } else {
                this.suggestionButton.isEnabled = false
                this.categoryEmptyWarning.visibility = View.VISIBLE
            }
            editCategoryButton.setOnClickListener {
                val action = IdeaChoiceFragmentDirections.actionIdeaChoiceFragmentToAddCategoryFragment(navigationArgs.categoryId)
                findNavController().navigate(action)
            }
            deleteCategoryButton.setOnClickListener { showDeleteConfirmationDialog() }
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteCategory(category)
                findNavController().navigateUp()
            }
            .show()
    }
}