package com.antigravity.bitcoinminingtycoon.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.antigravity.bitcoinminingtycoon.data.SaveReadiness

@Composable
fun SaveReadinessScreen(
    readiness: SaveReadiness,
    onStartNewSave: () -> Unit,
    onRetry: () -> Unit
) {
    val title = when (readiness) {
        SaveReadiness.Loading -> "Opening saved facility"
        SaveReadiness.Ready -> "Facility ready"
        SaveReadiness.RecoveryRestartRequired -> "Restart to start a new save"
        is SaveReadiness.CorruptCheckpointed -> "Save needs recovery"
        is SaveReadiness.CorruptUncheckpointed -> "Save is safely locked"
        is SaveReadiness.UnsupportedSchema -> "This save needs a newer app"
        is SaveReadiness.PersistenceFailed -> "Save could not be committed"
    }
    val detail = when (readiness) {
        SaveReadiness.Loading -> "Reading your local game data."
        SaveReadiness.Ready -> ""
        SaveReadiness.RecoveryRestartRequired -> "Your confirmation is saved. Close and reopen the app to create a clean schema-2 save. The recovery copy remains available in app-private storage."
        is SaveReadiness.CorruptCheckpointed -> "A private recovery copy was kept. Starting a new facility replaces the damaged save; the copy stays at filesDir/save_recovery/corrupt-save.json."
        is SaveReadiness.CorruptUncheckpointed -> "The original save is untouched, but a safe recovery copy could not be written. Free app storage and restart to try again. ${readiness.reason}"
        is SaveReadiness.UnsupportedSchema -> "Save schema ${readiness.schemaVersion} is newer than this app. Install a compatible newer build to preserve it. The save remains read-only."
        is SaveReadiness.PersistenceFailed -> "The last change was not published because it could not be saved. Check app storage, then retry."
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text(detail, style = MaterialTheme.typography.bodyLarge)
            if (readiness is SaveReadiness.CorruptCheckpointed) {
                Spacer(Modifier.height(24.dp))
                Text("This starts a new save. Your old save will not be restored automatically.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onStartNewSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics {
                            contentDescription = "Confirm start a new save; the recovery copy is preserved"
                        }
                ) {
                    Text("Start a new save")
                }
            } else if (readiness is SaveReadiness.PersistenceFailed) {
                Spacer(Modifier.height(24.dp))
                Button(onClick = onRetry, modifier = Modifier.height(52.dp)) { Text("Retry save loading") }
            }
        }
    }
}
