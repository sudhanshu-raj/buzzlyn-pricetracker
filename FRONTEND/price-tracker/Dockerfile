# Build
FROM node:18-alpine as build
WORKDIR /app

ARG VITE_BACKEND_SCRAPER_URL
ARG VITE_VAPID_PUBLIC_KEY
ARG VITE_BACKEND_SECURITY_URL
ENV VITE_BACKEND_SCRAPER_URL=${VITE_BACKEND_SCRAPER_URL}
ENV VITE_VAPID_PUBLIC_KEY=${VITE_VAPID_PUBLIC_KEY}
ENV VITE_BACKEND_SECURITY_URL=${VITE_BACKEND_SECURITY_URL}

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf 