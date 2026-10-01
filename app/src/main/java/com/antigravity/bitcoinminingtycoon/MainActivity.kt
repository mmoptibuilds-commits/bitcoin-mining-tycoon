package com.antigravity.bitcoinminingtycoon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.antigravity.bitcoinminingtycoon.app.BitcoinMiningTycoonApp
import com.antigravity.bitcoinminingtycoon.ui.navigation.AppNavHost
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import com.antigravity.bitcoinminingtycoon.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels {
        val app = application as BitcoinMiningTycoonApp
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(app.repository, app.clockProvider, app.soundPlayer, app.haptics) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BitcoinMiningTycoonTheme {
                AppNavHost(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startTicker()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopTicker()
        val app = application as? BitcoinMiningTycoonApp
        if (app != null) {
            lifecycleScope.launch {
                app.repository.flush()
            }
        }
    }
}
