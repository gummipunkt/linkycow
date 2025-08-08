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
2. Install the APK 

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

## Roadmap

- [ ] Dark mode support (automatic with Material You)
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