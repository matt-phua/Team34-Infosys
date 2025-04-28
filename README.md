# Meeters - A Mate-Matching Android App

SUTD 50.001 Information Systems & Programming 2025 - Team 34 1D Project

Welcome to **Meeters**, a real-time, location-based Android app designed to help people connect and meet new friends easily, especially in unfamiliar environments like a new school or workplace. Developed as part of the 50.001 1D project at SUTD by Team 34, this app fosters spontaneous and genuine social interactions using Google Maps and Firebase technologies.

---
## 👥 Team Members

- **Lim Han Yang** 
- **Htet Myat Ko Ko** 
- **Matthew Phua Tai Kit**
- **Chong Zhi Kai** 
- **Desmond Ngui You Hong**


---

## 📱 App Overview

In today's fast-paced world, many individuals struggle to find time or courage to make new connections. **Meeters** solves this by enabling real-time matching of nearby users who are open to meeting — all within just a few taps.

---

## 🔍 Key Features

- **📍 Real-Time Location Matching**: Discover nearby users and how far they are from you.
- **🟢 Availability Toggle**: Switch between "Available to meet" and "Do not disturb" modes.
- **📩 Instant Meetup Requests**: Send and receive real-time meeting requests with short messages.
- **📋 Track Requests**: View incoming and outgoing meeting requests, and their status (pending/accepted/declined).
- **🔐 Secure Authentication**: Powered by Firebase Authentication.

---

## 🧱 Tech Stack

- **Android (Java)**
- **Firebase Suite**:
  - Firebase Authentication
  - Firestore (NoSQL Database)
  - Firebase Cloud Messaging (FCM) – planned for future notifications
- **GeoFirestore**: Enables geospatial queries
- **Google Maps & Location Services**: For location rendering and distance calculations
- **UI/UX**: Designed using Canva and Figma

---

## 📐 System Design

- **Model-View-Controller Architecture**
- **Design Patterns**:
  - Inheritance (e.g. BaseActivity)
  - Observer (real-time Firestore listeners)
  - Adapter (RecyclerView)
  - Singleton (Firebase instances)

---

## 🧠 Data Structures & Algorithms

- **HashMap**: For key-value pair data with O(1) operations (used for user profiles, meeting request updates)
- **ArrayList**: For dynamic lists of nearby users and requests
- **Haversine Formula**: To calculate real-world distances between users
- **Linear Search & Sorting**: For filtering and displaying the latest requests

---

## ⚙️ App Architecture

- **Models**: `NearbyUser`, `MeetingRequest`
- **Activities**: `MainActivity`, `NotificationActivity`, etc.
- **Adapters**: `NearbyUserAdapter`, `MeetingRequestAdapter`

---

## 🧪 Future Improvements

1. **Advanced Chat Features**: Multimedia messaging, full-screen chat UI, and FCM-based chat notifications.
2. **Personalized Profiles**: Profile pictures, interest tags, and optional social media links.
3. **Discovery Extensions**: Group meetups and event-based matching features.


---

## 📎 References

- [GeoFirestore Android](https://github.com/imperiumlabs/GeoFirestore-Android)
- [Google Location Services API](https://developers.google.com/location-context/fused-location-provider)
- [Firebase Authentication Docs](https://firebase.google.com/docs/auth)
- [Firebase Firestore Docs](https://firebase.google.com/docs/firestore)

---

## 📜 License

This project is for academic use only and does not have a commercial license.




