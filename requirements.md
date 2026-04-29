# Trainer Hub Requirements

## Functional Requirements

### Dashboard
- The app shall show a trainer dashboard with active client count, sessions scheduled for today, overdue invoice count, and high-priority alerts.
- The app shall provide quick actions for adding a client, creating a session, and creating an invoice.

### Trainer Profile
- The app shall display the current trainer as verified.
- The app shall show the trainer’s gym affiliations and support an independent-trainer state.

### Client Management
- The app shall list all active clients assigned to the trainer.
- The app shall support searching and sorting clients.
- The app shall allow a trainer to add a new client with validation for required fields.
- The app shall automatically assign a newly created client to the current trainer.
- The app shall prevent a client from having more than one active trainer assignment at a time.
- The app shall preserve historical trainer assignments when reassignment occurs.

### Client Detail
- The app shall provide a client detail screen with overview, plans, sessions, payments, notes, and privacy tabs.
- The app shall show client status, goals, contact information, and assigned-trainer state.

### Training Plans
- The app shall allow a trainer to create a multi-week training plan for a client.
- The app shall allow each plan week to contain a summary and a list of exercises.
- The app shall allow one active plan per client while preserving historic plans.
- The app shall show a guided empty state when a client has no active plan.

### Sessions
- The app shall allow a trainer to schedule a session with date, duration, location, and session type.
- The app shall support marking sessions as scheduled, completed, or cancelled.
- The app shall allow post-session notes to be recorded and retained.

### Payments
- The app shall allow a trainer to create invoices for a client.
- The app shall show invoice status as draft, due, overdue, or paid.
- The app shall allow payment to be recorded inside the platform flow.
- The app shall retain a payment history linked to invoices.

### Notes and Privacy
- The app shall store client notes and session notes.
- The app shall store consent records for each client.
- The app shall support demo export-request and delete-request flows for GDPR readiness.

### Settings and Reliability
- The app shall support resetting demo data to a known good seeded state.
- The app shall store app preferences locally, including reminder preference and client sort preference.
- The app shall schedule local reminders for upcoming sessions and overdue invoices when reminders are enabled.

## Non-Functional Requirements

### Usability
- The app shall present a premium, calm, and professional interface suitable for a paid coaching service.
- The app shall use a consistent information hierarchy and reusable components across screens.
- The app shall not require login before the marker can access core features.

### Reliability
- The app shall work offline using locally persisted seeded data.
- The app shall launch successfully on a clean install.
- The app shall restore data correctly after the app is closed and reopened.
- The app shall handle empty states without broken navigation or dead screens.

### Maintainability
- The app shall separate data, domain, and UI concerns.
- The app shall use repository abstractions so local data can later be replaced with a backend implementation.
- The app shall avoid deprecated Android APIs.

### Privacy and Compliance
- The app shall make privacy-related actions visible within the client workflow.
- The app shall model consent and data-request events in an auditable way.
- The app shall avoid unnecessary personal-data collection.

### Performance and Scope
- The app shall prioritise smooth navigation and responsive list/detail flows over feature breadth.
- The app shall avoid non-essential social, nutrition, or wearable features that do not support one-to-one coaching.

