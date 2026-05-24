package com.project.store.ui.payment

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.project.store.R
import com.project.store.data.model.Payment
import com.project.store.data.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * PaymentBottomSheet - Interfaz de pago simulado.
 *
 * BottomSheetDialogFragment que presenta opciones de pago:
 * tarjeta de credito/debito, PSE y Nequi.
 * Simula procesamiento con 90% tasa de aprobacion.
 * Registra transacciones en la coleccion "payments" de Firestore.
 *
 * @author Julian
 */
class PaymentBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "PaymentBottomSheet"
        private const val ARG_ORDER_ID = "orderId"
        private const val ARG_AMOUNT = "amount"
        private const val ARG_BUYER_ID = "buyerId"

        fun newInstance(orderId: String, amount: Double, buyerId: String): PaymentBottomSheet {
            return PaymentBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORDER_ID, orderId)
                    putDouble(ARG_AMOUNT, amount)
                    putString(ARG_BUYER_ID, buyerId)
                }
            }
        }
    }

    var onPaymentResult: ((success: Boolean, transactionId: String) -> Unit)? = null
    private val repository = FirebaseRepository.getInstance()
    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderId = arguments?.getString(ARG_ORDER_ID).orEmpty()
        val amount = arguments?.getDouble(ARG_AMOUNT) ?: 0.0
        val buyerId = arguments?.getString(ARG_BUYER_ID).orEmpty()

        val tvAmount = view.findViewById<TextView>(R.id.tvPaymentAmount)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioPaymentMethod)
        val cardFields = view.findViewById<View>(R.id.cardFieldsContainer)
        val etCardNumber = view.findViewById<TextInputEditText>(R.id.etCardNumber)
        val etCardCvv = view.findViewById<TextInputEditText>(R.id.etCardCvv)
        val etCardExpiry = view.findViewById<TextInputEditText>(R.id.etCardExpiry)
        val btnPay = view.findViewById<MaterialButton>(R.id.btnPay)
        val progressBar = view.findViewById<ProgressBar>(R.id.paymentProgress)

        tvAmount.text = getString(R.string.payment_total, formatter.format(amount))
        btnPay.text = getString(R.string.payment_button, formatter.format(amount))

        etCardNumber.filters = arrayOf(InputFilter.LengthFilter(16))
        etCardCvv.filters = arrayOf(InputFilter.LengthFilter(3))
        etCardExpiry.filters = arrayOf(InputFilter.LengthFilter(5))

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            cardFields.visibility = if (checkedId == R.id.radioCard) View.VISIBLE else View.GONE
        }

        btnPay.setOnClickListener {
            val method = when (radioGroup.checkedRadioButtonId) {
                R.id.radioCard -> "card"
                R.id.radioPSE -> "pse"
                R.id.radioNequi -> "nequi"
                else -> "card"
            }

            btnPay.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val payment = Payment(
                orderId = orderId,
                buyerId = buyerId,
                amount = amount,
                method = method
            )

            lifecycleScope.launch {
                val result = repository.processPayment(payment)
                progressBar.visibility = View.GONE
                btnPay.isEnabled = true

                result.fold(
                    onSuccess = { finalPayment ->
                        val success = finalPayment.status == "approved"
                        onPaymentResult?.invoke(success, finalPayment.transactionId)
                        dismiss()
                    },
                    onFailure = {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_unknown),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}
