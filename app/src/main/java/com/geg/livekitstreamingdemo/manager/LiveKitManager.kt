package com.geg.livekitstreamingdemo.manager

import android.content.Context
import android.util.Log
import com.geg.livekitstreamingdemo.utils.AppConstants
import com.geg.livekitstreamingdemo.utils.ConnectionStatus
import io.livekit.android.AudioOptions
import io.livekit.android.AudioType
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.audio.AudioSwitchHandler
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.util.LoggingLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LiveKitManager (
    private val context: Context
) {

    private val livekitServerUrl = "wss://test-b4x6fxz3.livekit.cloud" // SET WEBSOCKET URL HERE

    // used to cancel the coroutine scope if necessary
    private val serviceJob = SupervisorJob()

    // used for all coroutines
    private val coroutineScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val roomNamesAndTokensMap = HashMap<String, TokenRequester.TokenResponse?>()
    private val roomNamesAndRoomsMap = HashMap<String, Room?>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    init {
        scope.launch {

            LiveKit.loggingLevel = LoggingLevel.VERBOSE

            while (true) {
                var status = "\n"
                for((key, value) in roomNamesAndRoomsMap) {
                    status += key + ": " + value?.state + "\n"
                }
                Log.i(AppConstants.LOGS.LOG_TAG, "All Room Status every 5 seconds: $status")
                ConnectionStatus.set(status)
                delay(5000)
            }
        }
    }

    // --- Room Connection ---

    fun launchConnectToRoom(token: TokenRequester.TokenResponse) = runBlocking {
        connectToRoom(token)
    }

    suspend fun connectToRoom(token: TokenRequester.TokenResponse) {

        val roomName = token.roomName

        if(roomNamesAndRoomsMap[roomName]?.state == Room.State.CONNECTED) {
            Log.i(AppConstants.LOGS.LOG_TAG, "Already connected to room $roomName. Skipping registration")
            return
        }

        roomNamesAndTokensMap[token.roomName] = token

        val audioHandler = AudioSwitchHandler(context)

        val audioOptions = AudioOptions(disableCommunicationModeWorkaround = true, audioOutputType = AudioType.CallAudioType(), audioHandler = audioHandler)
        val room = LiveKit.create(context, overrides = LiveKitOverrides(audioOptions = audioOptions)).apply {
        }

        room.prepareConnection(livekitServerUrl, token.participantToken)

        coroutineScope.launch {
            room.events.collect { event ->
                when (event) {
                    is RoomEvent.Connected -> onConnected(event, roomName)
                    is RoomEvent.Disconnected -> onDisconnected(event, roomName)
                    is RoomEvent.Reconnecting -> onReconnecting(event, roomName)
                    is RoomEvent.Reconnected -> onReconnected(event, roomName)
                    is RoomEvent.TrackPublished -> onTrackPublished(event, roomName)
                    is RoomEvent.TrackSubscribed -> onTrackSubscribed(event, roomName)
                    is RoomEvent.TrackUnsubscribed -> onTrackUnsubscribed(event, roomName)
                    else -> {}
                }
            }
        }

        val connectOptions = ConnectOptions(autoSubscribe = true)
        try {
            room.connect(livekitServerUrl, token.participantToken, connectOptions)

            roomNamesAndRoomsMap[token.roomName] = room

        } catch(ex1: IllegalStateException) {
            Log.i(AppConstants.LOGS.LOG_TAG, "Error while connecting to room $roomName: ${ex1.message}")
        } catch (ex2: Exception) {
            Log.i(AppConstants.LOGS.LOG_TAG, "Error while connecting to room $roomName: ${ex2.message}")
        }
    }

    private fun onTrackPublished(event: RoomEvent.TrackPublished, roomName: String) {
        Log.i(AppConstants.LOGS.LOG_TAG, "OnPublished: ${event.publication.name}")
    }

    private fun onTrackSubscribed(event: RoomEvent.TrackSubscribed, roomName: String) {
        Log.i(AppConstants.LOGS.LOG_TAG, "onSubscribed: ${event.publication.name}")
    }

    private fun onTrackUnsubscribed(event: RoomEvent.TrackUnsubscribed, roomName: String) {
        Log.i(AppConstants.LOGS.LOG_TAG, "onUnsubscribed: ${event.publications.name}")
    }

    // --- Room Events ---

    private fun onConnected(event: RoomEvent.Connected, roomName: String) {
        Log.i(AppConstants.LOGS.LOG_TAG, "onConnected: $roomName")

//        ConnectionStatus.set(ConnectionState.CONNECTED)
    }

    private fun onDisconnected(event: RoomEvent.Disconnected, roomName: String) {
        Log.i(AppConstants.LOGS.LOG_TAG, "onDisconnected: $roomName")

//        ConnectionStatus.set(ConnectionState.DISCONNECTED)
    }

    private fun onReconnecting(event: RoomEvent.Reconnecting, roomName: String) {
        Log.i(AppConstants.LOGS.LOG_TAG, "onReconnecting: $roomName")

//        ConnectionStatus.set(ConnectionState.RECONNECTING)
    }

    private fun onReconnected(event: RoomEvent.Reconnected, roomName: String) {
        Log.i(AppConstants.LOGS.LOG_TAG, "onReconnected: $roomName")

//        ConnectionStatus.set(ConnectionState.CONNECTED)
    }

}