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

Implemented doctor features: listing of appointments assigned to the logged-in doctor, actions to mark as done, cancel, or reschedule (date/time).

Implemented admin features (in progress):
- Manage users (add, change role, reset password, delete)
- Manage doctors' timeslots (add, toggle availability)

Next: add local reminders and tests.
## Current features implemented
- Local SQLite schema with `users`, `appointments`, and `timeslots` tables.
- Login and inline registration (creates a patient account).
- Patient flow: create new appointment (date/time/reason) and view history.
- Placeholders for Doctor and Admin activities.

## Next steps (planned)
- Implement doctor availability (timeslots) and appointment booking constraints.
- Implement appointment edit/cancel and admin user management.
- Add local notifications for reminders.
- Add tests and improve UI (RecyclerView, localization, better UX).

If you want, I can continue implementing doctor/admin features and reminders now.