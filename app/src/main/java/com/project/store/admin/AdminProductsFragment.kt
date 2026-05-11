package com.project.store.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.ProductAdapter
import com.project.store.databinding.FragmentAdminProductsBinding
import com.project.store.utils.MockRepository

class AdminProductsFragment : Fragment() {

    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.adminProductsCount.text = getString(
            R.string.admin_products_count,
            MockRepository.products.size
        )
        binding.adminProductsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.adminProductsRecyclerView.adapter = ProductAdapter(MockRepository.products) { product ->
            Toast.makeText(
                requireContext(),
                getString(R.string.admin_product_selected, product.name),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
