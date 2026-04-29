package uk.ac.cardiff.trainerhub.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository
import uk.ac.cardiff.trainerhub.domain.DashboardSnapshot
import uk.ac.cardiff.trainerhub.domain.TrainerProfile
import uk.ac.cardiff.trainerhub.ui.components.EmptyStateCard
import uk.ac.cardiff.trainerhub.ui.components.SectionTitle
import uk.ac.cardiff.trainerhub.ui.components.StatCard
import uk.ac.cardiff.trainerhub.ui.components.StatusChip
import uk.ac.cardiff.trainerhub.ui.components.prettyDateTime

data class DashboardUiState(
    val snapshot: DashboardSnapshot? = null,
    val trainerProfile: TrainerProfile? = null,
)

class DashboardViewModel(
    repository: TrainerHubRepository,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeDashboard(),
        repository.observeTrainerProfile(),
    ) { snapshot, trainerProfile ->
        DashboardUiState(
            snapshot = snapshot,
            trainerProfile = trainerProfile,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    companion object {
        fun factory(repository: TrainerHubRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    DashboardViewModel(repository)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    repository: TrainerHubRepository,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onOpenTrainerProfile: () -> Unit,
) {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshot = uiState.snapshot
    val trainerProfile = uiState.trainerProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Trainer Hub")
                        Text(
                            text = trainerProfile?.fullName ?: "Loading trainer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTrainerProfile) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Trainer profile",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (snapshot == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "Premium trainer overview",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = trainerProfile?.headline ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (trainerProfile?.isVerified == true) {
                                    StatusChip("VERIFIED")
                                }
                            }

                            Text(
                                text = trainerProfile?.bio ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StatCard(
                            label = "Active clients",
                            value = snapshot.activeClientCount.toString(),
                            supportingText = "Current live coaching load",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Overdue",
                            value = snapshot.overdueInvoices.toString(),
                            supportingText = "Payment items to review",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    SectionTitle(
                        title = "Today",
                        subtitle = "Sessions booked for the current day",
                    )
                }

                if (snapshot.todaySessions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No sessions today",
                            message = "Your dashboard is clear for now. New bookings will appear here.",
                        )
                    }
                } else {
                    items(snapshot.todaySessions) { session ->
                        Card {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "${prettyDateTime(session.scheduledAt)} • ${session.location}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                StatusChip(session.status)
                            }
                        }
                    }
                }

                item {
                    SectionTitle(
                        title = "Attention",
                        subtitle = "Important actions surfaced from the current data",
                    )
                }

                if (snapshot.alerts.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Nothing urgent",
                            message = "There are no alerts at the moment.",
                        )
                    }
                } else {
                    items(snapshot.alerts) { alert ->
                        Card {
                            Text(
                                text = alert,
                                modifier = Modifier.padding(18.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
