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
import uk.ac.cardiff.trainerhub.domain.ExercisePrescription
import uk.ac.cardiff.trainerhub.domain.PlanDraft
import uk.ac.cardiff.trainerhub.domain.PlanWeekDraft
import uk.ac.cardiff.trainerhub.ui.components.AppBackground
import uk.ac.cardiff.trainerhub.ui.components.PremiumButton
import androidx.compose.ui.text.input.KeyboardType

data class PlanEditorUiState(
    val planName: String = "Strength block",
    val goal: String = "Build confident movement",
    val weekNumber: String = "1",
    val exerciseName: String = "Squat",
    val sets: String = "3",
    val reps: String = "8",
    val notes: String = "",
    val errors: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

class PlanEditorViewModel(
    private val repository: TrainerHubRepository,
    private val clientId: String,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PlanEditorUiState())
    val uiState: StateFlow<PlanEditorUiState> = mutableState.asStateFlow()

    fun updatePlanName(value: String) {
        mutableState.value = mutableState.value.copy(planName = value)
    }

    fun updateGoal(value: String) {
        mutableState.value = mutableState.value.copy(goal = value)
    }

    fun updateWeekNumber(value: String) {
        mutableState.value = mutableState.value.copy(weekNumber = value)
    }

    fun updateExerciseName(value: String) {
        mutableState.value = mutableState.value.copy(exerciseName = value)
    }

    fun updateSets(value: String) {
        mutableState.value = mutableState.value.copy(sets = value)
    }

    fun updateReps(value: String) {
        mutableState.value = mutableState.value.copy(reps = value)
    }

    fun updateNotes(value: String) {
        mutableState.value = mutableState.value.copy(notes = value)
    }

    fun savePlan() {
        val state = mutableState.value
        val weekNumber = state.weekNumber.toIntOrNull()
        val sets = state.sets.toIntOrNull()
        val localErrors = mutableListOf<String>()

        if (weekNumber == null || weekNumber <= 0) {
            localErrors.add("Week number must be a valid number.")
        }
        if (state.exerciseName.isBlank()) {
            localErrors.add("Exercise name is required.")
        }
        if (sets == null || sets <= 0) {
            localErrors.add("Sets must be a valid number.")
        }
        if (state.reps.isBlank()) {
            localErrors.add("Reps are required.")
        }

        val draft = PlanDraft(
            clientId = clientId,
            title = state.planName,
            focus = state.goal,
            weeks = listOf(
                PlanWeekDraft(
                    weekNumber = weekNumber ?: 1,
                    summary = "Week ${weekNumber ?: 1} focus: ${state.goal.trim()}",
                    exercises = listOf(
                        ExercisePrescription(
                            exerciseName = state.exerciseName,
                            sets = sets ?: 0,
                            reps = state.reps,
                            notes = state.notes,
                        ),
                    ),
                ),
            ),
        )

        localErrors.addAll(BusinessRules.validatePlanDraft(draft))

        if (localErrors.isNotEmpty()) {
            mutableState.value = state.copy(errors = localErrors)
            return
        }

        viewModelScope.launch {
            mutableState.value = state.copy(
                errors = emptyList(),
                isSaving = true,
            )
            val saved = repository.createPlan(draft)
            mutableState.value = mutableState.value.copy(
                isSaving = false,
                isSaved = saved,
                errors = if (saved) emptyList() else listOf("The plan could not be saved."),
            )
        }
    }

    companion object {
        fun factory(
            repository: TrainerHubRepository,
            clientId: String,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    PlanEditorViewModel(repository, clientId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorScreen(
    repository: TrainerHubRepository,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    clientId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: PlanEditorViewModel = viewModel(
        factory = PlanEditorViewModel.factory(repository, clientId),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Training plan saved.")
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
                title = { Text("Create plan") },
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
                    onClick = viewModel::savePlan,
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
                        Text("Save plan")
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
                text = "Create one starting plan block for this client. Older plans stay in the client history.",
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = uiState.planName,
                onValueChange = viewModel::updatePlanName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Plan name") },
            )
            OutlinedTextField(
                value = uiState.goal,
                onValueChange = viewModel::updateGoal,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Goal") },
            )
            OutlinedTextField(
                value = uiState.weekNumber,
                onValueChange = viewModel::updateWeekNumber,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Week number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = uiState.exerciseName,
                onValueChange = viewModel::updateExerciseName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Exercise name") },
            )
            OutlinedTextField(
                value = uiState.sets,
                onValueChange = viewModel::updateSets,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Sets") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = uiState.reps,
                onValueChange = viewModel::updateReps,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Reps") },
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
