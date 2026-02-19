package com.geg.livekitstreamingdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.geg.livekitstreamingdemo.databinding.ActivityMainBinding
import com.geg.livekitstreamingdemo.manager.TokenRequester
import com.geg.livekitstreamingdemo.utils.ConnectionStatus
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    val tokenRequester: TokenRequester = TokenRequester(this)

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

            if (audioGranted && cameraGranted) {
                // Permessi concessi → puoi avviare microfono/camera
                onPermissionsGranted()
            } else {
                // Permesso negato
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            ConnectionStatus.state.collect { state ->
                binding.connectionStatus.text = state
            }
        }

        // Controlla e richiede i permessi
        checkPermissions()

    }

    private fun checkPermissions() {
        val audioPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (audioPermission == PackageManager.PERMISSION_GRANTED &&
            cameraPermission == PackageManager.PERMISSION_GRANTED
        ) {
            // Permessi già concessi
            onPermissionsGranted()
        } else {
            // Richiedi i permessi
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    private fun onPermissionsGranted() {
        tokenRequester.requestTokens()
    }
}