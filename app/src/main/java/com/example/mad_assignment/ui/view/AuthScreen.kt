package com.example.mad_assignment.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.ui.components.Eyebrow
import com.example.mad_assignment.ui.components.FolioBackBar
import com.example.mad_assignment.ui.components.FolioBanner
import com.example.mad_assignment.ui.components.FolioButton
import com.example.mad_assignment.ui.components.FolioFieldShape
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.ui.components.Tone
import com.example.mad_assignment.ui.components.folioFieldColors
import com.example.mad_assignment.viewModel.BookViewModel

/* sign in / create account, opened on demand (guests can browse without it) */
@Composable
fun AuthScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    var isSignUp by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.clearAuthError() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FolioBackBar(
                title = if (isSignUp) "Create account" else "Sign in",
                onBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Eyebrow(if (isSignUp) "Join the club" else "Welcome back")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isSignUp) "Create your\naccount." else "Good to see\nyou again.",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(Modifier.height(8.dp))
                Muted(
                    text = "Guests can browse the shelf. Sign in to borrow books.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
            }
            if (isSignUp) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    singleLine = true,
                    shape = FolioFieldShape,
                    colors = folioFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                shape = FolioFieldShape,
                colors = folioFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                shape = FolioFieldShape,
                colors = folioFieldColors(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            vm.authError?.let {
                FolioBanner(
                    body = it,
                    tone = Tone.Danger,
                    icon = Icons.Outlined.ErrorOutline
                )
            }
            FolioButton(
                text = if (isSignUp) "Create account" else "Sign in",
                onClick = {
                    if (isSignUp) vm.signUp(name, email, password, onNavigateBack)
                    else vm.signIn(email, password, onNavigateBack)
                },
                enabled = !vm.authBusy,
                loading = vm.authBusy
            )
            TextButton(
                onClick = {
                    isSignUp = !isSignUp
                    vm.clearAuthError()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? Sign in" else "No account? Create one",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}