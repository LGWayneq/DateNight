package com.example.datenightv3.activities

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.databinding.AddCategoryFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory

class AddCategoryFragment : Fragment() {

    private val viewModel: AppViewModel by activityViewModels {
        AppViewModelFactory(
            (activity?.application as DatabaseApplication).categoryDatabase.categoryDao(),
            (activity?.application as DatabaseApplication).ideaDatabase.ideaDao()
        )
    }
    private var _binding: AddCategoryFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = AddCategoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveAction.setOnClickListener { addNewCategory() }
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
                viewModel.addCategory(binding.categoryName.text.toString())
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