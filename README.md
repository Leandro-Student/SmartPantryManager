# SmartPantryManager

SmartPantryManager is an Android application designed to help users manage their pantry ingredients and suggest recipes based on what they have available. It helps reduce food waste and makes meal planning easier.

## Database Choice: SQLite

This app uses **SQLite** as its database. SQLite is a lightweight, embedded database that is built into every Android device. Here's why it's a great choice:

- **No Setup Required**: It's serverless, meaning you don't need to set up any separate database server.
- **Integrated**: It's an integral part of the Android operating system, so it's fast and reliable.
- **Perfect for Mobile**: It's ideal for local data storage on mobile devices, which is exactly what this app needs.
- **Structured Data**: It's a relational database, perfect for storing structured data like ingredients and recipes with clear relationships.

## Setup and Run Instructions

To run this project, follow these steps:

1. **Clone the repository**: Use `git clone https://github.com/Leandro-Student/SmartPantryManager-.git`
2. **Open in Android Studio**: Open the cloned project in Android Studio.
3. **Build and Run**: Let Android Studio sync the project. Once it's done, you can run the app on an emulator or physical device.

## Strict-Matching Logic
The core value of SmartPantryManager is its strict-matching algorithm.
A recipe is only suggested if **every single required ingredient** is currently present in the user's pantry, in at least the required quantity.
Partial matches (e.g., missing just one ingredient) are strictly excluded from the suggestions list to ensure the user never needs to go shopping.
The algorithm normalizes ingredient names (handles plurals like "tomatoes" -> "tomato") and converts units (e.g., "0.5 kg" -> "500 g") to guarantee accurate matching.