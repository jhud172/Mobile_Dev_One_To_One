# Android API Description

Trainer Hub uses modern Android APIs that directly support the assessment goals of reliability, premium UX, and maintainable architecture. The UI is built with **Jetpack Compose**, which provides declarative screen construction and reusable components for the dashboard, client list, client detail tabs, and forms. Compose makes it easier to keep a consistent premium interface because shared cards, badges, empty states, and dialogs can be implemented once and reused across the app.

Navigation between screens uses **Navigation Compose**. This is appropriate because the application is organised as a set of connected flows: dashboard, clients, add-client, client detail, plan editing, and settings. Navigation Compose keeps these flows explicit and safe, while allowing arguments such as a selected client ID to be passed cleanly between screens.

Screen state is managed with **ViewModel** and **StateFlow**. ViewModel is used so data survives configuration changes such as rotation and so UI logic is not embedded inside composables. StateFlow provides observable reactive state for lists, dashboards, detail screens, and forms. This supports lifecycle-safe updates and keeps the UI responsive when repository data changes.

Persistent local storage is handled by **Room**. Room is suitable because the assessment requires the app to work reliably on a clean install and without relying on unstable external services. The app stores trainers, gyms, clients, assignments, plans, sessions, invoices, payments, notes, and consent records in a structured local database. Room also supports future backend readiness because the same repository contracts can later be implemented against a Spring Boot API while preserving the current domain model.

Lightweight preference storage uses **DataStore**. DataStore is used for settings such as reminder enablement, preferred client sort mode, and seeded-demo bootstrap flags. It is more modern and reliable than older SharedPreferences-based patterns and fits well with coroutine and Flow usage.

To support one controlled beyond-module enhancement, the app uses **WorkManager** for local reminders. WorkManager is suitable for deferred and periodic background work such as reminding the trainer about upcoming sessions or overdue invoices. This demonstrates deeper Android capability while remaining stable because it does not require a live backend. If notification permission is not granted, the reminder flow degrades safely rather than crashing.

The app also uses core **Material 3** Android components for visual consistency and accessibility. Together, Compose, Navigation, ViewModel, StateFlow, Room, DataStore, and WorkManager form a modern Android stack that is well suited to a professional trainer workflow and easy to justify in relation to the app’s actual behaviour.

