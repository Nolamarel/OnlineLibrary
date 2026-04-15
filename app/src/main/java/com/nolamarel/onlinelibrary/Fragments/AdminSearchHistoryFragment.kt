package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentAdminSearchHistoryBinding
import com.nolamarel.onlinelibrary.network.AdminSearchHistoryResponse
import kotlinx.coroutines.launch

class AdminSearchHistoryFragment : Fragment() {

    private var _binding: FragmentAdminSearchHistoryBinding? = null
    private val binding get() = _binding!!

    private val items = mutableListOf<AdminSearchHistoryResponse>()
    private lateinit var adapter: AdminSearchHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminSearchHistoryBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = AdminSearchHistoryAdapter(items)

        binding.historyRv.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRv.adapter = adapter

        loadHistory()

        return binding.root
    }

    private fun getBearerToken(): String? {
        val token = SessionManager(requireContext()).getToken()
        return token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
    }

    private fun loadHistory() {
        binding.progressBar.visibility = View.VISIBLE

        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getAdminSearchHistory(bearer)

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    items.clear()
                    items.addAll(response.body().orEmpty())
                    adapter.notifyDataSetChanged()

                    binding.emptyText.visibility =
                        if (items.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить историю поиска",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}