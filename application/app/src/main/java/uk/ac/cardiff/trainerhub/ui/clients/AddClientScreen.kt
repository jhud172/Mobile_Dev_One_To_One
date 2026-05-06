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
import uk.ac.cardiff.trainerhub.domain.NewClientDraft
import uk.ac.cardiff.trainerhub.ui.components.AppBackground
import uk.ac.cardiff.trainerhub.ui.components.PremiumButton
import androidx.compose.ui.text.input.KeyboardType

data class AddClientUiState(
    val fullName: String = "Demo Client",
    val email: String = "demo.client@example.com",
    val phone: String = "07000000000",
    val goal: String = "Start a structured coaching plan",
    val notes: String = "Created during the assessment demo.",
    val errors: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

class AddClientViewModel(
    private val repository: TrainerHubRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AddClientUiState())
    val uiState: StateFlow<AddClientUiState> = mutableState.asStateFlow()

    fun updateFullName(value: String) {
        mutableState.value = mutableState.value.copy(fullName = value)
    }

    fun updateEmail(value: String) {
        mutableState.value = mutableState.value.copy(email = value)
    }

    fun updatePhone(value: String) {
        mutableState.value = mutableState.value.copy(phone = value)
    }

    fun updateGoal(value: String) {
        mutableState.value = mutableState.value.copy(goal = value)
    }

    fun updateNotes(value: String) {
        mutableState.value = mutableState.value.copy(notes = value)
    }

    fun saveClient() {
        val draft = NewClientDraft(
            fullName = mutableState.value.fullName,
            email = mutableState.value.email,
            phone = mutableState.value.phone,
            goal = mutableState.value.goal,
            notes = mutableState.value.notes,
        )
        val errors = BusinessRules.validateClientDraft(draft)
        if (errors.isNotEmpty()) {
            mutableState.value = mutableState.value.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(
                errors = emptyList(),
                isSaving = true,
            )
            val saved = repository.addClient(draft)
            mutableState.value = mutableState.value.copy(
                isSaving = false,
                isSaved = saved,
                errors = if (saved) emptyList() else listOf("The client could not be saved."),
            )
        }
    }

    companion object {
        fun factory(repository: TrainerHubRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    AddClientViewModel(repository)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClientScreen(
    repository: TrainerHubRepository,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: AddClientViewModel = viewModel(
        factory = AddClientViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Client added to the trainer list.")
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
                title = { Text("Add client") },
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
                    onClick = viewModel::saveClient,
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
                        Text("Save client")
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
                text = "Add a client record. The app links the client to the current trainer automatically.",
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::updateFullName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full name") },
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::updatePhone,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.goal,
                onValueChange = viewModel::updateGoal,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Primary goal") },
            )
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 4,
            )

            for (error in uiState.errors) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        }
    }
}
