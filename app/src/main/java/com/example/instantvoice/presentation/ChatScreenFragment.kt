package com.example.instantvoice.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.instantvoice.R
import com.example.instantvoice.databinding.FragmentRoomDetailBinding
import com.example.instantvoice.framework.AudioFocusManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ChatScreenFragment : Fragment() {
    private var _binding: FragmentRoomDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoomViewModel by viewModels()
    val args: ChatScreenFragmentArgs by navArgs()
    private var mediaRecorder: MediaRecorder? = null
    private var filePath: String? = null
    private lateinit var audioFocusManager: AudioFocusManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        toolbarClick()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.roomNameTextView.text = args.name
        audioFocusManager = AudioFocusManager(requireContext())
        toolbarClick()
        viewModel.getCurrentUser()
        setToolbarItemVisibility()
        radioButtonsClick()
    }

    private fun toolbarClick() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.addUser -> {
                    showAddUserDialog()
                    true
                }

                else -> false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private fun radioButtonsClick() {
        binding.recordButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    vibratePhone(70)
                    v.setBackgroundResource(
                        R.drawable.circle_red
                    )
                    startRecording()

                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    vibratePhone(70)
                    v.setBackgroundResource(
                        R.drawable.circle
                    )
                    stopRecording()
                    true
                }

                else -> false
            }
        }
    }

    private fun showAddUserDialog() {
        val action =
            ChatScreenFragmentDirections.actionChatScreenFragmentToAddUserDialogFragment(args.roomID)
        findNavController().navigate(action)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording() {

        if (audioFocusManager.requestAudioFocus()) {
            val fileName = "${System.currentTimeMillis()}.3gp"
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            filePath = File(storageDir, fileName).absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(filePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
        audioFocusManager.abandonAudioFocus()
        filePath?.let { filePath ->
            viewModel.currentUserDTO.observe(viewLifecycleOwner) {
                if (it == null) {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT)
                        .show()
                    return@observe
                } else {
                    viewModel.uploadVoiceMessage(args.roomID, filePath, it.uid)
                }
            }
        }
    }

    private fun vibratePhone(duration: Long) {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(duration)
        }
    }


    private fun setToolbarItemVisibility() {
        viewModel.currentUserDTO.observe(viewLifecycleOwner) { user ->
            if (!user!!.admin) {
                binding.toolbar.menu.findItem(R.id.addUser).isEnabled = false
            }
        }
    }

}