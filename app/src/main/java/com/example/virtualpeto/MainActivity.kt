package com.example.virtualpeto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                onSpawnClick = { pet ->
                    if (Settings.canDrawOverlays(this)) {
                        val serviceIntent = Intent(this, PetService::class.java).apply {
                            putExtra("PET_URI", pet.uri.toString())
                        }
                        startService(serviceIntent)
                    } else {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                },
                onClosePetClick = { pet ->
                    stopService(Intent(this, PetService::class.java))
                }
            )
        }
    }
}