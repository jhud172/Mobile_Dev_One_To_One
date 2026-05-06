package uk.ac.cardiff.trainerhub.ui.clients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository
import uk.ac.cardiff.trainerhub.domain.ClientSortMode
import uk.ac.cardiff.trainerhub.domain.ClientSummary
import uk.ac.cardiff.trainerhub.ui.components.AppBackground
import uk.ac.cardiff.trainerhub.ui.components.EmptyStateCard
import uk.ac.cardiff.trainerhub.ui.components.PremiumCard
import uk.ac.cardiff.trainerhub.ui.components.SectionTitle
import uk.ac.cardiff.trainerhub.ui.components.StatusChip
import uk.ac.cardiff.trainerhub.ui.components.moneyText
import uk.ac.cardiff.trainerhub.ui.components.prettyDateTime

data class ClientsUiState(
    val clients: List<ClientSummary> = emptyList(),
    val searchText: String = "",
    val sortMode: ClientSortMode = ClientSortMode.NEXT_SESSION,
)

class ClientsViewModel(
    private val repository: TrainerHubRepository,
) : ViewModel() {
    private val searchText = MutableStateFlow("")

    val uiState: StateFlow<ClientsUiState> = combine(
        repository.observeClients(),
        repository.clientSortMode,
        searchText,
    ) { clients, sortMode, query ->
        val filtered = mutableListOf<ClientSummary>()
        for (client in clients) {
            val matchesSearch = query.isBlank() ||
                client.fullName.contains(query, ignoreCase = true) ||
                client.goal.contains(query, ignoreCase = true)
            if (matchesSearch) {
                filtered.add(client)
            }
        }

        val sorted = when (sortMode) {
            ClientSortMode.NAME -> filtered.sortedBy { it.fullName }
            ClientSortMode.NEXT_SESSION -> filtered.sortedWith(
                compareBy<ClientSummary> { it.nextSessionAt ?: Long.MAX_VALUE }
                    .thenBy { it.fullName },
            )
            ClientSortMode.PAYMENT_STATUS -> filtered.sortedWith(
                compareByDescending<ClientSummary> { it.overdueInvoices }
                    .thenBy { it.fullName },
            )
        }

        ClientsUiState(
            clients = sorted,
            searchText = query,
            sortMode = sortMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientsUiState(),
    )

    fun onSearchTextChange(value: String) {
        searchText.value = value
    }

    fun onSortModeSelected(mode: ClientSortMode) {
        viewModelScope.launch {
            repository.setClientSortMode(mode)
        }
    }

    companion object {
        fun factory(repository: TrainerHubRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ClientsViewModel(repository)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    repository: TrainerHubRepository,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onOpenClient: (String) -> Unit,
    onAddClient: () -> Unit,
) {
    val viewModel: ClientsViewModel = viewModel(
        factory = ClientsViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = { Text("Clients") },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add client") },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add client",
                    )
                },
                onClick = onAddClient,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { innerPadding ->
        AppBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionTitle(
                    title = "Trainer client book",
                    subtitle = "Search, sort, and review the current one-to-one load",
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.searchText,
                    onValueChange = viewModel::onSearchTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search clients") },
                    singleLine = true,
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = uiState.sortMode == ClientSortMode.NEXT_SESSION,
                            onClick = { viewModel.onSortModeSelected(ClientSortMode.NEXT_SESSION) },
                            label = { Text("Next session") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.sortMode == ClientSortMode.PAYMENT_STATUS,
                            onClick = { viewModel.onSortModeSelected(ClientSortMode.PAYMENT_STATUS) },
                            label = { Text("Payments") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.sortMode == ClientSortMode.NAME,
                            onClick = { viewModel.onSortModeSelected(ClientSortMode.NAME) },
                            label = { Text("Name") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }

            if (uiState.clients.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No matching clients",
                        message = "Try another search term, or tap Add client to create a new record.",
                    )
                }
            } else {
                items(uiState.clients) { client ->
                    PremiumCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Open client ${client.fullName}"
                            }
                            .clickable { onOpenClient(client.id) },
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = client.fullName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = client.goal,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                    )
                                }
                                StatusChip(client.status)
                            }

                            val nextSessionText = if (client.nextSessionAt == null) {
                                "No upcoming session"
                            } else {
                                "Next session: ${prettyDateTime(client.nextSessionAt)}"
                            }

                            Text(
                                text = nextSessionText,
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            Text(
                                text = "Outstanding value: ${moneyText(client.totalOutstandingPence)}",
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
