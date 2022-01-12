package com.example.datenightv3.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.classes.Category
import com.example.datenightv3.databinding.AddCategoryFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import kotlinx.coroutines.launch

class AddCategoryFragment : Fragment() {

    private val viewModel: AppViewModel by activityViewModels {
        AppViewModelFactory(
            (activity?.application as DatabaseApplication).categoryDatabase.categoryDao(),
            (activity?.application as DatabaseApplication).ideaDatabase.ideaDao()
        )
    }
    private var _binding: AddCategoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: AddCategoryFragmentArgs by navArgs()
    private var requireLocation = false
    private lateinit var category: Category
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = AddCategoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.categoryId
        if (id > 0) {
            viewModel.getCategory(id).observe(this.viewLifecycleOwner) {
                category = it
                bindEditText(category)
            }
        }
        else binding.saveAction.setOnClickListener { addNewCategory() }
        binding.locationCheckboxLayout.setOnClickListener {
            requireLocation = !requireLocation
            binding.locationCheckbox.toggle()
        }
        binding.locationCheckbox.setOnClickListener { requireLocation = !requireLocation }
    }

    private fun bindEditText(category: Category) {
        binding.apply {
            categoryName.setText(category.categoryName, TextView.BufferType.SPANNABLE)
            if (category.requireLocation == 1) {
                requireLocation = !requireLocation
                locationCheckbox.toggle()
            }
            saveAction.setOnClickListener {
                lifecycle.coroutineScope.launch {updateCategory()}
            }
        }
    }

    private fun updateCategory() {
        if (isEntryValid()) {
            viewModel.getUpdatedCategory(navigationArgs.categoryId,
            binding.categoryName.text.toString(),
            requireLocation.compareTo(false))
            findNavController().navigateUp()
            findNavController().navigateUp()
        }
    }

    private fun isEntryValid(): Boolean {
        return !binding.categoryName.text.toString().isBlank()
    }

    private fun isDuplicateCategory(): Boolean {
        return viewModel.doesCategoryExist(binding.categoryName.text.toString())
    }

    private fun addNewCategory() {
        if (isEntryValid()) {
            if (isDuplicateCategory()) {
                binding.duplicateCategoryMessage.visibility = View.VISIBLE
            } else {
                viewModel.addCategory(binding.categoryName.text.toString(), requireLocation.compareTo(false))
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }
}