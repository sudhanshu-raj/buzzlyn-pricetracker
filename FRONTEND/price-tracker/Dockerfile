# Build
FROM node:18-alpine as build
WORKDIR /app

ARG VITE_VAPID_PUBLIC_KEY
ARG VITE_SCRAPER_BASE_URL
ARG VITE_SECURITY_BASE_URL


RUN echo "VITE_VAPID_PUBLIC_KEY=$VITE_VAPID_PUBLIC_KEY" > .env && \
    echo "VITE_SCRAPER_BASE_URL=$VITE_SCRAPER_BASE_URL" >> .env && \
    echo "VITE_SECURITY_BASE_URL=$VITE_SECURITY_BASE_URL" >> .env

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf 
