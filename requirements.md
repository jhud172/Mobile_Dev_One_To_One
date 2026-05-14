# One To One Android Requirements

## Functional Requirements

### Public Access

- The app shall open to a premium public home screen when no user is logged in.
- The app shall provide an explore screen describing the One To One service.
- The app shall support login with username or email and password.
- The app shall support signup for client, trainer, and gym account roles.
- The app shall provide demo mode for assessment use when the hosted service is unavailable.
- The app shall hide server URL configuration from normal users and use the configured hosted HTTPS API.

### Authentication

- The app shall restore a remembered login session securely.
- The app shall route restored users to the correct role-specific interface.
- The app shall support logout.
- Logout shall clear the encrypted Android session token.
- Logout shall request server-side mobile session invalidation when the backend is reachable.
- The app shall never store user passwords after login or signup.

### Client Experience

- Client users shall see a role-specific home screen.
- Client users shall see calendar, training, and chat navigation.
- Calendar days with work assigned shall be tappable.
- Tapping a calendar day shall open the matching day plan.
- Day plan items shall show completion state.
- Completing a day item shall write the change through the mobile API.
- Training logs shall require notes before submission.
- Training logs shall be written through the mobile API.
- Chat messages shall require message text before submission.
- Chat messages shall be sent through the mobile API.

### Trainer Experience

- Trainer users shall see home, clients, calendar, chat, and account navigation.
- Trainer users shall see trainer verification state where relevant.
- Trainer-only backend actions shall require a verified trainer account.
- Trainer actions against a client shall require an active trainer-client relationship.
- Trainer-created sessions and plans shall be saved through backend APIs.

### Gym Experience

- Gym users shall see home, trainers, requests, calendar, and account navigation.
- Gym request review actions shall be role-restricted by the backend.
- Gym request approval shall preserve reviewer metadata on the server.

### Validation And Error Handling

- Required login fields shall show validation errors before any login request is made.
- Required signup fields shall show validation errors before any signup request is made.
- Signup email shall be validated locally before submission.
- Signup password shall require a minimum length before submission.
- Training logs and chat messages shall show local validation errors when empty.
- Loading, empty, server error, unauthorised, and demo states shall be visible and understandable.

## Non-Functional Requirements

### Product Fit

- The app should feel premium and consistent with the One To One platform.
- The app should support client training, trainer management, gym oversight, messaging, and account access only.
- The app should avoid generic fitness tracker feature bloat.
- The app should preserve the One To One rule that a client has only one active trainer at a time.
- Payments should remain server-managed.
- The Android app must not collect or process card details directly.

### Security And Privacy

- Android shall store only an opaque mobile session token.
- The mobile token shall be encrypted locally using Android Keystore.
- Server-side mobile tokens shall be hashed and revocable.
- Passwords, database URLs, database passwords, Stripe secrets, and OpenAI keys shall not be present in Android code.
- Chatbot requests shall send only necessary coaching context and must not expose payment details or private server configuration.
- Role-based access control shall be enforced by the backend.
- Client users shall not be able to access trainer or gym private data.
- Trainer users shall not be able to access unrelated clients.

### Platform And Build

- The app shall be implemented in Kotlin with Jetpack Compose.
- The app shall use Material 3 components and Android platform APIs.
- The app shall build with Gradle.
- The app shall install successfully on the Android emulator.
- The app shall remain usable on a clean emulator install through demo mode.

## Verification Requirements

The final verification sequence should include:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

Backend verification should include the Spring Boot build and boot run from the website repository:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

Production verification should confirm that the hosted Render deployment exposes the `/api/mobile/**` endpoints used by Android.
