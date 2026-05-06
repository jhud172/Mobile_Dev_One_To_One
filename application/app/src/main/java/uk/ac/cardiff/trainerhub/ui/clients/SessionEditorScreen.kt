package uk.ac.cardiff.trainerhub.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository
import uk.ac.cardiff.trainerhub.domain.BusinessRules
import uk.ac.cardiff.trainerhub.domain.SessionDraft
import uk.ac.cardiff.trainerhub.ui.components.AppBackground
import uk.ac.cardiff.trainerhub.ui.components.PremiumButton
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class SessionEditorUiState(
    val date: String = defaultSessionDate(),
    val time: String = "10:00",
    val location: String = "Foundry West",
    val sessionType: String = "Strength",
    val notes: String = "",
    val errors: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

private fun defaultSessionDate(): String {
    return LocalDate.now().plusDays(1).toString()
}

class SessionEditorViewModel(
    private val repository: TrainerHubRepository,
    private val clientId: String,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SessionEditorUiState())
    val uiState: StateFlow<SessionEditorUiState> = mutableState.asStateFlow()

    fun updateDate(value: String) {
        mutableState.value = mutableState.value.copy(date = value)
    }

    fun updateTime(value: String) {
        mutableState.value = mutableState.value.copy(time = value)
    }

    fun updateLocation(value: String) {
        mutableState.value = mutableState.value.copy(location = value)
    }

    fun updateSessionType(value: String) {
        mutableState.value = mutableState.value.copy(sessionType = value)
    }

    fun updateNotes(value: String) {
        mutableState.value = mutableState.value.copy(notes = value)
    }

    fun saveSession() {
        val state = mutableState.value
        val errors = mutableListOf<String>()
        val scheduledAt = parseDateTime(state.date, state.time)

        if (scheduledAt == null) {
            errors.add("Use a valid date and time.")
        }
        if (state.sessionType.isBlank()) {
            errors.add("Session type is required.")
        }

        val title = if (state.sessionType.isBlank()) {
            ""
        } else {
            "${state.sessionType.trim()} session"
        }

        val draft = SessionDraft(
            clientId = clientId,
            title = title,
            scheduledAt = scheduledAt ?: 0L,
            durationMinutes = 60,
            location = state.location,
            type = state.sessionType,
            notes = state.notes,
        )

        errors.addAll(BusinessRules.validateSessionDraft(draft))

        if (errors.isNotEmpty()) {
            mutableState.value = state.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            mutableState.value = state.copy(
                errors = emptyList(),
                isSaving = true,
            )
            val saved = repository.createSession(draft)
            mutableState.value = mutableState.value.copy(
                isSaving = false,
                isSaved = saved,
                errors = if (saved) emptyList() else listOf("The session could not be saved."),
            )
        }
    }

    private fun parseDateTime(
        dateText: String,
        timeText: String,
    ): Long? {
        return try {
            val date = LocalDate.parse(dateText.trim())
            val time = LocalTime.parse(timeText.trim())
            date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun factory(
            repository: TrainerHubRepository,
            clientId: String,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SessionEditorViewModel(repository, clientId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionEditorScreen(
    repository: TrainerHubRepository,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    clientId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: SessionEditorViewModel = viewModel(
        factory = SessionEditorViewModel.factory(repository, clientId),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Session saved.")
            onSaved()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = { Text("Create session") },
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
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                PremiumButton(
                    onClick = viewModel::saveSession,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Save session")
                    }
                }
            }
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
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Use date format YYYY-MM-DD and time format HH:MM.",
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = uiState.date,
                onValueChange = viewModel::updateDate,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") },
            )
            OutlinedTextField(
                value = uiState.time,
                onValueChange = viewModel::updateTime,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Time") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = uiState.location,
                onValueChange = viewModel::updateLocation,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Location") },
            )
            OutlinedTextField(
                value = uiState.sessionType,
                onValueChange = viewModel::updateSessionType,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Session type") },
            )
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 3,
            )

            for (error in uiState.errors) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        }
    }
}
