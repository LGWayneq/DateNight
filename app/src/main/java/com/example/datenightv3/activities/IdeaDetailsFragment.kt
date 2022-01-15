package com.example.datenightv3.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.R
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.databinding.IdeaDetailsFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import com.example.datenightv3.viewmodel.LocationViewModel
import com.example.datenightv3.viewmodel.LocationViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class IdeaDetailsFragment : Fragment() {

    private var _binding: IdeaDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: IdeaDetailsFragmentArgs by navArgs()

    private val viewModel: AppViewModel by activityViewModels {
        AppViewModelFactory(
            (activity?.application as DatabaseApplication).categoryDatabase.categoryDao(),
            (activity?.application as DatabaseApplication).ideaDatabase.ideaDao()
        )
    }
    private val locationViewModel: LocationViewModel by activityViewModels {
        LocationViewModelFactory(
            (activity?.application as DatabaseApplication).locationDatabase.LocationDao()
        )
    }

    private lateinit var idea: Idea
    private lateinit var locationName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = IdeaDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ideaId: Int = navigationArgs.ideaId
        viewModel.getIdea(ideaId).observe(this.viewLifecycleOwner) { _idea ->
            lifecycle.coroutineScope.launch {
                idea = _idea
                bind(idea)
                locationName = locationViewModel.getLocationName(idea.locationId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_button, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("text/html")
                if (navigationArgs.requireLocation) {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.share_text,
                        idea.name,
                        locationName,
                        idea.description)
                    ))
                }
                else {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.share_text_no_location,
                        idea.name,
                        idea.description)
                    ))
                }
                startActivity(Intent.createChooser(shareIntent, "Share using"))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun bind(idea: Idea) {
        if (navigationArgs.requireLocation) {
            binding.ideaLocation.visibility = View.VISIBLE
            binding.ideaLocationLabel.visibility = View.VISIBLE
            binding.locateButton.visibility = View.VISIBLE
            binding.locateButton.setOnClickListener {
                val action = IdeaDetailsFragmentDirections.actionIdeaDetailsFragmentToMapFragment(
                    idea.name,
                    idea.locationId!!
                )
                findNavController().navigate(action)
            }
        }
        binding.apply {
            ideaName.text = idea.name
            lifecycle.coroutineScope.launch {
                ideaLocation.text = locationViewModel.getLocationName(idea.locationId)
            }
            ideaDescription.text = idea.description
            binding.deleteIdea.setOnClickListener { showDeleteConfirmationDialog() }
            editIdea.setOnClickListener {
                val action = IdeaDetailsFragmentDirections.actionIdeaDetailsFragmentToAddIdeaFragment(
                    titleString = "Edit " + idea.name,
                    ideaId = idea.id,
                    categoryId = idea.categoryId,
                    requireLocation = navigationArgs.requireLocation
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteIdea(idea)
                findNavController().navigateUp()
            }
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}