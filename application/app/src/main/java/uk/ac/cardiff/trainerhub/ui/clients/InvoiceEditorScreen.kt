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
import uk.ac.cardiff.trainerhub.domain.InvoiceDraft
import uk.ac.cardiff.trainerhub.ui.components.AppBackground
import uk.ac.cardiff.trainerhub.ui.components.PremiumButton
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate
import java.time.ZoneId

data class InvoiceEditorUiState(
    val title: String = "Coaching invoice",
    val amount: String = "100.00",
    val dueDate: String = defaultInvoiceDueDate(),
    val notes: String = "",
    val errors: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

private fun defaultInvoiceDueDate(): String {
    return LocalDate.now().plusDays(7).toString()
}

class InvoiceEditorViewModel(
    private val repository: TrainerHubRepository,
    private val clientId: String,
) : ViewModel() {
    private val mutableState = MutableStateFlow(InvoiceEditorUiState())
    val uiState: StateFlow<InvoiceEditorUiState> = mutableState.asStateFlow()

    fun updateTitle(value: String) {
        mutableState.value = mutableState.value.copy(title = value)
    }

    fun updateAmount(value: String) {
        mutableState.value = mutableState.value.copy(amount = value)
    }

    fun updateDueDate(value: String) {
        mutableState.value = mutableState.value.copy(dueDate = value)
    }

    fun updateNotes(value: String) {
        mutableState.value = mutableState.value.copy(notes = value)
    }

    fun saveInvoice() {
        val state = mutableState.value
        val dueAt = parseDate(state.dueDate)
        val amountPence = parseAmount(state.amount)
        val errors = mutableListOf<String>()

        if (dueAt == null) {
            errors.add("Use a valid due date.")
        }

        val draft = InvoiceDraft(
            clientId = clientId,
            description = state.title,
            amountPence = amountPence,
            dueAt = dueAt ?: 0L,
            notes = state.notes,
        )

        errors.addAll(BusinessRules.validateInvoiceDraft(draft))

        if (errors.isNotEmpty()) {
            mutableState.value = state.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            mutableState.value = state.copy(
                errors = emptyList(),
                isSaving = true,
            )
            val saved = repository.createInvoice(draft)
            mutableState.value = mutableState.value.copy(
                isSaving = false,
                isSaved = saved,
                errors = if (saved) emptyList() else listOf("The invoice could not be saved."),
            )
        }
    }

    private fun parseDate(value: String): Long? {
        return try {
            LocalDate.parse(value.trim())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAmount(value: String): Int {
        val amount = value.trim().toDoubleOrNull() ?: 0.0
        return (amount * 100).toInt()
    }

    companion object {
        fun factory(
            repository: TrainerHubRepository,
            clientId: String,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    InvoiceEditorViewModel(repository, clientId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditorScreen(
    repository: TrainerHubRepository,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    clientId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: InvoiceEditorViewModel = viewModel(
        factory = InvoiceEditorViewModel.factory(repository, clientId),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Invoice saved.")
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
                title = { Text("Create invoice") },
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
                    onClick = viewModel::saveInvoice,
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
                        Text("Save invoice")
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
                text = "Use due date format YYYY-MM-DD and enter amount in pounds, for example 180.00.",
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
            )
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::updateAmount,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                value = uiState.dueDate,
                onValueChange = viewModel::updateDueDate,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Due date") },
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
