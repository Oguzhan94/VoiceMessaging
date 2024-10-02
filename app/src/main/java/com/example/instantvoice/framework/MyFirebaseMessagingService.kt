package com.example.instantvoice.framework

import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.instantvoice.core.data.repository.UserRepositoryImpl
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var userRepositoryImpl: UserRepositoryImpl

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.let {
            val body = it["body"]
            val title = it["title"]
            if (body != null) {
                val inputData = workDataOf(
                    "url" to body,
                    "title" to title
                )
                val workRequest =
                    OneTimeWorkRequestBuilder<PlayVoiceMessageWorker>().setInputData(inputData)
                        .build()

                WorkManager.getInstance(this).enqueue(workRequest)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TAG", "Refreshed token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userRepositoryImpl.updateUserFcmToken()
            } catch (e: Exception) {
                Log.e("TAG", "Error updating FCM token", e)
            }
        }
    }
}


