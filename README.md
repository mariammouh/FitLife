# FitLife - Personal Fitness & Activity Tracker

FitLife is a modern Android application designed to help users track their fitness journey, visualize progress through advanced analytics, and maintain motivation with a dynamic 3D avatar system.

##  Features

###  Comprehensive Dashboard
- **Real-time Stats**: Track calories burned, workout minutes, and total activities for the day.
- **Visual Analytics**: 
    - **Line Charts**: Monthly calorie trends.
    - **Pie Charts**: Distribution of activity types.
    - **Bar Charts**: Mood correlation and average session performance.
- **Goal Tracking**: Monitor weight progress with start, current, and target weight visualization.
- **Streaks**: Keep track of your consistency with a 7-day activity streak.

###  Dynamic 3D Avatar System
- **State Analysis**: The app analyzes your activity over the last 7 days to determine your "Fitness State" (Athletic, Healthy, Lazy, or Overworked).
- **Interactive 3D Models**: Powered by **Google Filament**, your avatar changes appearance based on your gender and fitness state.
- **3D Workout Instructor**: Follow 3D models for specific exercises like Yoga and Strength training.

###  Activity Management
- **Detailed Logging**: Record activity name, type, duration, distance, calories, intensity, and heart rate.
- **Mood Tracking**: Log your mood before and after workouts to see how exercise affects your mental well-being.
- **Activity History**: A searchable and detailed list of all your past sessions.

###  Personalized Settings & Reminders
- **Smart Reminders**: Schedule daily workout reminders and hydration alerts.
- **Goal Customization**: Set specific fitness goals (Lose Weight, Build Muscle, Maintain, Improve Endurance).
- **Premium Model**: Access unlimited sessions and advanced features with FitLife Premium.

##  Tech Stack

- **Language**: Kotlin
- **UI Framework**: XML Layouts & Material Design 3 (Transitioning to Jetpack Compose)
- **Networking**: Retrofit 2 & OkHttp
- **3D Rendering**: Google Filament (GLTF/GLB support)
- **Data Visualization**: MPAndroidChart
- **Notifications**: AlarmManager & NotificationCompat
- **Backend**: PHP / MySQL API


##  Setup & Configuration

1. **Backend**: Ensure your PHP backend is running and the MySQL database is imported.
2. **API URL**: Update the `BASE_URL` in `RetrofitClient.kt` to match your local server IP or domain.
3. **Assets**: Place `.glb` models for avatars and workouts in the `app/src/main/assets` directory.
4. **Build**: Build the project using Android Studio with Gradle 8.7+.

##  License

This project is licensed under the MIT License.
