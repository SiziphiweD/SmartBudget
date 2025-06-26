# 💰 SmartBudget – Expense Tracking & Budgeting App

## 📱 Overview
**SmartBudget** is a mobile-first budgeting application developed in Kotlin using Android Studio. The app helps users easily track their spending, set financial goals, and stay motivated through visual insights and gamified rewards — all while working **on real Android phones**.

Developed as the final project for **Part 3 of the OPSC6311 PoE**, the app includes key features such as expense tracking, category-based spending graphs, budget goal tracking, and custom functionality like reminders and CSV exports.

---

## 🎯 Core Features

### 📊 Expense Tracking & Analytics
- Add, view, and delete expenses across categories (Food, Transport, etc.)
- View a **graph showing amount spent per category** over a selectable period
- See **visual feedback** on whether you’re within or over your min/max budget goals
- Monthly summaries and history tracking

### 🏅 Gamification
- Earn **badges** for:
  - Logging expenses daily for 7 days
  - Staying within budget limits
  - Categorizing every expense
- Progress indicators and success messages

---

## 🌟 Custom Features

### ✅ Feature 1: **Daily Reminder Notifications**
Sends a daily reminder to encourage consistent logging of expenses.

### ✅ Feature 2: **Export Expenses to CSV**
Enables users to export their monthly spending to a `.csv` file for analysis, backup, or tax records.

These features are clearly implemented in the app and demonstrated in the presentation video.

---

## 📦 Technical Stack
- **Language:** Kotlin
- **Framework:** Android SDK
- **Database:** Firebase
- **Charts:** MPAndroidChart
- **Version Control:** Git + GitHub
- **CI/CD:** GitHub Actions

---

## 📲 Installation
To install the APK:
1. Download the latest [SmartBudget APK](app/build/outputs/apk/debug/app-debug.apk)
2. Transfer to an Android phone
3. Enable “Install from Unknown Sources”
4. Tap and install the APK

---

## 🎬 Demo Video
Watch the full walkthrough of **SmartBudget** on a real Android phone, with a voice-over explaining each feature:  
📹 [Watch the Demo Video](https://youtube.com/shorts/kMGkLqGlpT4?feature=share

---

## 🧪 Logging
SmartBudget uses `Log.d()` throughout the code to show events like:
- Adding an expense
- Badge achievements
- Graph updates

```kotlin
Log.d("SmartBudget", "Expense added: R350 - Food")
Log.d("SmartBudget", "Badge earned: Consistent Logger")
