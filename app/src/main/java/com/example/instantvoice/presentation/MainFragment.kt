package com.example.instantvoice.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instantvoice.R
import com.example.instantvoice.core.data.model.RoomDTO
import com.example.instantvoice.core.data.model.UserDTO
import com.example.instantvoice.databinding.FragmentMainBinding
import com.example.instantvoice.presentation.adapter.RoomAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var roomAdapter: RoomAdapter

    private lateinit var currentUserDTO: UserDTO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        viewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomAdapter = RoomAdapter { room ->
            navigateToRoomDetails(room)
        }
        binding.recyclerViewRooms.adapter = roomAdapter
        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(context)


        viewModel.rooms.observe(viewLifecycleOwner) { rooms ->
            roomAdapter.submitList(rooms)
            Log.d("UserRepository", "Rooms: $rooms")
        }
        viewModel.startListeningToUserRooms()


        setToolbarItemVisibility()
        toolbarClick()
        viewModel.setUserToken()

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
    }

    private fun toolbarClick() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.addRoom -> {
                    showAddRoomDialog()
                    true
                }

                R.id.mute -> {
                    viewModel.mute()
                    true
                }

                R.id.unMute -> {
                    viewModel.mute()
                    true
                }

                R.id.signOut -> {
                    viewModel.logout()
                    navigateToLogin()
                    true
                }

                else -> false
            }
        }
    }

    private fun setToolbarItemVisibility() {

        viewModel.currentUserDTO.observe(viewLifecycleOwner) { user ->
            user?.let {
                if (it.ismute) {
                    binding.toolbar.menu.findItem(R.id.unMute).isVisible = true
                    binding.toolbar.menu.findItem(R.id.mute).isVisible = false

                } else {
                    binding.toolbar.menu.findItem(R.id.mute).isVisible = true
                    binding.toolbar.menu.findItem(R.id.unMute).isVisible = false
                }
                if (!it.admin) {
                    binding.toolbar.menu.findItem(R.id.addRoom).isEnabled = false
                }
            }

        }
    }


    private fun showAddRoomDialog() {
        val dialog = AddRoomDialogFragment()
        dialog.show(parentFragmentManager, "AddRoomDialog")
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
    }

    private fun navigateToRoomDetails(roomDTO: RoomDTO) {
        val action = MainFragmentDirections.actionMainFragmentToChatScreenFragment(
            roomDTO.roomID,
            roomDTO.name
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
