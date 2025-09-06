# SHELT - Smart Helmet for Enhanced Location Tracking  

🚴‍♂️ **SHELT** is a smart helmet system designed to **enhance rider safety** through real-time crash detection, emergency alerts, and navigation assistance. It integrates **hardware (Raspberry Pi, accelerometer, GPS)** with **software (Android app in Kotlin, Firestore, and web interface)** to provide a seamless safety solution.  

🔗 **Live Website:** [shelt-web.vercel.app](https://shelt-web.vercel.app/)  

---

## 🚀 Features  

- **Crash Detection**  
  Detects crashes using accelerometer and triggers emergency response.  

- **Emergency SMS with Location**  
  Sends an SMS to the registered emergency contact with **live GPS location** upon crash.  

- **Smart Navigation**  
  Allows the rider to set a route using Google Maps, which is displayed on the helmet visor for hands-free navigation.  

- **Real-time Data Sync**  
  Integrated with **Firestore** for storing and retrieving user, emergency contact, and ride data.  

- **Cross-Platform Interface**  
  - 📱 **Android App (Kotlin)** – to manage navigation and emergency setup  
  - 🌐 **Web Dashboard** – for monitoring and data visualization  
  - ⚙️ **Hardware Module** – Raspberry Pi + sensors for real-world crash detection  

---

## 🛠️ Tech Stack  

**Hardware**  
- Raspberry Pi  
- Accelerometer  
- GPS Module  

**Software**  
- Android (Kotlin)  
- Firebase Firestore  
- SMS Gateway (for alerts)  
- Web App (React + Vercel)  

---

## ⚡ Getting Started  

### 1. Clone Repository  
```bash
git clone https://github.com/kittu02/SHELT.git
cd SHELT
```

### 2. Android App Setup  
- Open `android-app/` in **Android Studio**  
- Configure Firebase project and add your `google-services.json`  
- Run the app on your device  

### 3. Hardware Setup  
- Install Raspbian on Raspberry Pi  
- Connect accelerometer + GPS via GPIO  
- Run Python scripts from `hardware/` to start crash detection  

---

## 📊 Firestore Structure  

- **Users Collection**  
  - User ID  
  - Name  
  - Emergency Contact  

- **Crashes Collection**  
  - Crash ID  
  - User ID  
  - Location (lat, long)  
  - Timestamp  

---

## 🎯 Future Enhancements  

- Integration with **health monitoring sensors** (heart rate, temperature)  
- Voice-controlled navigation  
- Multi-language support  
- AI-based accident severity detection  

---


## 🤝 Contributing  

Contributions are welcome! Please fork this repo and submit a pull request.  

---

## 📜 License  

This project is licensed under the **MIT License** – see the [LICENSE](LICENSE) file for details.  


