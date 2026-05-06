package uk.ac.cardiff.trainerhub.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository
import uk.ac.cardiff.trainerhub.domain.TrainerProfile
import uk.ac.cardiff.trainerhub.ui.components.AppBackground
import uk.ac.cardiff.trainerhub.ui.components.EmptyStateCard
import uk.ac.cardiff.trainerhub.ui.components.InfoRow
import uk.ac.cardiff.trainerhub.ui.components.PremiumCard
import uk.ac.cardiff.trainerhub.ui.components.SectionTitle
import uk.ac.cardiff.trainerhub.ui.components.StatusChip

data class TrainerProfileUiState(
    val profile: TrainerProfile? = null,
)

class TrainerProfileViewModel(
    repository: TrainerHubRepository,
) : ViewModel() {
    val uiState: StateFlow<TrainerProfileUiState> = repository.observeTrainerProfile()
        .map { profile ->
            TrainerProfileUiState(profile = profile)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TrainerProfileUiState(),
        )

    companion object {
        fun factory(repository: TrainerHubRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    TrainerProfileViewModel(repository)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerProfileScreen(
    repository: TrainerHubRepository,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    val viewModel: TrainerProfileViewModel = viewModel(
        factory = TrainerProfileViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = { Text("Trainer profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AppBackground {
        if (profile == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(contentPadding),
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
                    PremiumCard(tonal = true) {
                        SectionTitle(
                            title = profile.fullName,
                            subtitle = profile.headline,
                        )
                        if (profile.isVerified) {
                            StatusChip("VERIFIED")
                        }
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                item {
                    PremiumCard {
                        InfoRow(
                            label = "Working setup",
                            value = if (profile.operatesIndependently) {
                                "Independent coaching is enabled."
                            } else {
                                "Independent coaching is not enabled in this profile."
                            },
                        )
                    }
                }

                item {
                    Text(
                        text = "Gym affiliations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (profile.gymAffiliations.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No gym links",
                            message = "This trainer currently has no linked gym records.",
                        )
                    }
                } else {
                    items(profile.gymAffiliations) { gym ->
                        PremiumCard(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = gym,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
