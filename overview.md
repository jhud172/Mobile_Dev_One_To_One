# One To One Android Overview

## Purpose

The One To One Android app is a native mobile companion for the One To One premium coaching platform. It extends the website into a focused Android experience for clients, trainers, and gyms while keeping the Spring Boot website and PostgreSQL database as the source of truth.

The product is based on a clear coaching rule: a client trains through one active trainer relationship at a time. The mobile app supports that relationship rather than becoming a generic fitness tracker.

## User Groups

### Public Users

Public users can:

- View a premium welcome screen.
- Explore the One To One concept.
- Log in to an existing account.
- Create a client, trainer, or gym account.
- Continue in demo mode if the live server is unavailable.

### Clients

Client users can:

- View a personalised home screen.
- Open calendar days and day plans.
- Complete assigned day items.
- Record training logs.
- Use coach chat.
- Log out securely.

### Trainers

Trainer users can:

- View trainer home information.
- Review linked client records.
- Open calendar and chat tools.
- Access account actions.
- Use trainer-only actions through the backend where verification and active client links allow it.

### Gym Accounts

Gym users can:

- View gym home information.
- Review trainer records.
- Review request information.
- Use calendar and account tools.

## Design Direction

The app takes visual direction from the One To One website but is not a web page squeezed into Android. It uses:

- Jetpack Compose and Material 3.
- Premium cards, clean spacing, and clear hierarchy.
- Website-inspired green, neutral, and gold tones.
- The One To One logo from the website repository.
- Role-aware bottom navigation.
- Consistent button sizing, loading states, disabled states, and validation errors.

## Architecture

The Android app uses:

- Kotlin and Jetpack Compose for the UI.
- StateFlow and coroutines for asynchronous state.
- Android Keystore for encrypted remembered login tokens.
- Room for demo and local fallback data.
- WorkManager for reminder support.
- A remote API package for DTOs, network calls, mobile auth state, and session storage.

The website provides a dedicated `/api/mobile/**` JSON API. Android does not scrape Thymeleaf pages and does not connect directly to PostgreSQL.

## Hosted Integration

The Android app is configured to use the live Render web service:

```text
https://two025-group14-c24071109-1.onrender.com
```

The Android app only stores and calls this HTTPS web API base URL. It must never contain PostgreSQL URLs, database passwords, Stripe secrets, OpenAI keys, or other server-only environment values.

At the time of writing, the Render website is reachable but the live deployment must include the new mobile API endpoints for the Android login/signup/API flows to work against production data.

## Assessment Reliability

Demo mode remains available so the app can be marked even if the hosted service is unavailable. This provides a controlled fallback while still demonstrating the real intended architecture: Android client, Spring Boot mobile API, and shared One To One database.
