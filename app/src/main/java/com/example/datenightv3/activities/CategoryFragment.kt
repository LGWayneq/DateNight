/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.datenightv3.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datenightv3.data.adapter.CategoryAdapter
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.databinding.CategoryFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CategoryFragment: Fragment() {

    private var _binding: CategoryFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

    private val viewModel: AppViewModel by activityViewModels {
        AppViewModelFactory(
            (activity?.application as DatabaseApplication).categoryDatabase.categoryDao(),
            (activity?.application as DatabaseApplication).ideaDatabase.ideaDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CategoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val categoryAdapter = CategoryAdapter {
            val action = CategoryFragmentDirections
                .actionCategoryFragmentToIdeaChoiceFragment(
                    categoryId = it.id,
                    categoryName = it.categoryName
                )
            view.findNavController().navigate(action)
        }
        recyclerView.adapter = categoryAdapter
        lifecycle.coroutineScope.launch {
            viewModel.allCategory().collect {
                categoryAdapter.submitList(it)
            }
        }
        binding.floatingActionButton.setOnClickListener {
            val action = CategoryFragmentDirections
                .actionCategoryFragmentToAddCategoryFragment()
            this.findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}