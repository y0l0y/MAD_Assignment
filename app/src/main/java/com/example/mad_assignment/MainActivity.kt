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
import com.example.mad_assignment.model.dao_and_database.AppDatabase
import com.example.mad_assignment.model.other.BookRepository
import com.example.mad_assignment.model.other.CatalogSeed
import com.example.mad_assignment.model.other.CoverStorage
import com.example.mad_assignment.model.other.NetworkMonitor
import com.example.mad_assignment.model.other.UserPreferencesRepository
import com.example.mad_assignment.model.remote.FirebaseDataSource
import com.example.mad_assignment.network.NetworkModule
import com.example.mad_assignment.ui.view.AppNav
import com.example.mad_assignment.viewModel.BookViewModel

class MainActivity : ComponentActivity() {
    private val bookViewModel: BookViewModel by viewModels{
        viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(applicationContext)
                BookViewModel(
                    repository = BookRepository(
                        catalogDao = db.catalogDao(),
                        remote = FirebaseDataSource(),
                        api = NetworkModule.api,
                        seed = CatalogSeed(applicationContext)
                    ),
                    preferencesRepo = UserPreferencesRepository(applicationContext),
                    coverStorage = CoverStorage(applicationContext),
                    network = NetworkMonitor(applicationContext)
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
