# Retrospective

## Project Outcome

The project began as a simpler Android trainer demo and has developed into a One To One mobile companion with public, client, trainer, and gym account flows. The final direction is stronger because it connects the app to the real product model instead of presenting a standalone offline trainer tool.

The app now better reflects the One To One platform: premium presentation, verified trainer logic, role-aware navigation, secure session handling, mobile API integration, and a focused feature set around coaching relationships.

## What Worked Well

### Product Focus

The app now avoids becoming a broad fitness tracker. Calendar, day plans, logs, chat, trainer clients, and gym requests all relate directly to client training or trainer/gym management.

### Role-Aware Structure

Separating public, client, trainer, and gym experiences makes the app easier to understand and closer to the real platform. Each role receives navigation that matches its responsibilities.

### Mobile-First UI

The redesign improved the app from a basic demo into a more premium Compose interface. The use of consistent cards, buttons, bottom navigation, status chips, validation messages, and the One To One logo gives the app a clearer identity.

### Secure Session Design

Remembered login now uses an opaque token encrypted with Android Keystore. This is a stronger approach than storing credentials and keeps secrets out of the Android codebase.

### Assessment Reliability

Demo mode remains valuable. It allows the app to be demonstrated even if the hosted backend is unavailable or has not yet been redeployed with the mobile API endpoints.

## Trade-Offs

### Backend Deployment Dependency

The Android app is configured to use the live Render web service, but full production functionality depends on the website being deployed with the `/api/mobile/**` endpoints. Until that deployment is live, real API calls may fail even though the Android app builds and opens correctly.

### Scope Control

Some advanced workflows are intentionally limited. Payments remain on the website, and the app avoids handling card details or deep payment management. This protects security and keeps the app aligned with the platform rules.

### Demo And Real Data

Keeping demo mode means the app has two operating paths: local demonstration and live API operation. This adds complexity, but it improves assessment reliability and makes the project safer to present.

## Improvements Made During Implementation

- Replaced the old local trainer demo shell with a role-aware One To One shell.
- Added public login, signup, explore, and demo flows.
- Added role-specific navigation for clients, trainers, and gyms.
- Removed the day tab from the client bottom navigation and made calendar days open day details.
- Configured the app to use the hosted Render web service instead of emulator localhost.
- Removed the server URL field from the login screen.
- Added validation and visible error UI to login, signup, training logs, and chat.
- Added One To One logo usage inside the Android app.
- Improved button, card, status, and colour styling.
- Added secure token storage using Android Keystore.
- Added documentation describing the API, requirements, overview, and assessment fit.

## What Could Be Improved Further

- Redeploy the Spring Boot website to Render with the mobile API endpoints included.
- Create known client, trainer, and gym test accounts for final marking.
- Add automated UI tests for login validation, signup validation, role routing, calendar navigation, and demo mode.
- Add instrumented tests for secure session restore and logout clearing.
- Expand server-side test coverage for mobile role restrictions.
- Add richer empty states once real production data is available.

## Final Reflection

The strongest part of the project is now its alignment with the actual One To One platform. The app has a clear product purpose, uses Android APIs appropriately, protects sensitive information, and presents a credible mobile interface for the assignment.

The main limitation is deployment readiness rather than Android build readiness. The Android app builds and installs successfully, but the live backend must expose the mobile API endpoints for the hosted integration to be fully operational.
