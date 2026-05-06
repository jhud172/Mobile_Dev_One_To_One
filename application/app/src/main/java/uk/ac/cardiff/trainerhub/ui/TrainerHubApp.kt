package uk.ac.cardiff.trainerhub.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.util.Patterns
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uk.ac.cardiff.trainerhub.R
import uk.ac.cardiff.trainerhub.data.remote.AuthState
import uk.ac.cardiff.trainerhub.data.remote.CalendarDay
import uk.ac.cardiff.trainerhub.data.remote.ChatMessage
import uk.ac.cardiff.trainerhub.data.remote.DayItem
import uk.ac.cardiff.trainerhub.data.remote.HomeSummary
import uk.ac.cardiff.trainerhub.data.remote.MobileRole
import uk.ac.cardiff.trainerhub.data.remote.MobileUser
import uk.ac.cardiff.trainerhub.data.remote.OneToOneMobileRepository
import uk.ac.cardiff.trainerhub.data.remote.RoleItem
import uk.ac.cardiff.trainerhub.data.remote.TrainingLog
import uk.ac.cardiff.trainerhub.data.reminders.ReminderScheduler
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository
import uk.ac.cardiff.trainerhub.ui.components.AppBackground
import uk.ac.cardiff.trainerhub.ui.components.EmptyStateCard
import uk.ac.cardiff.trainerhub.ui.components.PremiumButton
import uk.ac.cardiff.trainerhub.ui.components.PremiumCard
import uk.ac.cardiff.trainerhub.ui.components.SectionTitle
import uk.ac.cardiff.trainerhub.ui.components.StatCard
import uk.ac.cardiff.trainerhub.ui.components.StatusChip
import java.time.LocalDate
import java.time.YearMonth

private data class NavItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun TrainerHubApp(
    repository: TrainerHubRepository,
    mobileRepository: OneToOneMobileRepository,
    reminderScheduler: ReminderScheduler,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val authState by mobileRepository.authState.collectAsStateWithLifecycle()
    val remindersEnabled by repository.remindersEnabled.collectAsStateWithLifecycle(initialValue = true)

    LaunchedEffect(Unit) {
        repository.ensureSeeded()
        mobileRepository.restoreSession()
    }

    LaunchedEffect(remindersEnabled) {
        reminderScheduler.update(remindersEnabled)
    }

    AppBackground {
        when {
            authState.loading -> LoadingScreen()
            authState.user == null -> PublicShell(mobileRepository, authState, snackbarHostState)
            else -> SignedInShell(mobileRepository, authState, snackbarHostState)
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublicShell(
    mobileRepository: OneToOneMobileRepository,
    authState: AuthState,
    snackbarHostState: SnackbarHostState,
) {
    var route by remember { mutableStateOf("welcome") }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { BrandTitle() },
            )
        },
        bottomBar = {
            NavigationBar {
                listOf(
                    NavItem("welcome", "Home", Icons.Outlined.Home),
                    NavItem("explore", "Explore", Icons.Outlined.FitnessCenter),
                    NavItem("login", "Login", Icons.AutoMirrored.Outlined.Login),
                    NavItem("signup", "Sign up", Icons.Outlined.PersonAdd),
                ).forEach { item ->
                    NavigationBarItem(
                        selected = route == item.key,
                        onClick = { route = item.key },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        when (route) {
            "explore" -> ExploreScreen(padding)
            "login" -> LoginScreen(mobileRepository, authState, padding)
            "signup" -> SignupScreen(mobileRepository, authState, padding)
            else -> WelcomeScreen(mobileRepository, padding, onLogin = { route = "login" }, onSignup = { route = "signup" })
        }
    }
}

@Composable
private fun WelcomeScreen(
    mobileRepository: OneToOneMobileRepository,
    padding: PaddingValues,
    onLogin: () -> Unit,
    onSignup: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PremiumCard(tonal = true) {
                BrandMark()
                Text("Premium coaching, built around one relationship.", style = MaterialTheme.typography.headlineMedium)
                Text("Clients train with a verified coach. Trainers manage real clients. Gyms oversee their coaching team.")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PremiumButton(onClick = onLogin, modifier = Modifier.weight(1f)) { Text("Login") }
                PremiumButton(onClick = onSignup, modifier = Modifier.weight(1f)) { Text("Sign up") }
            }
        }
        item {
            PremiumCard {
                Text("Assessment-safe demo", fontWeight = FontWeight.SemiBold)
                Text("Use this if the live server is not reachable during marking.")
                TextButton(onClick = { scope.launch { mobileRepository.useDemoMode() } }) {
                    Text("Continue in demo mode")
                }
            }
        }
    }
}

@Composable
private fun ExploreScreen(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionTitle("Explore One To One", "A mobile companion for clients, trainers and gyms") }
        items(
            listOf(
                "Verified trainers and one active trainer-client relationship.",
                "Day view, calendar, training logs and coach chat.",
                "Role-aware tools for clients, trainers and gym accounts.",
                "Payments and sensitive account work stay inside the hosted platform.",
            ),
        ) { text ->
            PremiumCard { Text(text) }
        }
    }
}

@Composable
private fun LoginScreen(
    mobileRepository: OneToOneMobileRepository,
    authState: AuthState,
    padding: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val usernameError = if (submitted && username.isBlank()) "Username or email is required." else null
    val passwordError = if (submitted && password.isBlank()) "Password is required." else null
    val formErrors = listOfNotNull(usernameError, passwordError)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SectionTitle("Login", "Connect to your hosted One To One account") }
        item {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username or email") },
                singleLine = true,
                isError = usernameError != null,
                supportingText = { usernameError?.let { Text(it) } },
            )
        }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it) } },
            )
        }
        if (formErrors.isNotEmpty()) {
            item { ValidationErrorCard("Complete login", formErrors) }
        }
        item {
            PremiumButton(
                onClick = {
                    submitted = true
                    if (username.isNotBlank() && password.isNotBlank()) {
                        isSubmitting = true
                        scope.launch {
                            mobileRepository.login(username.trim(), password)
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            ) {
                Text("Login")
            }
        }
        authState.error?.let { item { EmptyStateCard("Login issue", it) } }
    }
}

@Composable
private fun SignupScreen(
    mobileRepository: OneToOneMobileRepository,
    authState: AuthState,
    padding: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf(MobileRole.CLIENT) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val firstNameError = if (submitted && firstName.isBlank()) "First name is required." else null
    val lastNameError = if (submitted && lastName.isBlank()) "Last name is required." else null
    val emailError = when {
        !submitted -> null
        email.isBlank() -> "Email is required."
        !isValidEmail(email.trim()) -> "Use a valid email address."
        else -> null
    }
    val usernameError = when {
        !submitted -> null
        username.isBlank() -> "Username is required."
        username.trim().length < 3 -> "Username must be at least 3 characters."
        else -> null
    }
    val passwordError = when {
        !submitted -> null
        password.isBlank() -> "Password is required."
        password.length < 8 -> "Password must be at least 8 characters."
        else -> null
    }
    val formErrors = listOfNotNull(firstNameError, lastNameError, emailError, usernameError, passwordError)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SectionTitle("Create account", "Choose the role that should drive the app experience") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                RoleButton("Client", role == MobileRole.CLIENT, Modifier.weight(1f)) { role = MobileRole.CLIENT }
                RoleButton("Trainer", role == MobileRole.TRAINER, Modifier.weight(1f)) { role = MobileRole.TRAINER }
                RoleButton("Gym", role == MobileRole.GYM_ADMIN, Modifier.weight(1f)) { role = MobileRole.GYM_ADMIN }
            }
        }
        item {
            OutlinedTextField(
                firstName,
                { firstName = it },
                Modifier.fillMaxWidth(),
                label = { Text("First name") },
                singleLine = true,
                isError = firstNameError != null,
                supportingText = { firstNameError?.let { Text(it) } },
            )
        }
        item {
            OutlinedTextField(
                lastName,
                { lastName = it },
                Modifier.fillMaxWidth(),
                label = { Text("Last name") },
                singleLine = true,
                isError = lastNameError != null,
                supportingText = { lastNameError?.let { Text(it) } },
            )
        }
        item {
            OutlinedTextField(
                email,
                { email = it },
                Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it) } },
            )
        }
        item {
            OutlinedTextField(
                username,
                { username = it },
                Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
                isError = usernameError != null,
                supportingText = { usernameError?.let { Text(it) } },
            )
        }
        item {
            OutlinedTextField(
                password,
                { password = it },
                Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it) } },
            )
        }
        if (formErrors.isNotEmpty()) {
            item { ValidationErrorCard("Complete signup", formErrors) }
        }
        item {
            PremiumButton(
                onClick = {
                    submitted = true
                    if (firstName.isNotBlank() &&
                        lastName.isNotBlank() &&
                        isValidEmail(email.trim()) &&
                        username.trim().length >= 3 &&
                        password.length >= 8
                    ) {
                        isSubmitting = true
                        scope.launch {
                            mobileRepository.signup(
                                role = role,
                                email = email.trim(),
                                username = username.trim(),
                                password = password,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                            )
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            ) {
                Text("Create ${roleLabel(role)} account")
            }
        }
        authState.error?.let { item { EmptyStateCard("Signup issue", it) } }
    }
}

@Composable
private fun RoleButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    PremiumCard(
        modifier = modifier.clickable(onClick = onClick),
        tonal = selected,
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignedInShell(
    mobileRepository: OneToOneMobileRepository,
    authState: AuthState,
    snackbarHostState: SnackbarHostState,
) {
    val user = authState.user ?: return
    val destinations = destinationsFor(user.role)
    var route by remember(user.role) { mutableStateOf(destinations.first().key) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        BrandMark(modifier = Modifier.size(34.dp))
                        Column {
                        Text("One To One")
                        Text(user.fullName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { item ->
                    NavigationBarItem(
                        selected = route == item.key,
                        onClick = { route = item.key },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        when (route) {
            "day" -> DayScreen(mobileRepository, selectedDay, padding, authState.demoMode)
            "calendar" -> CalendarScreen(
                repository = mobileRepository,
                padding = padding,
                demoMode = authState.demoMode,
                onOpenDay = { day ->
                    selectedDay = day
                    route = "day"
                },
            )
            "train" -> TrainingScreen(mobileRepository, padding, authState.demoMode)
            "chat" -> ChatScreen(mobileRepository, padding, authState.demoMode)
            "clients", "trainers", "requests" -> RoleListScreen(mobileRepository, user, route, padding, authState.demoMode)
            "more" -> MoreScreen(mobileRepository, user, padding)
            else -> HomeScreen(
                repository = mobileRepository,
                user = user,
                padding = padding,
                demoMode = authState.demoMode,
                onNavigate = { target ->
                    if (target == "day") {
                        selectedDay = LocalDate.now()
                    }
                    route = target
                },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    repository: OneToOneMobileRepository,
    user: MobileUser,
    padding: PaddingValues,
    demoMode: Boolean,
    onNavigate: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var home by remember { mutableStateOf<HomeSummary?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(user, demoMode) {
        if (demoMode) {
            home = HomeSummary("Demo mode: local assessment-safe preview.", 3, 1, listOf("Open day view", "Log training", "Ask coach"), emptyList())
        } else {
            try {
                home = repository.home()
            } catch (exception: Exception) {
                error = exception.message
            }
        }
    }
    ContentState(home, error, padding) { data ->
        item {
            PremiumCard(tonal = true) {
                BrandMark()
                Text(roleLabel(user.role) + " home", style = MaterialTheme.typography.headlineMedium)
                Text(data.headline)
                if (user.role == MobileRole.TRAINER) StatusChip(if (user.trainerVerified) "VERIFIED" else "PENDING")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Today", data.todayCount.toString(), "Items planned", Modifier.weight(1f))
                StatCard("Done", data.todayCompleted.toString(), "Completed", Modifier.weight(1f))
            }
        }
        items(data.actions) { action ->
            ActionCard(action = action, onClick = { onNavigate(routeForAction(action, user.role)) })
        }
        if (user.role == MobileRole.CLIENT) {
            item {
                PremiumButton(onClick = { scope.launch { repository.logout() } }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun DayScreen(repository: OneToOneMobileRepository, date: LocalDate, padding: PaddingValues, demoMode: Boolean) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<DayItem>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    fun load() {
        scope.launch {
            if (demoMode) {
                items = listOf(DayItem("demo", "Strength session", "Demo lower body work", "TASK", false))
            } else {
                try {
                    items = repository.day(date)
                } catch (exception: Exception) {
                    error = exception.message
                }
            }
        }
    }
    LaunchedEffect(demoMode, date) { load() }
    ContentState(items, error, padding) { data ->
        item { SectionTitle(if (date == LocalDate.now()) "Today" else date.toString(), "Complete the work assigned for this day") }
        if (data.isEmpty()) item { EmptyStateCard("Clear day", "No tasks or sessions are scheduled.") }
        items(data) { item ->
            PremiumCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.title, fontWeight = FontWeight.SemiBold)
                        Text(item.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusChip(if (item.completed) "COMPLETED" else item.type)
                }
                if (!item.completed && !demoMode) {
                    TextButton(onClick = { scope.launch { repository.complete(item.id); load() } }) {
                        Text("Mark complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarScreen(
    repository: OneToOneMobileRepository,
    padding: PaddingValues,
    demoMode: Boolean,
    onOpenDay: (LocalDate) -> Unit,
) {
    var days by remember { mutableStateOf<List<CalendarDay>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(demoMode) {
        if (demoMode) {
            days = (1..7).map { CalendarDay(LocalDate.now().plusDays(it.toLong() - 1).toString(), it % 3, it % 2) }
        } else {
            try {
                days = repository.month(YearMonth.now())
            } catch (exception: Exception) {
                error = exception.message
            }
        }
    }
    ContentState(days, error, padding) { data ->
        item { SectionTitle("Calendar", "Tap any active day to open its day plan") }
        item {
            PremiumButton(onClick = { onOpenDay(LocalDate.now()) }, modifier = Modifier.fillMaxWidth()) {
                Text("View today's plan")
            }
        }
        val activeDays = data.filter { it.total > 0 }
        if (activeDays.isEmpty()) {
            item { EmptyStateCard("No active days", "No tasks are scheduled this month.") }
        } else {
            items(activeDays) { day ->
                PremiumCard(
                    modifier = Modifier.clickable { onOpenDay(LocalDate.parse(day.date)) },
                    tonal = day.date == LocalDate.now().toString(),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(day.date, fontWeight = FontWeight.SemiBold)
                        Text("${day.completed}/${day.total} complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingScreen(repository: OneToOneMobileRepository, padding: PaddingValues, demoMode: Boolean) {
    val scope = rememberCoroutineScope()
    var logs by remember { mutableStateOf<List<TrainingLog>?>(null) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    val notesError = if (submitted && notes.isBlank()) "Add training notes before saving." else null
    fun load() {
        scope.launch {
            if (demoMode) {
                logs = listOf(TrainingLog("demo", LocalDate.now().toString(), "Demo training log", 45))
            } else {
                try {
                    logs = repository.training()
                } catch (exception: Exception) {
                    error = exception.message
                }
            }
        }
    }
    LaunchedEffect(demoMode) { load() }
    ContentState(logs, error, padding) { data ->
        item { SectionTitle("Training logs", "Write progress back to the One To One database") }
        item {
            PremiumCard(tonal = true) {
                OutlinedTextField(
                    notes,
                    { notes = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Training notes") },
                    isError = notesError != null,
                    supportingText = { notesError?.let { Text(it) } },
                )
                notesError?.let { ValidationErrorCard("Complete log", listOf(it)) }
                PremiumButton(
                    onClick = {
                        submitted = true
                        if (notes.isNotBlank()) {
                            scope.launch {
                                if (!demoMode) repository.addTrainingLog(notes.trim(), 45)
                                notes = ""
                                submitted = false
                                load()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = notesError == null,
                ) { Text("Save log") }
            }
        }
        items(data) { log ->
            PremiumCard {
                Text(log.date, fontWeight = FontWeight.SemiBold)
                Text(log.comments.ifBlank { "Training logged" })
                Text("${log.durationMinutes} minutes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ChatScreen(repository: OneToOneMobileRepository, padding: PaddingValues, demoMode: Boolean) {
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<ChatMessage>?>(null) }
    var draft by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    val draftError = if (submitted && draft.isBlank()) "Write a message before sending." else null
    fun load() {
        scope.launch {
            if (demoMode) {
                messages = listOf(ChatMessage("ASSISTANT", "Demo coach ready."))
            } else {
                try {
                    messages = repository.chatHistory()
                } catch (exception: Exception) {
                    error = exception.message
                }
            }
        }
    }
    LaunchedEffect(demoMode) { load() }
    ContentState(messages, error, padding) { data ->
        item { SectionTitle("Coach chat", "Ask about today's training, calendar and next actions") }
        items(data) { msg ->
            PremiumCard(tonal = msg.role.equals("ASSISTANT", true)) {
                Text(if (msg.role.equals("USER", true)) "You" else "Coach", fontWeight = FontWeight.SemiBold)
                Text(msg.content)
            }
        }
        item {
            PremiumCard {
                OutlinedTextField(
                    draft,
                    { draft = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Message") },
                    isError = draftError != null,
                    supportingText = { draftError?.let { Text(it) } },
                )
                draftError?.let { ValidationErrorCard("Complete message", listOf(it)) }
                PremiumButton(
                    onClick = {
                        submitted = true
                        scope.launch {
                            if (draft.isNotBlank()) {
                                if (!demoMode) repository.sendChat(draft.trim())
                                draft = ""
                                submitted = false
                                load()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = draftError == null,
                ) { Text("Send") }
            }
        }
    }
}

@Composable
private fun RoleListScreen(repository: OneToOneMobileRepository, user: MobileUser, route: String, padding: PaddingValues, demoMode: Boolean) {
    var items by remember { mutableStateOf<List<RoleItem>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(user, route, demoMode) {
        if (demoMode) {
            items = listOf(RoleItem("demo", "Demo relationship", "Assessment preview", "ACTIVE"))
        } else {
            try {
                items = repository.roleItems()
            } catch (exception: Exception) {
                error = exception.message
            }
        }
    }
    ContentState(items, error, padding) { data ->
        item { SectionTitle(route.replaceFirstChar { it.uppercase() }, "Role-specific One To One records") }
        if (data.isEmpty()) item { EmptyStateCard("Nothing here yet", "Records will appear once the website database has linked data.") }
        items(data) { row ->
            PremiumCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(row.title, fontWeight = FontWeight.SemiBold)
                        Text(row.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusChip(row.status.ifBlank { "ACTIVE" })
                }
            }
        }
    }
}

@Composable
private fun MoreScreen(repository: OneToOneMobileRepository, user: MobileUser, padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PremiumCard(tonal = true) {
                Text(user.fullName, style = MaterialTheme.typography.titleLarge)
                Text("${roleLabel(user.role)} account")
                Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            PremiumCard {
                Text("Privacy", fontWeight = FontWeight.SemiBold)
                Text("The app stores the mobile session token encrypted and clears it on logout.")
            }
        }
        item {
            PremiumButton(onClick = { scope.launch { repository.logout() } }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                Text("Logout")
            }
        }
    }
}

@Composable
private fun <T> ContentState(
    data: T?,
    error: String?,
    padding: PaddingValues,
    content: LazyListScope.(T) -> Unit,
) {
    if (data == null && error == null) {
        LoadingScreen()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (error != null) {
            item { EmptyStateCard("Server issue", error) }
        } else if (data != null) {
            content(data)
        }
    }
}

private fun screenPadding(padding: PaddingValues): PaddingValues =
    PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = padding.calculateTopPadding() + 16.dp,
        bottom = padding.calculateBottomPadding() + 24.dp,
    )

private fun destinationsFor(role: MobileRole): List<NavItem> = when (role) {
    MobileRole.TRAINER -> listOf(
        NavItem("home", "Home", Icons.Outlined.Home),
        NavItem("clients", "Clients", Icons.Outlined.People),
        NavItem("calendar", "Calendar", Icons.Outlined.CalendarMonth),
        NavItem("chat", "Chat", Icons.AutoMirrored.Outlined.Chat),
        NavItem("more", "More", Icons.Outlined.Settings),
    )
    MobileRole.GYM_ADMIN -> listOf(
        NavItem("home", "Home", Icons.Outlined.Home),
        NavItem("trainers", "Trainers", Icons.Outlined.People),
        NavItem("requests", "Requests", Icons.AutoMirrored.Outlined.ListAlt),
        NavItem("calendar", "Calendar", Icons.Outlined.CalendarMonth),
        NavItem("more", "More", Icons.Outlined.Settings),
    )
    else -> listOf(
        NavItem("home", "Home", Icons.Outlined.Home),
        NavItem("calendar", "Calendar", Icons.Outlined.CalendarMonth),
        NavItem("train", "Train", Icons.Outlined.FitnessCenter),
        NavItem("chat", "Chat", Icons.AutoMirrored.Outlined.Chat),
    )
}

private fun roleLabel(role: MobileRole): String = when (role) {
    MobileRole.TRAINER -> "Trainer"
    MobileRole.GYM_ADMIN -> "Gym"
    MobileRole.CLIENT -> "Client"
    MobileRole.UNKNOWN -> "One To One"
}

@Composable
private fun BrandTitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BrandMark(modifier = Modifier.size(34.dp))
        Text("One To One")
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier.height(48.dp)) {
    Image(
        painter = painterResource(id = R.drawable.one_to_one_logo),
        contentDescription = "One To One logo",
        modifier = modifier,
    )
}

@Composable
private fun ActionCard(action: String, onClick: () -> Unit) {
    PremiumCard(
        modifier = Modifier.clickable(onClick = onClick),
        tonal = false,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(action, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Text("Open", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun routeForAction(action: String, role: MobileRole): String {
    val normalised = action.lowercase()
    return when {
        "day" in normalised -> "day"
        "plan" in normalised -> "calendar"
        "log" in normalised || "train" in normalised -> "train"
        "coach" in normalised || "message" in normalised || "chat" in normalised -> "chat"
        "calendar" in normalised -> "calendar"
        "client" in normalised -> "clients"
        "request" in normalised -> "requests"
        "trainer" in normalised && role == MobileRole.GYM_ADMIN -> "trainers"
        else -> "home"
    }
}

@Composable
private fun ValidationErrorCard(title: String, errors: List<String>) {
    PremiumCard(tonal = true) {
        Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
        errors.distinct().forEach { error ->
            Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
