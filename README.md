# Emergency Medical Service (EMS)

This repository contains the Emergency Medical Service platform, divided into:

- **`back/`**: Spring Boot backend service with Docker & PostgreSQL / Keycloak support.
- **`front/`**: Angular frontend command center application with Tailwind CSS and SSR support.

## Project Structure

```text
emergency-medical-service/
├── back/     # Spring Boot application
└── front/    # Angular command center application
```

## Getting Started

### Backend
Navigate to the backend directory:
```bash
cd back
./mvnw spring-boot:run
```
Or start with Docker Compose:
```bash
docker-compose up -d
```

### Frontend
Navigate to the frontend directory:
```bash
cd front
npm install
npm start
```
The frontend will run at `http://localhost:4200/`.
