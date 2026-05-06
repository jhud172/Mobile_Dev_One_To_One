package uk.ac.cardiff.trainerhub.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository
import uk.ac.cardiff.trainerhub.ui.components.AppBackground
import uk.ac.cardiff.trainerhub.ui.components.PremiumButton
import uk.ac.cardiff.trainerhub.ui.components.PremiumCard
import uk.ac.cardiff.trainerhub.ui.components.SectionTitle

data class SettingsUiState(
    val remindersEnabled: Boolean = true,
    val message: String? = null,
)

class SettingsViewModel(
    private val repository: TrainerHubRepository,
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.remindersEnabled,
        message,
    ) { remindersEnabled, currentMessage ->
        SettingsUiState(
            remindersEnabled = remindersEnabled,
            message = currentMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRemindersEnabled(enabled)
            message.value = if (enabled) {
                "Reminders turned on."
            } else {
                "Reminders turned off."
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            message.value = "Demo data reset completed."
        }
    }

    fun clearMessage() {
        message.value = null
    }

    companion object {
        fun factory(repository: TrainerHubRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SettingsViewModel(repository)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: TrainerHubRepository,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showSnackbar(uiState.message ?: "")
            viewModel.clearMessage()
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset demo data") },
            text = { Text("This will clear local changes and load the seeded trainer demo again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetDemoData()
                    },
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = { Text("Settings") },
            )
        },
    ) { innerPadding ->
        AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle(
                title = "Demo controls",
                subtitle = "These tools keep the assessment build reliable on a clean install.",
            )

            PremiumCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("Upcoming reminders", fontWeight = FontWeight.SemiBold)
                            Text("Local reminders cover upcoming sessions and overdue payments.")
                        }
                        Switch(
                            checked = uiState.remindersEnabled,
                            onCheckedChange = viewModel::setRemindersEnabled,
                            modifier = Modifier.semantics {
                                contentDescription = "Enable upcoming reminders"
                            },
                        )
                    }
            }

            PremiumCard(tonal = true) {
                Text("Privacy note", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Export and delete request actions are stored locally so the app can show GDPR-aware flows before a live backend is added.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PremiumButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset demo data")
            }
        }
        }
    }
}
