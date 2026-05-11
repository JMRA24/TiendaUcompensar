package com.project.store.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.CartAdapter
import com.project.store.databinding.FragmentCartBinding
import com.project.store.utils.CartManager
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        bindTotals()
        binding.checkoutButton.setOnClickListener {
            findNavController().navigate(R.id.nav_checkout)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(CartManager.getItems())
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = cartAdapter
    }

    private fun bindTotals() {
        binding.cartSubtotal.text = getString(
            R.string.summary_subtotal,
            currencyFormatter.format(CartManager.getSubtotal())
        )
        binding.cartShipping.text = getString(
            R.string.summary_shipping,
            currencyFormatter.format(CartManager.getShipping())
        )
        binding.cartTotal.text = getString(
            R.string.summary_total,
            currencyFormatter.format(CartManager.getTotal())
        )
    }
}
