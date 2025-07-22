# LinkyCow

LinkyCow is a modern Android application that provides a beautiful and intuitive interface for [Linkwarden](https://linkwarden.app/), the open-source collaborative bookmark manager.

## Features

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

## Screenshots

| Dashboard | Search | Link Details | Add Link |
|-----------|--------|--------------|----------|
| *Coming soon* | *Coming soon* | *Coming soon* | *Coming soon* |

## Installation

### Requirements
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

## Development

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

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Guidelines
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Roadmap

- [ ] Dark mode support
- [ ] Bulk operations (select multiple links)
- [ ] Widget support
- [ ] Custom themes
- [ ] Add and editing Tags and Categories
- [ ] Add WebView for Preserved Formats
- [ ] Add infinite scrolling

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Linkwarden](https://linkwarden.app/) - The amazing bookmark manager that makes this app possible

---

**Made with ❤️ for the Linkwarden community**

*LinkyCow is not affiliated with or endorsed by the Linkwarden project. It is an independent client application.* 