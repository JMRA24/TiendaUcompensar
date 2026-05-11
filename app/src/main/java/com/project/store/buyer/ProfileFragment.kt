package com.project.store.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.adapters.OrderHistoryAdapter
import com.project.store.databinding.FragmentProfileBinding
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository
import com.project.store.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindProfile()
        binding.logoutButton.setOnClickListener {
            SessionManager.logout(requireContext())
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindProfile() {
        val buyer = MockRepository.getUsersByRole(UserRole.BUYER).first()
        binding.profileName.text = buyer.fullName
        binding.profileEmail.text = buyer.email
        binding.profilePhone.text = buyer.phone

        binding.orderHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.orderHistoryRecyclerView.adapter = OrderHistoryAdapter(
            MockRepository.getOrdersByBuyer(buyer.id)
        )
    }
}
