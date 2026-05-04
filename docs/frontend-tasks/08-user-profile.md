# Task 08: User Profile & Security

## Overview
Implement the UI for users to manage their personal information and account security.

## UI Requirements
- **Profile Page**: View and edit user details (FullName, PhoneNumber, Address).
- **Security Settings**: Change password form.
- **Account Summary**: Display user role, registration date, and active permissions.

## Backend Integration
- **Service**: `auth-service`
- **Endpoints**:
  - `GET /api/auth/me`: Get current user info.
  - `PUT /api/auth/me`: Update profile info.
  - `PUT /api/auth/change-password`: Change account password.

## Technical Tasks
1. Implement `updateProfile` and `changePassword` in `features/auth/services/auth.service.ts`.
2. Create `ProfileForm` and `ChangePasswordForm` components.
3. Register routes in `app/(app)/settings/profile`.
4. Ensure validation for password matching and complexity.
