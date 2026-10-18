package com.example.mad_assignment.ui.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.ui.components.Eyebrow
import com.example.mad_assignment.ui.components.FolioBackBar
import com.example.mad_assignment.ui.components.FolioCard
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.viewModel.BookViewModel

@Composable
fun ContactScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val cs = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            FolioBackBar(title = "Contact Us", onBack = onNavigateBack)
        },
        containerColor = cs.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FolioCard(contentPadding = 20.dp) {
                Text(
                    text = "Get in Touch",
                    style = MaterialTheme.typography.headlineMedium,
                    color = cs.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Muted("We are here to help and answer any question you might have. We look forward to hearing from you.")
            }

            ContactInfoCard(title = "Address") {
                Text(
                    text = "Curtin University Malaysia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface
                )
            }

            ContactInfoCard(title = "Business Hours") {
                Text(
                    text = "Office\n9:00am to 5:00pm\nMonday to Friday",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface
                )
            }

            ContactInfoCard(title = "Phone") {
                Muted("General", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "+60 11-1639 6026",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.primary,
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 10.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+601116396026"))
                            context.startActivity(intent)
                        }
                )
            }

            ContactInfoCard(title = "WhatsApp") {
                Text(
                    text = "Chat with Us Now",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/601116396026"))
                        context.startActivity(intent)
                    }
                )
            }

            ContactInfoCard(title = "Email") {
                Text(
                    text = "700053678@student.curtin.edu.my",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:700053678@student.curtin.edu.my")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ContactInfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    FolioCard(contentPadding = 18.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Eyebrow(title)
            content()
        }
    }
}