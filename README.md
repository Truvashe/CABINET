# CabinetMedicalApp

Local-only Android app for managing medical appointments (SQLite).

## Quick run
- Open the project in Android Studio.
- Build and run on an emulator or device.
- The launcher opens a **Connexion** screen.

## Seed accounts (login credentials):
- Admin: username `admin` / password `admin123` (role: admin)
- Doctor: username `doctor` / password `doc123` (role: doctor)
- Patient: username `patient` / password `patient123` (role: patient)

## Current features implemented
- Local SQLite schema with `users`, `appointments`, and `timeslots` tables.
- Login and inline registration (creates a patient account).
- Patient flow: create new appointment (date/time/reason) and view history with cancel option.
- Doctor flow: view assigned appointments, mark as done, cancel, or reschedule.
- Admin features: Manage users and doctors' timeslots (placeholders).
- Local appointment reminders with configurable lead time (10, 30, or 60 minutes).
- Settings screen to configure reminder preferences.

## Next steps (planned)
- Implement doctor availability (timeslots) and appointment booking constraints.
- Improve admin user management UI.
- Add comprehensive tests.
- Improve UI/UX (better styling, localization).

If you want, I can continue implementing doctor/admin features and reminders now.