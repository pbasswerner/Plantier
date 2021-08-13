package com.example.plantier

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.badoo.reaktive.disposable.Disposable
import com.example.plantier.ui.theme.PlantierTheme
import com.vmadalin.easypermissions.EasyPermissions

class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var connectedDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        requestLocationPermission()

        setContent {
            PlantierTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Landing(onClick = { viewModel.scanForClue() }, name = "Paula")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //viewModel.scanForClue()
    }

    private fun hasPermissions() = EasyPermissions.hasPermissions(
        this,
        Manifest.permission.BLUETOOTH
    )

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}

@Composable
fun Landing(
    onClick: () -> Unit = {},
    name: String
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colors.background)) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Button(onClick = { onClick() }, Modifier.align(Alignment.Center)) {
            Text(text = "Connect")
        }
    }
}



