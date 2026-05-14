# One To One Android Companion

Native Android client for the One To One premium fitness platform.

The application is built as a mobile companion to the existing Spring Boot One To One website. It gives public users, clients, trainers, and gym accounts a role-aware Android experience while keeping the website and database as the source of truth.

## Current Status

The Android app now includes:

- Public welcome, explore, login, signup, and demo mode.
- Role-aware navigation for client, trainer, and gym accounts.
- Hosted API configuration for the live Render web service.
- Secure remembered sessions using Android Keystore encryption.
- Calendar, day plan, training log, chat, profile, and role-specific list screens.
- Local validation and visible error states for required user input.
- Demo mode so the app can still be assessed if the hosted service is unavailable.

The Android app points at:

```text
https://two025-group14-c24071109-1.onrender.com
```

The hosted server must include the `/api/mobile/**` Spring Boot endpoints for real login, signup, calendar, training, and chat calls to work.

## Repositories

Android application:

```text
Mobile_Dev_One_To_One/application
```

One To One Spring Boot website:

```text
Crystal-Productions-OneToOne/One To One/One-To-One/Web_App
```

## Build Commands

From the Android `application` directory:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

The app has been verified with `assembleDebug` and `installDebug` on the configured Android emulator.

## Documentation

- `overview.md` explains the application purpose, users, architecture, and current implementation.
- `requirements.md` lists functional and non-functional requirements.
- `api.md` explains Android APIs, libraries, security, and Spring Boot mobile endpoints.
- `underview.md` summarises how the app uses One To One and how it relates to the assignment.
- `retrospective.md` reflects on the implementation decisions, strengths, limitations, and future work.
