# 🎮 UNO Chat App

A modern, multiplayer UNO card game with integrated social features built using JavaFX 17. Play UNO with friends online, chat in real-time, share posts, and build your gaming community!

![JavaFX](https://img.shields.io/badge/JavaFX-17-blue)
![Firebase](https://img.shields.io/badge/Firebase-REST-orange)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 📋 Table of Contents

- [Features](#-features)
- [Technologies](#-technologies)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [How to Run](#-how-to-run)
- [Game Controls](#-game-controls)
- [Gameplay Features](#-gameplay-features)
- [Social Features](#-social-features)
- [Troubleshooting](#-troubleshooting)
- [Development Notes](#-development-notes)
- [Contributing](#-contributing)

---

## ✨ Features

### 🎯 Core Game Features
- **Classic UNO Gameplay**: Full implementation of official UNO rules
- **Multiple Game Modes**:
    - Single Player vs AI
    - Online Multiplayer
    - Private Room System
- **Smart Card System**: Auto-validation, special card effects (Skip, Reverse, Draw 2/4, Wild)
- **Turn Timer**: 10-second countdown per turn with visual progress bar
- **UNO Button**: Call "UNO!" when you have one card left
- **Dynamic Themes**: Random beautiful gradient themes for each game
- **Card Animations**: Smooth card dealing, drawing, and playing animations

### 💬 Social Features
- **Real-Time Chat**: In-game chat with message history
- **Friends System**:
    - Send/Accept/Decline friend requests
    - Online status indicators
    - Direct messaging
    - Unfriend capability
- **News Feed**:
    - Create posts with text and images
    - Like and comment on posts
    - Media upload support (via Catbox)
- **User Profiles**:
    - Custom avatar upload
    - Bio/Status editing
    - Activity tracking
- **Search**: Find and add new friends by username
- **Notifications**: Real-time friend request notifications

### 🎨 UI/UX Features
- **Modern Dark Theme**: Sleek gradient-based design
- **Responsive Layout**: Adapts to different screen sizes
- **Mouse Transparency**: Overlays don't block gameplay
- **Smooth Transitions**: Fade animations between scenes
- **Status Overlays**: Non-intrusive game notifications
- **Conditional UI**: Action bar hides in solo/AI mode

---

## 🛠 Technologies

### Frontend
- **JavaFX 17**: Modern Java UI framework
- **FXML**: Declarative UI layouts
- **CSS**: Custom styling with gradients and animations

### Backend & Services
- **Firebase Realtime Database**: User data, posts, friends
- **Firebase REST API**: Custom REST client implementation
- **Catbox.moe**: Image hosting service

### Networking
- **Java Sockets**: TCP networking for multiplayer
- **Multi-threading**: ExecutorService for background tasks
- **Scheduled Polling**: Friend requests and online status updates

### Development Tools
- **Java 17**: LTS version with module system
- **Maven/Gradle**: Dependency management (if applicable)
- **Git**: Version control

---

## 📁 Project Structure

```
UnoChatApp/
├── src/
│   ├── module-info.java              # Java Platform Module System
│   ├── application/                   # Main application & views
│   │   ├── Main.java                 # Application entry point
│   │   ├── UnoChatApp.java           # Chat application
│   │   ├── UnoGameApp.java           # Main game engine
│   │   ├── UnoGameMenu.java          # Main menu
│   │   ├── UnoLobbyScreen.java       # Multiplayer lobby
│   │   ├── LoginView.fxml            # Login screen layout
│   │   ├── NewsView.fxml             # News feed layout
│   │   └── profileView.fxml          # Profile page layout
│   ├── control/                       # Controllers (MVC)
│   │   ├── LoginController.java      # Login logic
│   │   ├── NewsController.java       # News feed logic
│   │   └── ProfileController.java    # Profile logic
│   ├── dao/                          # Data Access Objects
│   │   ├── FirebaseUserRest.java     # User authentication
│   │   ├── FirebaseNewsRest.java     # News & friends API
│   │   └── FirebaseProfileRest.java  # Profile API
│   ├── model/                        # Data models
│   │   ├── User.java                 # User model
│   │   └── Post.java                 # Post model
│   └── utils/                        # Utility classes
│       ├── CatboxUploader.java       # Image upload helper
│       ├── EmailService.java         # Email notifications
│       ├── NavigationHelper.java     # Scene navigation
│       └── showCustomAlert.java      # Alert dialogs
├── bin/                              # Compiled classes
├── classes/                          # Production build
├── img/                              # Image assets
├── uno_chat_history/                 # Chat logs
├── uno_config.properties             # Configuration file
└── README.md                         # This file
```

---

## 📋 Prerequisites

- **Java Development Kit (JDK) 17+**
- **JavaFX SDK 17** (if not bundled with JDK)
- **Internet Connection** (for Firebase and Catbox services)
- **Minimum 4GB RAM**
- **Screen Resolution**: 1280x720 or higher recommended

---

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/UnoChatApp.git
cd UnoChatApp
```

### 2. Download JavaFX SDK
If your JDK doesn't include JavaFX:
- Download JavaFX SDK 17 from [Gluon](https://gluonhq.com/products/javafx/)
- Extract to a known location (e.g., `~/javafx-sdk-17.0.16/`)

### 3. Configure Firebase
Edit `uno_config.properties`:
```properties
firebase.url=https://your-firebase-project.firebaseio.com/
firebase.auth.token=your-auth-token-here
```

### 4. Add Required JAR Dependencies
Ensure these JARs are in your classpath:
- `gson-2.8.9.jar` (JSON parsing)
- `javax.mail.jar` (Email service)
- `activation.jar` (Email attachments)
- JavaFX modules (if not using SDK)

---

## ⚙️ Configuration

### Firebase Setup
1. Create a Firebase Realtime Database project
2. Get your database URL and auth token
3. Update `uno_config.properties`
4. Initialize database structure:
```json
{
  "users": {},
  "posts": {},
  "friendRequests": {},
  "chatHistory": {}
}
```

### Email Service (Optional)
For OTP/password reset features:
```properties
email.smtp.host=smtp.gmail.com
email.smtp.port=587
email.username=your-email@gmail.com
email.password=your-app-password
```

---

## 🎮 How to Run

### Linux/Mac
```bash
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media \
     -Djava.library.path=/path/to/javafx-sdk/lib \
     -classpath ./bin:./lib/* \
     application.Main
```

### Windows
```cmd
java --module-path "C:\javafx-sdk\lib" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media ^
     -Djava.library.path="C:\javafx-sdk\lib" ^
     -classpath ".\bin;.\lib\*" ^
     application.Main
```

### IDE Setup (Eclipse/IntelliJ)
1. Import project as existing Java project
2. Add JavaFX SDK to module path
3. Add VM arguments:
   ```
   --module-path /path/to/javafx-sdk/lib
   --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media
   ```
4. Set main class: `application.Main`
5. Run

---

## 🎯 Game Controls

### General Controls
- **Mouse**: Click to select and play cards
- **Chat Button**: Toggle in-game chat panel
- **UNO Button**: Click when you have 1 card left (penalties apply if forgotten!)
- **Menu Button**: Return to main menu (forfeits game)

### Multiplayer Controls
- **Friend Icon**: Send friend request to player (real players only)
- **Block Icon**: Block player (real players only)
- **Chat**: Send messages visible to all players in room

### Card Playing Rules
- **Valid Moves**: Match color OR number OR play Wild/Wild Draw 4
- **Special Cards**:
    - **Skip**: Next player loses their turn
    - **Reverse**: Reverses play direction
    - **Draw 2**: Next player draws 2 cards
    - **Wild**: Choose any color
    - **Wild Draw 4**: Choose color + next player draws 4

---

## 🎲 Gameplay Features

### Hosting a Game
1. Click "Create Room" in lobby
2. Set room name and password (optional)
3. Wait for players to join
4. Click "Start Game" when ready

### Joining a Game
1. Click "Join Room" in lobby
2. Enter room code/password
3. Wait for host to start

### AI Opponents
- Smart card selection algorithm
- Instant turn execution
- No action bar/social features in AI mode

### Turn System
- 10-second timer per turn
- Auto-draw if time expires
- Visual progress bar indicator
- Turn order: clockwise (reversible)

### Winning Conditions
- First player to play all cards wins
- Points calculated based on remaining cards
- Leaderboard tracking (future feature)

---

## 👥 Social Features

### Friends Management
- **Add Friend**: Search by username → Send request
- **Accept/Decline**: Check notifications icon (red badge)
- **Unfriend**: Click friend card → Unfriend option
- **Online Status**: Green (online) / Red (offline) indicator
- **Direct Message**: Click friend → DM button → Opens chat

### News Feed
- **Create Post**:
    - Text content (required or image required)
    - Upload images (via Catbox.moe)
    - Auto-refresh feed
- **Interact**:
    - Like posts (❤️ button)
    - Comment (💬 button)
    - Real-time updates

### Profile
- **Edit Avatar**: Click profile circle → Upload image
- **Edit Bio**: Click "Edit Profile" → Enter bio → Save
- **View Stats**: Games played, win rate (future)

---

## 🔧 Troubleshooting

### Common Issues

#### "Package does not exist" compile error
**Solution**: Verify `module-info.java` exports all packages:
```java
exports application;
exports control;
exports dao;
exports model;
exports utils;
```

#### JavaFX components not found
**Solution**: Add JavaFX to module path:
```bash
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

#### ClassCastException: BorderPane cannot be cast to StackPane
**Solution**: Already fixed in NewsController/ProfileController with defensive root wrapping

#### Overlays blocking gameplay
**Solution**: Mouse transparency fixes applied to chat panel and status overlays

#### Images not loading
- Check internet connection
- Verify Firebase storage URLs
- Check Catbox.moe service status
- Image cache cleared on restart

#### Connection timeout in multiplayer
- Check firewall settings
- Verify port 12345 is open
- Ensure both players have internet access
- Try hosting on different network

#### macOS-specific issues
- Use `ms-17.0.16` JDK or later
- Ensure JavaFX native libraries match OS version
- Check `java.library.path` points to correct lib folder

---

## 💻 Development Notes

### Architecture Patterns
- **MVC**: Model-View-Controller separation
- **Observer**: Timeline-based game state updates
- **Factory**: Card and player creation
- **Singleton**: Current user state management

### Threading Strategy
- **Main Thread**: JavaFX UI operations only
- **Background Executor**: 5-thread pool for Firebase/network
- **Scheduled Executor**: Polling every 5 seconds
- **Platform.runLater()**: All UI updates from background threads

### Performance Optimizations
- **Image Cache**: LRU cache (max 50 images) with background loading
- **Incremental Friend List**: Update-in-place instead of rebuild
- **Mouse Transparency**: Prevent event propagation overhead
- **Conditional Rendering**: Hide unused UI elements

### Code Quality
- **Error Handling**: Try-catch on all network/IO operations
- **Resource Cleanup**: ExecutorService shutdown on exit
- **Memory Management**: Clear caches and maps on scene transitions
- **Null Safety**: Defensive checks before dereferencing

### Known Limitations
- Chat is local-only (no multiplayer sync yet)
- Blocklist exists but not integrated into UI filtering
- No reconnect logic if connection drops mid-game
- Limited to 4 players per room

---

## 🐛 Bug Reporting

Found a bug? Please report with:
1. **Steps to reproduce**
2. **Expected behavior**
3. **Actual behavior**
4. **Screenshots/logs**
5. **System info** (OS, Java version, JavaFX version)

---

## 🚀 Future Enhancements

- [ ] Chat synchronization for multiplayer
- [ ] Voice chat integration
- [ ] Spectator mode
- [ ] Tournament system
- [ ] Achievements and badges
- [ ] Custom card skins
- [ ] Mobile app (JavaFX Mobile/Gluon)
- [ ] Replay system
- [ ] AI difficulty levels
- [ ] Statistics dashboard

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Comment complex logic
- Use meaningful variable names
- Keep methods under 50 lines when possible
- Add JavaDoc for public APIs

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👨‍💻 Authors

- **Your Name** - *Initial work* - [YourGitHub](https://github.com/yourusername)

---

## 🙏 Acknowledgments

- UNO game rules by Mattel
- JavaFX community for excellent documentation
- Firebase for backend infrastructure
- Catbox.moe for image hosting
- All contributors and testers

---

## 📞 Contact

- **Email**: your.email@example.com
- **Discord**: YourDiscord#1234
- **GitHub Issues**: [Report a bug](https://github.com/yourusername/UnoChatApp/issues)

---

## 📸 Screenshots

### Game Room
![Game Room Screenshot](docs/screenshots/game_room.png)

### News Feed
![News Feed Screenshot](docs/screenshots/news_feed.png)

### Lobby
![Lobby Screenshot](docs/screenshots/lobby.png)

---

**Made with ❤️ and JavaFX**

*Last Updated: December 2025*
