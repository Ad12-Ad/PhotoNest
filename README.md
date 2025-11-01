# PhotoNest

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-orange)](https://kotlinlang.org/) 
[![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-blue)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-auth-blueviolet)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-green)](#license)

## Launching on Google Play Store Soon!

PhotoNest will be available for download on playstore on **Novemeber 15, 2025**.

Star this repository to get notified when the app goes live! ⭐

## Overview

PhotoNest is a modern, feature-rich Android application designed for photography enthusiasts who want to share their visual stories with the world. Built with cutting-edge technologies like **Kotlin** and **Jetpack Compose**, PhotoNest emphasizes a smooth user experience, modern UI design, and clean architecture principles, making it a robust and scalable social media platform for photo sharing.

## Features

- **Engaging Splash Screen:** Animated splash screen to welcome users.
- **Secure Authentication:** Firebase Authentication for seamless SignIn and SignUp flows.
- **Adaptive Theming:** Supports light and dark mode themes for user preference.
- **Intuitive Navigation:** Smooth navigation system for easy app exploration.
- **Photo Sharing:** Upload and share photos with the community.
- **Social Interactions:** Like, comment, and bookmark posts.
- **User Profiles:** Customizable user profiles with post history.
- **Explore Feed:** Discover trending photos in grid and list view layouts.
- **Optimized State Management:** Uses ViewModel to maintain UI state efficiently.
- **Enhanced Error Handling:** Custom AlertDialogs provide clear error messages.
- **Real-time Input Validation:** Improved form usability with field-specific checks.

## Screenshots

<p float="left">
  <img src="https://github.com/user-attachments/assets/f43bc563-1d1a-4acb-99e7-91def1442398" width="150" />
  <img src="https://github.com/user-attachments/assets/1a898860-3b1f-43bc-a554-dbb6ff39ff2d" width="150" />
  <img src="https://github.com/user-attachments/assets/0463ba6d-3873-4464-a7be-3ea27109f4af" width="150" />
  <img src="https://github.com/user-attachments/assets/fcf7d426-dfd3-4e2f-9b6c-0dea94ba4711" width="150" />
  <img src="https://github.com/user-attachments/assets/3dd992b0-616b-4fb5-89b1-04ab93e958fe" width="150" />
</p>

<p float="left">
  <img src="https://github.com/user-attachments/assets/5027c346-99ec-4ff9-9bec-3d171ce2f560" width="150" />
  <img src="https://github.com/user-attachments/assets/1cfcd84c-a452-4a5a-8d20-431ea553a09b" width="150" />
  <img src="https://github.com/user-attachments/assets/3a9084fa-cd69-4337-a283-4d4d43b5794b" width="150" />
   <img src="https://github.com/user-attachments/assets/6945e3c6-53c0-4aca-b816-d12592d680ca" width="150" />
  <img src="https://github.com/user-attachments/assets/de31ec09-4235-481d-9bf1-f577d61a72c8" width="150" />
</p>

## Functionalities and Implementation

PhotoNest offers a comprehensive set of features built with modern Android development practices using Kotlin and Jetpack Compose.

### User Authentication

- **Feature:** Secure SignIn and SignUp flows with email verification.
- **Implementation:** Utilizes Firebase Authentication SDK for password-based user authentication, including session management and secure token handling. Firebase handles backend user data storage and ensures security compliance seamlessly.

### Theming

- **Feature:** Dynamic Light and Dark mode support that adapts to user preferences.
- **Implementation:** Uses Jetpack Compose's Material3 theming system with `MaterialTheme`. Theme toggling is managed via `ViewModel` with exposed state, ensuring UI updates reactively across the entire app.

### Navigation

- **Feature:** Smooth, intuitive navigation across app screens including Explore, Post Detail, Profile, Bookmarks, and Settings.
- **Implementation:** Implements Jetpack Navigation Compose library, leveraging `NavHost` and `NavController` to handle navigation stack, deep links, and screen arguments efficiently.

### State Management

- **Feature:** Robust UI state maintenance across configuration changes and process death.
- **Implementation:** Applies Android's `ViewModel` architecture component for each UI scope, combined with `StateFlow` for reactive state updates. This approach prevents UI inconsistencies and preserves user inputs during configuration changes.

### Post Management

- **Feature:** Users can upload photos, view them in grid or list layouts, and interact through likes and bookmarks.
- **Implementation:** Uses Firebase Firestore for real-time data syncing and Firebase Storage for image hosting. Images are compressed before upload to optimize storage and bandwidth. Post metadata is stored in Firestore documents with efficient querying and pagination support.

### Comments System

- **Feature:** Real-time commenting on posts with timestamp and user attribution.
- **Implementation:** Comments are stored as Firestore subcollections under each post document for scalability. The system uses `ListenableFuture` for real-time updates, displaying new comments instantly without manual refresh.

### Bookmarks

- **Feature:** Save favorite posts for later viewing.
- **Implementation:** Bookmark data is stored both in Firestore (for sync across devices) and locally using Room database for offline access. The dual-storage approach ensures seamless user experience regardless of network connectivity.

### Image Loading and Caching

- **Feature:** Efficient image loading with smooth scrolling in feeds and galleries.
- **Implementation:** Integrates Coil library (Kotlin-first image loading) to asynchronously fetch and cache images from Firebase URLs. Includes placeholder and error state handling for enhanced user experience.

### Error Handling and Validation

- **Feature:** Clear feedback for operations like authentication failures, upload errors, and invalid inputs.
- **Implementation:** Custom Compose AlertDialogs with descriptive error messages. Real-time input validation logic in ViewModels enables/disables action buttons based on field correctness, reducing user errors.

### Pull-to-Refresh

- **Feature:** Refresh feed content with pull-down gesture.
- **Implementation:** Uses Compose's `SwipeRefresh` component integrated with coroutine-based data fetching to reload posts from Firestore on user trigger.

## Getting Started

Follow these steps to set up and run PhotoNest locally:

1. **Clone the repository:**

    ```bash
   git clone https://lnkd.in/gZ8aEvMQ

2. **Open the project** in Android Studio (latest version recommended).

3. **Sync Gradle dependencies** and resolve any build issues.

4. **Configure Firebase:**
- Create a new Firebase project in the [Firebase Console](https://console.firebase.google.com/)
- Enable Firebase Authentication (Email/Password provider)
- Enable Cloud Firestore and Firebase Storage
- Download `google-services.json` and place it in the `app/` directory

5. **Build and run** the app on an emulator or physical device.

## Prerequisites

- **Android Studio** (latest version)
- **Kotlin** 1.9.0 or higher
- **Jetpack Compose** UI framework
- **Firebase account** for Authentication, Firestore, and Storage setup
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

## Architecture & Technologies

PhotoNest follows **Clean Architecture** principles ensuring maintainability, testability, and modularity.

### Architecture Pattern
- **MVVM (Model-View-ViewModel)** with clear separation of concerns
- **Repository pattern** for data abstraction
- **Use cases** for business logic encapsulation

### Tech Stack
- **Jetpack Compose** - Modern declarative UI development
- **Firebase Authentication** - User authentication and authorization
- **Cloud Firestore** - Real-time NoSQL cloud database
- **Firebase Storage** - Cloud storage for user-uploaded images
- **Room Database** - Local data persistence and offline support
- **Coil** - Image loading and caching library
- **Hilt/Dagger** - Dependency injection
- **Kotlin Coroutines & Flow** - Asynchronous programming
- **Navigation Compose** - In-app navigation
- **Material3** - Modern Material Design components

### Future Enhancements
- Push notifications for likes and comments
- Advanced search and filtering options
- Direct messaging between users
- Photo editing capabilities within the app

## Contributing

Contributions are welcome! If you'd like to contribute to PhotoNest:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**PhotoNest** - Built with ❤️ by [Ashok Dewasi]

For questions, feedback, or issues, please open an issue on GitHub or contact the maintainer.
