package com.example.mad_assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mad_assignment.Model.AppDatabase
import com.example.mad_assignment.Model.BookRepository
import com.example.mad_assignment.Model.CoverStorage
import com.example.mad_assignment.Model.UserPreferencesRepository
import com.example.mad_assignment.Network.NetworkModule
import com.example.mad_assignment.ViewModel.BookViewModel
import com.example.mad_assignment.ui.View.AppNav

class MainActivity : ComponentActivity() {
    private val bookViewModel: BookViewModel by viewModels{
        viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(applicationContext)
                BookViewModel(
                    repository = BookRepository(db.bookDao(), NetworkModule.api),
                    preferencesRepo = UserPreferencesRepository(applicationContext),
                    coverStorage = CoverStorage(applicationContext)
                )
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = bookViewModel.isDarkMode
            MaterialTheme(
                colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()
            ){
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(viewModel = bookViewModel)
                }
            }
        }
    }
}
