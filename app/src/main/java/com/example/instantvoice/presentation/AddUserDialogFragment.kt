package com.example.instantvoice.presentation

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instantvoice.databinding.FragmentAddUserDialogBinding
import com.example.instantvoice.presentation.adapter.UserAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddUserDialogFragment : DialogFragment() {

    private var _binding: FragmentAddUserDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoomViewModel by viewModels()
    val args: AddUserDialogFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddUserDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userAdapter = UserAdapter { user ->
            if (user.inRoom && !user.admin) {
                viewModel.removeMemberToRoom(args.roomId, user.uid)
            } else {
                viewModel.addMemberToRoom(args.roomId, user.uid)
            }

            viewModel.loadUsersInRoom(args.roomId)
        }

        binding.userListRecyclerView.adapter = userAdapter
        binding.userListRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.submitList(users)
            Log.e("UserRepository", "Rooms: $users")
        }
        viewModel.loadUsersInRoom(args.roomId)

    }

    override fun onResume() {
        val window = dialog!!.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window!!.setLayout((size.x * 0.90).toInt(), (size.y * 0.90).toInt())
        window!!.setGravity(Gravity.CENTER)
        super.onResume()
    }
}