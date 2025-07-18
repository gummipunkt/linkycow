# LinkyCow 🐄

LinkyCow is a modern Android application that provides a beautiful and intuitive interface for [Linkwarden](https://linkwarden.app/), the open-source collaborative bookmark manager.

## Features ✨

- **Modern Material 3 UI** - Clean, intuitive interface following Android design guidelines
- **Dashboard View** - Browse all your saved links in an organized list
- **Search Functionality** - Quickly find links with real-time search
- **Link Details** - View comprehensive information about each link including tags and collections
- **Add Links** - Save new links directly from the app or share from other apps
- **Share Integration** - Save links shared from other apps with a single tap
- **Link Management** - Archive and delete links with confirmation dialogs
- **Pull-to-Refresh** - Swipe down to refresh your link collection
- **Clickable URLs** - Tap any URL to open it in your browser
- **Auto-Login** - Secure session management with persistent login

## Screenshots 📱

| Dashboard | Search | Link Details | Add Link |
|-----------|--------|--------------|----------|
| *Coming soon* | *Coming soon* | *Coming soon* | *Coming soon* |

## Installation 📲

### Requirements
- Android 10.0 (API level 29) or higher
- Access to a [Linkwarden](https://linkwarden.app/) instance

### From Source
1. Clone this repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/linkycow.git
   cd linkycow
   ```

2. Open the project in Android Studio

3. Build and install:
   ```bash
   ./gradlew assembleRelease
   ```

4. Install the APK on your device

## Setup 🛠️

1. **Launch LinkyCow** on your Android device
2. **Enter your Linkwarden details:**
   - Linkwarden URL (e.g., `https://your-linkwarden.com`)
   - Username
   - Password
3. **Tap Login** to connect to your Linkwarden instance
4. **Start managing your links!**

## Usage 💡

### Basic Operations
- **Browse Links**: View all your links on the main dashboard
- **Search**: Tap the search icon in the top bar to find specific links
- **View Details**: Tap any link to see detailed information, tags, and collection
- **Add Links**: Use the floating action button (+) to add new links manually
- **Share Links**: Share URLs from other apps directly to LinkyCow
- **Refresh**: Pull down on the dashboard to refresh your links

### Link Management
- **Archive**: Tap the archive icon in link details to archive a link
- **Delete**: Tap the delete icon in link details to permanently remove a link
- **Open**: Tap any URL to open it in your default browser

## Architecture 🏗️

LinkyCow is built using modern Android development practices:

- **Jetpack Compose** - Modern declarative UI toolkit
- **MVVM Architecture** - Clean separation of concerns
- **Kotlin Coroutines** - Asynchronous programming
- **Ktor Client** - HTTP networking with OkHttp engine
- **DataStore** - Secure data persistence
- **Navigation Component** - Type-safe navigation
- **Material 3** - Latest Material Design components

## API Integration 🔌

LinkyCow integrates with the Linkwarden API:

- **Authentication**: Secure session-based login
- **Dashboard**: Fetch all links via `/api/v2/dashboard`
- **Search**: Real-time search via `/api/v1/search`
- **Link Details**: Detailed information via `/api/v1/links/:id`
- **Create Links**: Add new links via `/api/v1/links`
- **Archive**: Archive links via `/api/v1/links/:id/archive`
- **Delete**: Remove links via `/api/v1/links/:id`

## Development 👨‍💻

### Building from Source

1. **Prerequisites:**
   - Android Studio Hedgehog (2023.1.1) or later
   - Android SDK 34
   - Kotlin 1.9.23

2. **Clone and Setup:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/linkycow.git
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
│   ├── data/           # Data layer (API clients, repositories)
│   ├── ui/             # UI layer (screens, viewmodels)
│   │   ├── login/      # Login functionality
│   │   ├── main/       # Dashboard and main screens
│   │   ├── linkdetail/ # Link detail view
│   │   ├── addlink/    # Add link functionality
│   │   ├── share/      # Share integration
│   │   ├── navigation/ # Navigation setup
│   │   ├── theme/      # Material 3 theming
│   │   └── common/     # Shared UI components
│   └── MainActivity.kt
└── src/main/res/       # Resources (layouts, strings, etc.)
```

## Contributing 🤝

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Guidelines
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Known Issues 🐛

- Some Android devices may show harmless system warnings in logs
- Large link collections may take a moment to load initially

## Roadmap 🗺️

- [ ] Dark mode support
- [ ] Bulk operations (select multiple links)
- [ ] Widget support
- [ ] Custom themes
- [ ] Add link features like Tagging, Categories
- [ ] Add and editing Tags and Categories

## Support 💬

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/gummipunkt/linkycow/issues) page
2. Create a new issue with detailed information
3. Include your Android version and device model

## Privacy 🔒

LinkyCow respects your privacy:
- No data is collected or transmitted to third parties
- All communication is directly with your Linkwarden instance
- Login credentials are stored securely on your device
- No analytics or tracking

## License 📄

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments 🙏

- [Linkwarden](https://linkwarden.app/) - The amazing bookmark manager that makes this app possible

---

**Made with ❤️ for the Linkwarden community**

*LinkyCow is not affiliated with or endorsed by the Linkwarden project. It is an independent client application.* 