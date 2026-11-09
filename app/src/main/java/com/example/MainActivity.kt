package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.StoreRepository
import com.example.ui.screens.MainScaffold
import com.example.ui.theme.EbraKhaitTheme
import com.example.ui.viewmodel.StoreViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EbraKhaitTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val context = LocalContext.current
                    val storeViewModel: StoreViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val db = AppDatabase.getInstance(context)
                                val repo = StoreRepository(db.storeDao())
                                @Suppress("UNCHECKED_CAST")
                                return StoreViewModel(repo) as T
                            }
                        }
                    )

                    MainScaffold(
                        storeViewModel = storeViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
