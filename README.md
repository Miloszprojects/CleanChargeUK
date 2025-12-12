# ⚡ CleanCharge UK

**CleanCharge UK** is a modern web application that helps users **charge electric vehicles when the UK power grid is the cleanest** 🌱⚡  
The app visualizes the energy mix and recommends the best charging windows based on real regional data.

---

## ✨ Features

- 🗺️ **Interactive UK Map**
  - Select a region by clicking on the map
  - Hover highlights regions without triggering data loading
- 🥧 **Energy Mix Visualization**
  - Three pie charts showing the energy mix for:
    - Today
    - Tomorrow
    - The day after tomorrow
  - Clean energy percentage displayed for each day
- 📊 **Charging Window Recommendations**
  - Bar chart with optimal charging windows (1–6 hours)
  - Slider to select preferred charging duration
  - Overlay with exact start/end time and average clean energy share
- ⏱️ **UK Timezone Aware**
  - All dates and times are displayed in **Europe/London** timezone

---

## 🧱 Architecture Overview

The project follows a clean, modular architecture:

### Frontend
- **Angular (standalone components)**
- Charting powered by **Chart.js + ng2-charts**
- SVG-based interactive UK map
- Responsive dashboard layout

### Backend
- **Spring Boot (Java)**
- Integrates with **UK Carbon Intensity API**
- Computes:
  - Daily energy mixes
  - Optimal charging windows (sliding window algorithm)
- Fully timezone-aware (UK / UTC handling)

### Database
- **PostgreSQL**
- Used for persistence and future extensibility

---

## 🚀 Deployment (Kubernetes)

The application is deployed using **Kubernetes** and **Docker**, fully containerized.

Deployment is managed via **three YAML manifests**:

```
k8s/
├── backend.yaml     # Spring Boot API
├── frontend.yaml    # Angular frontend
└── postgres.yaml    # PostgreSQL database
```

Each service runs in its own pod and communicates internally via Kubernetes services.

---

## 🛠️ Local Development (Optional)

High-level overview:

1. Run backend (Spring Boot)
2. Run frontend (Angular)
3. Ensure Carbon Intensity API access
4. Configure environment variables if needed

---

## 🌍 Purpose & Vision

CleanCharge UK was built to:

- Encourage **greener EV charging habits**
- Make complex energy data **easy to understand**
- Combine **data, UX, and infrastructure best practices**

The application fully meets its original assumptions and works smoothly end-to-end 🚀

---


Enjoy charging when the grid is greenest 💚⚡  
**CleanCharge UK**

---

## 👤 Maintainer

**Milosz Podsiadly**  
📧 [m.podsiadly99@gmail.com](mailto:m.podsiadly99@gmail.com)  
🔗 [GitHub – MiloszPodsiadly](https://github.com/MiloszPodsiadly)

---
## 🪪 License

Licensed under the [MIT License](https://opensource.org/licenses/MIT).