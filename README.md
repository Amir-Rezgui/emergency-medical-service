# Emergency Medical Service (EMS)

This repository contains the Emergency Medical Service platform, divided into:

- **`backend/`**: Spring Boot backend service with Docker & MongoDB / Keycloak support.
- **`frontend/`**: Angular frontend command center application with Tailwind CSS and SSR support.

## Project Structure

```text
emergency-medical-service/
├── backend/     # Spring Boot application
└── frontend/    # Angular command center application
```

## Getting Started

### Backend
Navigate to the backend directory:
```bash
cd backend
./mvnw spring-boot:run
```
Or start with Docker Compose:
```bash
cd backend
docker-compose up -d
```

### Frontend
Navigate to the frontend directory:
```bash
cd frontend
npm install
npm start
```
The frontend will run at `http://localhost:4200/`.
