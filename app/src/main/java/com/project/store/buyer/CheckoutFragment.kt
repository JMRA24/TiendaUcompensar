package com.project.store.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.project.store.R
import com.project.store.databinding.FragmentCheckoutBinding
import com.project.store.models.UserRole
import com.project.store.utils.CartManager
import com.project.store.utils.MockRepository
import java.text.NumberFormat
import java.util.Locale

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindBuyerDefaults()
        bindSummary()
        binding.placeOrderButton.setOnClickListener {
            Toast.makeText(requireContext(), R.string.order_success_title, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindBuyerDefaults() {
        val buyer = MockRepository.getUsersByRole(UserRole.BUYER).first()
        binding.fullNameInput.setText(buyer.fullName)
    }

    private fun bindSummary() {
        val itemCount = CartManager.getItems().sumOf { it.quantity }
        binding.checkoutItems.text = getString(R.string.checkout_order_preview, itemCount)
        binding.checkoutTotal.text = getString(
            R.string.summary_total,
            currencyFormatter.format(CartManager.getTotal())
        )
    }
}
