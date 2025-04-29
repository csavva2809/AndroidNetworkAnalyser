# 📡 AndroidNetworkAnalyser

AndroidNetworkAnalyser is a Kotlin-based Android application designed to monitor and analyze mobile network activity in real time. It provides users with **detailed connection information**, **anomaly detection**, and **graphical visualizations** — all wrapped in a modern, intuitive UI.

> 🔐 Built with a security-first mindset, the app also includes features like anomaly alerting, notification delivery, and Wi-Fi Direct-based nearby device warnings.

---

## 🚀 Features

- 📶 Real-time WiFi & Mobile network monitoring
- ⚠️ Threat/anomaly detection (Unsecured networks, signal drops, data surges)
- 📊 Graphs for signal strength, data usage & anomalies
- 🔔 Push notifications for suspicious behavior
- 📤 Firebase sync for anomaly uploads
- 📡 Nearby device alerts using Wi-Fi Direct (P2P)
- 🗂️ Local data storage via Room (SQLite)

---

## 🛠️ Technologies & Libraries

| Component | Library / Tool |
|----------|----------------|
| UI & Theming | Jetpack Compose + Material3 |
| Charts | [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) |
| DB | Room Persistence Library |
| Cloud Sync | Firebase Firestore |
| Notifications | Android Notification APIs |
| Nearby Alerting | Wi-Fi P2P APIs |
| Permissions | AndroidX ActivityCompat & Permission Helpers |

---

## 📷 Screenshots

> Add actual screenshots in your repo `/screenshots` and reference them here.

| Dashboard | Graphs | Alerts |
|----------|--------|--------|
| ![](screenshots/dashboard.png) | ![](screenshots/graphs.png) | ![](screenshots/alerts.png) |

---

## 📦 Installation

1. Clone the repository  
   ```bash
   git clone https://github.com/csavva2809/AndroidNetworkAnalyser.git
