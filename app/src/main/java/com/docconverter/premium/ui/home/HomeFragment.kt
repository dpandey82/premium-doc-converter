package com.docconverter.premium.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.docconverter.premium.R
import com.docconverter.premium.databinding.FragmentHomeBinding
import com.docconverter.premium.ui.document.DocumentAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Home screen fragment showing recent documents and quick actions
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recentDocumentsAdapter: DocumentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecentDocuments()
        setupQuickActions()
        observeViewModelState()
    }

    private fun setupRecentDocuments() {
        recentDocumentsAdapter = DocumentAdapter { document ->
            // Navigate to document details when clicked
            val action = HomeFragmentDirections
                .actionHomeToDocumentDetails(documentId = document.id)
            findNavController().navigate(action)
        }

        binding.recentDocumentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentDocumentsAdapter
        }
    }

    private fun setupQuickActions() {
        // Setup convert button
        binding.btnConvert.setOnClickListener {
            findNavController().navigate(R.id.navigation_convert)
        }

        // Setup history button
        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.navigation_history)
        }
    }

    private fun observeViewModelState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentDocuments.collectLatest { documents ->
                recentDocumentsAdapter.submitList(documents)

                // Show/hide empty state
                if (documents.isEmpty()) {
                    binding.recentDocumentsRecyclerView.visibility = View.GONE
                    binding.emptyRecentDocuments.visibility = View.VISIBLE
                } else {
                    binding.recentDocumentsRecyclerView.visibility = View.VISIBLE
                    binding.emptyRecentDocuments.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.conversionStats.collectLatest { stats ->
                binding.statsConversions.text = stats.totalConversions.toString()
                binding.statsSavedSpace.text = stats.savedStorageFormatted
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
