# Buzzlyn PriceTracker 

**Overview**

- **What :** Helps to track the e-commerce products like amazon, flipkart, etc. and will notify when there is any changes in price or stock , currently supports whatsapp, email and push notification also . 
- **Tech:** Full stack (Spring Boot backends, React/Vite frontend, Python FastAPI scraper, MySQL, Nginx reverse-proxy) containerized with Docker Compose Web .

**Quick Demo**

https://github.com/user-attachments/assets/1b1ef751-2013-4df1-b0db-a703e89a7ed5

**Run whole web using docker or also can runs each separately** 
```
docker compose up --build
```

Access:
- Frontend: `http://localhost:8080`
- Auth API: `http://localhost:8080/auth/*`
- Main API: `http://localhost:8080/api/*`

Environment:
- Fill secrets in `./.env` (use `.env.example` as a template) before starting.

That's it — intentionally brief. See individual folders for service-specific details.

