package com.example.mad_assignment.ui.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_assignment.viewModel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Us", color = textColorPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColorPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Get in Touch",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColorPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "We are here to help and answer any question you might have. We look forward to hearing from you.",
                        fontSize = 14.sp,
                        color = textColorSecondary
                    )
                }
            }

            ContactInfoCard(
                title = "Address",
                cardBg = cardBackgroundColor,
                titleColor = accentColor
            ) {
                Text(
                    text = "Curtin Malaysia.",
                    fontSize = 14.sp,
                    color = textColorPrimary,
                    lineHeight = 20.sp
                )
            }
            ContactInfoCard(
                title = "Business Hours",
                cardBg = cardBackgroundColor,
                titleColor = accentColor
            ) {
                Text(
                    text = "Office\n9:00am to 5:00pm\nMonday to Friday",
                    fontSize = 14.sp,
                    color = textColorPrimary,
                    lineHeight = 20.sp
                )
            }

            ContactInfoCard(
                title = "Phone",
                cardBg = cardBackgroundColor,
                titleColor = accentColor
            ) {
                Text(text = "General", fontSize = 12.sp, color = textColorSecondary)
                Text(
                    text = "+60 11-1639 6026",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 10.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+60 11-1639 6026"))
                            context.startActivity(intent)
                        }
                )
            }
            ContactInfoCard(
                title = "WhatsApp",
                cardBg = cardBackgroundColor,
                titleColor = accentColor
            ) {
                Text(
                    text = "Chat with Us Now",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/"))
                        context.startActivity(intent)
                    }
                )
            }
            ContactInfoCard(
                title = "Email",
                cardBg = cardBackgroundColor,
                titleColor = accentColor
            ) {
                Text(
                    text = "curtin@gmail.ccom",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:curtin@gmail.com")
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
    cardBg: androidx.compose.ui.graphics.Color,
    titleColor: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            content()
        }
    }
}