# LinkyCow

LinkyCow is a modern Android application that provides a beautiful and intuitive interface for [Linkwarden](https://linkwarden.app/), the open-source collaborative bookmark manager.

## Features

### Modern Design
- **Material Design 3 (Material You)** - Beautiful pastel purple theme with adaptive colors
- **Themed Icons Support** - App icon adapts to your device's theme
- **Responsive UI** - Optimized for different screen sizes

### Link Management
- **Dashboard View** - Browse all your saved links with infinite scrolling
- **Add & Edit Links** - Create new links or edit existing ones with full metadata support
- **Collections & Tags** - Organize links with collections and tags (autocomplete support)
- **Search Functionality** - Quickly find links with real-time search
- **Link Details** - View comprehensive information about each link

### Localization
- **Multi-language Support** - Available in English and German
- **Automatic Language Detection** - Uses your device's language settings

### Integration & Convenience
- **Share Integration** - Save links shared from other apps with a single tap
- **Pull-to-Refresh** - Swipe down to refresh your link collection
- **Clickable URLs** - Tap any URL to open it in your browser
- **Auto-Login** - Secure session management with persistent login
- **Infinite Scrolling** - Seamlessly load more links as you scroll

### Security & Performance
- **Secure Authentication** - Safe storage of credentials with session management
- **Optimized Performance** - Efficient API calls with cursor-based pagination
- **Input Validation** - URL normalization and field validation

## Screenshots

| Dashboard | Search | Link Details | Add Link |
|-----------|--------|--------------|----------|
| *Coming soon* | *Coming soon* | *Coming soon* | *Coming soon* |

## Installation

### Requirements
- Android 10.0 (API level 34) or higher
- Access to a [Linkwarden](https://linkwarden.app/) instance

### From Releases
1. Download the latest APK from the [Releases](https://github.com/gummipunkt/linkycow/releases) page
2. Install the APK on your device
3. Grant necessary permissions when prompted

### From Source
1. Clone this repository:
   ```bash
   git clone https://github.com/gummipunkt/linkycow.git
   cd linkycow
   ```

2. Open the project in Android Studio

3. Build and install:
   ```bash
   ./gradlew assembleRelease
   ```

## Usage

1. **First Launch**: Enter your Linkwarden server URL, username, and password
2. **Dashboard**: Browse your links, search, and pull to refresh
3. **Add Links**: Use the floating action button or share from other apps
4. **Edit Links**: Tap on any link to view details and edit
5. **Collections & Tags**: Organize your links using the dropdown and autocomplete fields

## Development

### Building from Source

1. **Prerequisites:**
   - Android Studio Hedgehog (2023.1.1) or later
   - Android SDK 35
   - Kotlin 1.9.23
   - Gradle 8.13

2. **Clone and Setup:**
   ```bash
   git clone https://github.com/gummipunkt/linkycow.git
   cd linkycow
   ```

3. **Build:**
   ```bash
   ./gradlew assembleDebug
   ```

### Project Structure
```
app/
├── src/main/java/com/wltr/linkycow/
│   ├── data/
│   │   ├── local/          # Local data storage (sessions)
│   │   └── remote/         # API clients and DTOs
│   ├── ui/
│   │   ├── login/          # Authentication screens
│   │   ├── main/           # Dashboard with infinite scrolling
│   │   ├── linkdetail/     # Link detail view and editing
│   │   ├── addlink/        # Add/edit link with collections & tags
│   │   ├── settings/       # App settings and logout
│   │   ├── about/          # About screen
│   │   ├── share/          # Share functionality from other apps
│   │   ├── navigation/     # Navigation with Safe Args
│   │   ├── theme/          # Material 3 theming
│   │   └── common/         # Shared UI components
│   └── MainActivity.kt
└── src/main/res/
    ├── values/             # English strings and themes
    ├── values-de/          # German translations
    ├── mipmap-*/           # Adaptive app icons
    └── drawable/           # Vector drawables and icons
```

### Technical Stack
- **Architecture**: MVVM with Jetpack Compose
- **Networking**: Ktor client with kotlinx.serialization
- **State Management**: StateFlow and Compose State
- **Navigation**: Jetpack Navigation with Safe Args
- **UI**: Material Design 3 (Material You)
- **Language**: Kotlin with Coroutines

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Guidelines
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Notes
- Follow Material Design 3 guidelines for UI changes
- Add string resources for both English and German
- Test on different screen sizes and orientations
- Ensure proper error handling and loading states

## Roadmap

- [ ] Dark mode support (automatic with Material You)
- [ ] Bulk operations (select multiple links)
- [ ] Offline support with local caching
- [ ] Widget support for quick link access
- [ ] Custom themes beyond Material You

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Linkwarden](https://linkwarden.app/) - The amazing bookmark manager that makes this app possible
- [Material Design 3](https://m3.material.io/) - For the beautiful design system
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - For the modern UI toolkit

---

**Made with ❤️ for the Linkwarden community**

*LinkyCow is not affiliated with or endorsed by the Linkwarden project. It is an independent client application.* 