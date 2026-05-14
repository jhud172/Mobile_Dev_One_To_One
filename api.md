# Android API And Backend Integration

## Summary

The One To One Android app uses Android platform APIs, Jetpack libraries, and a dedicated Spring Boot mobile API to provide a role-aware mobile experience for public users, clients, trainers, and gym accounts.

The Android app talks to the hosted website through HTTPS. It does not connect directly to PostgreSQL and does not reuse browser-only Thymeleaf form flows.

## Android APIs And Libraries

### Jetpack Compose

Jetpack Compose is used for the full user interface:

- Public welcome and explore screens.
- Login and signup forms.
- Role-aware bottom navigation.
- Home, calendar, day plan, training log, chat, role list, and account screens.
- Reusable premium UI components such as cards, buttons, status chips, section titles, and empty/error states.

Compose state is used to show loading, validation, error, empty, demo, and authenticated states.

### Material 3

Material 3 provides the base design system. The app customises colours, spacing, button behaviour, cards, and navigation to match the premium One To One direction while still following Android interaction expectations.

### Kotlin Coroutines And StateFlow

Coroutines are used for network calls, session restore, login, signup, logout, chat, calendar loading, day completion, and training log submission.

StateFlow is used for authentication state so the UI can automatically switch between public, loading, demo, and role-specific authenticated shells.

### Android Keystore

Android Keystore protects remembered login state. The app stores only an opaque bearer token encrypted with AES/GCM. The app does not store passwords, database credentials, payment secrets, or AI provider keys.

### Room

Room remains in the project as a local data layer for demo and fallback behaviour. This keeps the app demonstrable during assessment even if the hosted backend is unavailable.

### WorkManager

WorkManager remains available for reminder scheduling. It supports local reminder behaviour without needing the app to stay open.

## Mobile API Base URL

The app is configured to use the hosted Render web service:

```text
https://two025-group14-c24071109-1.onrender.com
```

The server URL is no longer exposed as a login page input. Existing saved local development URLs such as `localhost`, `127.0.0.1`, and `10.0.2.2` are ignored so the app uses the configured hosted URL by default.

## Spring Boot Mobile API

The website exposes a dedicated mobile API under:

```text
/api/mobile/**
```

These endpoints reuse existing website users, roles, password hashing, validation, and database tables. They are JSON endpoints for Android rather than HTML pages for browsers.

### Authentication Endpoints

```text
POST /api/mobile/auth/login
POST /api/mobile/auth/logout
POST /api/mobile/auth/signup/client
POST /api/mobile/auth/signup/trainer
POST /api/mobile/auth/signup/gym
GET  /api/mobile/me
```

Authentication returns an opaque mobile token. Server-side tokens are stored as hashes with expiry and revocation support.

### Core App Endpoints

```text
GET  /api/mobile/home
GET  /api/mobile/calendar/month?month=YYYY-MM
GET  /api/mobile/calendar/day?date=YYYY-MM-DD
POST /api/mobile/day/tasks/{id}/complete
GET  /api/mobile/training
POST /api/mobile/training/logs
GET  /api/mobile/chat/history
POST /api/mobile/chat/message
GET  /api/mobile/notifications
POST /api/mobile/notifications/{id}/read
GET  /api/mobile/profile
```

### Client Endpoints

```text
GET /api/mobile/client/trainer
GET /api/mobile/client/plan
GET /api/mobile/client/logs
```

### Trainer Endpoints

```text
GET  /api/mobile/trainer/clients
GET  /api/mobile/trainer/clients/{id}
POST /api/mobile/trainer/clients/{id}/sessions
POST /api/mobile/trainer/clients/{id}/plans
```

Trainer session and plan actions are protected by backend rules:

- The trainer must be verified.
- The client must have an active trainer-client relationship with that trainer.
- Assigned sessions and plans write to the website database.

### Gym Endpoints

```text
GET  /api/mobile/gym/trainers
GET  /api/mobile/gym/requests
POST /api/mobile/gym/requests/{id}/approve
```

Gym request review actions are role-restricted and preserve server-side reviewer metadata.

## Error Handling

The app handles:

- Local validation errors before submission.
- Loading states while requests are running.
- Server error messages from failed API responses.
- Empty lists when no records exist.
- Demo mode when the live service cannot be used.
- Logged-out state when no valid session exists.

## Security Requirements

The Android app must never contain:

- PostgreSQL internal or external database URLs.
- Database usernames or passwords.
- Stripe secret keys.
- OpenAI keys.
- Render private environment values.
- Any server-only secret.

Android only needs the HTTPS web API base URL. Database access remains behind the Spring Boot backend.

## Current Deployment Status

The live Render website is reachable. For production Android integration, the deployed website must include the mobile API package and expose the `/api/mobile/**` endpoints. If those endpoints are not deployed, the Android app can open but real API login and data screens will return server errors until the backend deployment is updated.
