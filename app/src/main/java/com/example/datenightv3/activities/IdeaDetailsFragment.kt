package com.example.datenightv3.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.R
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.databinding.IdeaDetailsFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class IdeaDetailsFragment : Fragment() {

    private var _binding: IdeaDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: IdeaDetailsFragmentArgs by navArgs()
    private val ideasFragmentNavigationArgs: IdeasFragmentArgs by navArgs()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: AppViewModel by activityViewModels {
        AppViewModelFactory(
            (activity?.application as DatabaseApplication).categoryDatabase.categoryDao(),
            (activity?.application as DatabaseApplication).ideaDatabase.ideaDao()
        )
    }

    private lateinit var idea: Idea

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
        viewModel.getIdea(ideaId).observe(this.viewLifecycleOwner) { idea ->
            this.idea = idea
            bind(idea)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_item, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("text/plain")
                if (navigationArgs.requireLocation) shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text,
                    idea.ideaName,
                    idea.ideaLocation,
                    idea.ideaDescription)
                )
                else shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_no_location,
                    idea.ideaName,
                    idea.ideaDescription)
                )
                startActivity(Intent.createChooser(shareIntent, "Share using"))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun bind(idea: Idea) {
        if (idea.ideaLocation != null) {
            binding.ideaLocation.visibility = View.VISIBLE
            binding.ideaLocationLabel.visibility = View.VISIBLE
            binding.locateButton.visibility = View.VISIBLE
            binding.locateButton.setOnClickListener {
                val action = IdeaDetailsFragmentDirections.actionIdeaDetailsFragmentToMapFragment(
                    idea.ideaName,
                    idea.ideaLocation
                )
                findNavController().navigate(action)
            }
        }
        binding.apply {
            ideaName.text = idea.ideaName
            ideaLocation.text = idea.ideaLocation
            ideaDescription.text = idea.ideaDescription
            binding.deleteIdea.setOnClickListener { showDeleteConfirmationDialog() }
            editIdea.setOnClickListener {
                val action = IdeaDetailsFragmentDirections.actionIdeaDetailsFragmentToAddIdeaFragment(
                    titleString = "Edit " + idea.ideaName,
                    ideaId = idea.id,
                    categoryName = idea.categoryName,
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