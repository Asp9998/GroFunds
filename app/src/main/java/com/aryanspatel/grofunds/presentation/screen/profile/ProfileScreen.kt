package com.aryanspatel.grofunds.presentation.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            var name by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(uid) {
                if (uid != null) {
                    val snap = FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
                        .get().await()
                    name = snap.getString("display_name") ?: FirebaseAuth.getInstance().currentUser?.displayName
                }
            }

            Text(text = name.orEmpty())

            Button(
                text = "log out", enabled = true,
                onClick = {authViewModel.signOut() },
            )
        }
    }
}