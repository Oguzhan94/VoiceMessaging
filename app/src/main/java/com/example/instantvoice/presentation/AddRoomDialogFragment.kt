package com.example.instantvoice.presentation

import android.app.Dialog
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.instantvoice.R
import com.example.instantvoice.databinding.AddRoomDialogBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddRoomDialogFragment : DialogFragment() {

    private var _binding: AddRoomDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddRoomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddRoom.setOnClickListener {
            val roomName = binding.etRoomName.text.toString()
            if (roomName.isNotBlank()) {
                viewModel.addRoom(roomName)
            } else {
                Toast.makeText(requireContext(), R.string.roomNameCanNotBeEmpty, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }


        viewModel.addRoomStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is MainViewModel.AddRoomStatus.Success -> {
                    Toast.makeText(requireContext(), R.string.successRoomAdd, Toast.LENGTH_SHORT)
                        .show()
                    dismiss()
                }

                else -> {}
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onResume() {
        val window = dialog!!.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window!!.setLayout((size.x * 0.90).toInt(), (size.y * 0.40).toInt())
        window!!.setGravity(Gravity.CENTER)
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
