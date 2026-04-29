package uk.ac.cardiff.trainerhub.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository
import uk.ac.cardiff.trainerhub.domain.ClientDetail
import uk.ac.cardiff.trainerhub.ui.components.EmptyStateCard
import uk.ac.cardiff.trainerhub.ui.components.SectionTitle
import uk.ac.cardiff.trainerhub.ui.components.StatusChip
import uk.ac.cardiff.trainerhub.ui.components.TimelineRow
import uk.ac.cardiff.trainerhub.ui.components.moneyText
import uk.ac.cardiff.trainerhub.ui.components.prettyDate
import uk.ac.cardiff.trainerhub.ui.components.prettyDateTime

data class ClientDetailUiState(
    val detail: ClientDetail? = null,
    val message: String? = null,
)

class ClientDetailViewModel(
    private val repository: TrainerHubRepository,
    clientId: String,
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)
    val uiState: StateFlow<ClientDetailUiState> = combine(
        repository.observeClientDetail(clientId),
        message,
    ) { detail, currentMessage ->
        ClientDetailUiState(
            detail = detail,
            message = currentMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientDetailUiState(),
    )

    fun markInvoicePaid(invoiceId: String) {
        viewModelScope.launch {
            val success = repository.markInvoicePaid(invoiceId)
            message.value = if (success) "Invoice marked as paid." else "The payment update failed."
        }
    }

    fun completeSession(sessionId: String, notes: String) {
        viewModelScope.launch {
            val success = repository.completeSession(sessionId, notes)
            message.value = if (success) "Session marked as completed." else "The session could not be updated."
        }
    }

    fun cancelSession(sessionId: String, notes: String) {
        viewModelScope.launch {
            val success = repository.cancelSession(sessionId, notes)
            message.value = if (success) "Session cancelled." else "The session could not be cancelled."
        }
    }

    fun requestExport(clientId: String) {
        viewModelScope.launch {
            val success = repository.requestExport(clientId)
            message.value = if (success) "Export request added to the record." else "Export request failed."
        }
    }

    fun requestDelete(clientId: String) {
        viewModelScope.launch {
            val success = repository.requestDelete(clientId)
            message.value = if (success) "Delete request added to the record." else "Delete request failed."
        }
    }

    fun clearMessage() {
        message.value = null
    }

    companion object {
        fun factory(
            repository: TrainerHubRepository,
            clientId: String,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ClientDetailViewModel(repository, clientId)
                }
            }
        }
    }
}

@Composable
fun ClientDetailScreen(
    repository: TrainerHubRepository,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    clientId: String,
    onBack: () -> Unit,
    onCreatePlan: (String) -> Unit,
    onCreateSession: (String) -> Unit,
    onCreateInvoice: (String) -> Unit,
) {
    val viewModel: ClientDetailViewModel = viewModel(
        factory = ClientDetailViewModel.factory(
            repository = repository,
            clientId = clientId,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = uiState.detail
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showSnackbar(uiState.message ?: "")
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.fullName ?: "Client detail") },
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
        if (detail == null) {
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
            val tabs = listOf("Overview", "Plans", "Sessions", "Payments", "Notes", "Privacy")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    for ((index, tab) in tabs.withIndex()) {
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tab) },
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = contentPadding.calculateTopPadding() + 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (selectedTab) {
                        0 -> overviewItems(detail)
                        1 -> planItems(detail, onCreatePlan)
                        2 -> sessionItems(detail, viewModel, onCreateSession)
                        3 -> paymentItems(detail, viewModel, onCreateInvoice)
                        4 -> noteItems(detail)
                        else -> privacyItems(detail, viewModel)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.overviewItems(detail: ClientDetail) {
    item {
        SectionTitle(
            title = detail.fullName,
            subtitle = detail.goal,
        )
    }

    item {
        StatusChip(detail.status)
    }

    item {
        Text("Assigned trainer", fontWeight = FontWeight.SemiBold)
        Text(detail.assignedTrainerName)
    }

    item {
        Text("Contact", fontWeight = FontWeight.SemiBold)
        Text(detail.email)
        Text(detail.phone)
    }

    item {
        Text("Gym access", fontWeight = FontWeight.SemiBold)
        if (detail.assignedGyms.isEmpty()) {
            Text("Independent coaching setup")
        } else {
            for (gym in detail.assignedGyms) {
                Text(gym)
            }
        }
    }

    item {
        Text("Notes", fontWeight = FontWeight.SemiBold)
        Text(detail.notes)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.planItems(
    detail: ClientDetail,
    onCreatePlan: (String) -> Unit,
) {
    item {
        Button(
            onClick = { onCreatePlan(detail.id) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create plan")
        }
    }

    if (detail.plans.isEmpty()) {
        item {
            EmptyStateCard(
                title = "No plan yet",
                message = "This client does not have an active training plan at the moment.",
            )
        }
    } else {
        items(detail.plans) { plan ->
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(plan.focus)
                if (plan.isActive) {
                    StatusChip("ACTIVE")
                }
                for (week in plan.weeks) {
                    Text("Week ${week.weekNumber}: ${week.summary}", fontWeight = FontWeight.Medium)
                    for (exercise in week.exercises) {
                        Text("• ${exercise.exerciseName} - ${exercise.sets} x ${exercise.reps}")
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.sessionItems(
    detail: ClientDetail,
    viewModel: ClientDetailViewModel,
    onCreateSession: (String) -> Unit,
) {
    val now = System.currentTimeMillis()
    val upcomingSessions = detail.sessions.filter {
        it.status == "SCHEDULED" && it.scheduledAt >= now
    }
    val pastSessions = detail.sessions.filter {
        it.status != "SCHEDULED" || it.scheduledAt < now
    }

    item {
        Button(
            onClick = { onCreateSession(detail.id) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create session")
        }
    }

    if (detail.sessions.isEmpty()) {
        item {
            EmptyStateCard(
                title = "No sessions",
                message = "There are no scheduled or completed sessions for this client yet.",
            )
        }
    } else {
        item {
            Text("Upcoming sessions", fontWeight = FontWeight.SemiBold)
        }

        if (upcomingSessions.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No upcoming sessions",
                    message = "Add a session to show the client schedule here.",
                )
            }
        } else {
            items(upcomingSessions) { session ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${prettyDateTime(session.scheduledAt)} • ${session.location}")
                    StatusChip(session.status)
                    if (session.status == "SCHEDULED") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = { viewModel.completeSession(session.id, session.notes) },
                            ) {
                                Text("Mark completed")
                            }
                            Button(
                                onClick = { viewModel.cancelSession(session.id, session.notes) },
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Past sessions", fontWeight = FontWeight.SemiBold)
        }

        if (pastSessions.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No past sessions",
                    message = "Completed or earlier sessions will appear here.",
                )
            }
        } else {
            items(pastSessions) { session ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${prettyDateTime(session.scheduledAt)} • ${session.location}")
                    StatusChip(session.status)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.paymentItems(
    detail: ClientDetail,
    viewModel: ClientDetailViewModel,
    onCreateInvoice: (String) -> Unit,
) {
    item {
        Button(
            onClick = { onCreateInvoice(detail.id) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create invoice")
        }
    }

    if (detail.invoices.isEmpty()) {
        item {
            EmptyStateCard(
                title = "No invoices",
                message = "Payments for this client will appear here when they are created.",
            )
        }
    } else {
        items(detail.invoices) { invoice ->
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(invoice.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Due ${prettyDate(invoice.dueAt)} • ${moneyText(invoice.amountPence)}")
                StatusChip(invoice.status)
                if (invoice.paymentReference != null) {
                    Text("Reference: ${invoice.paymentReference}")
                }
                if (invoice.status != "PAID") {
                    Button(
                        onClick = { viewModel.markInvoicePaid(invoice.id) },
                    ) {
                        Text("Mark paid")
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.noteItems(detail: ClientDetail) {
    if (detail.sessionNotes.isEmpty()) {
        item {
            EmptyStateCard(
                title = "No notes",
                message = "Session notes will appear here once they are recorded.",
            )
        }
    } else {
        items(detail.sessionNotes) { note ->
            TimelineRow(
                title = note.sessionTitle,
                subtitle = "${prettyDateTime(note.createdAt)} • ${note.content}",
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.privacyItems(
    detail: ClientDetail,
    viewModel: ClientDetailViewModel,
) {
    item {
        SectionTitle(
            title = "Consent and privacy",
            subtitle = "Demo actions for GDPR-aware platform behaviour",
        )
    }

    if (detail.consents.isEmpty()) {
        item {
            EmptyStateCard(
                title = "No consent records",
                message = "Consent records will be listed here for this client.",
            )
        }
    } else {
        items(detail.consents) { consent ->
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(consent.consentType, fontWeight = FontWeight.SemiBold)
                    StatusChip(if (consent.granted) "ACTIVE" else "ATTENTION")
                }
                Text(prettyDateTime(consent.recordedAt))
                Text(consent.details)
            }
        }
    }

    item {
        Button(
            onClick = { viewModel.requestExport(detail.id) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create export request")
        }
    }

    item {
        Button(
            onClick = { viewModel.requestDelete(detail.id) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create delete request")
        }
    }
}
