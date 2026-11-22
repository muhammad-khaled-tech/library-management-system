# Build stage: compile the Angular app
FROM node:24-alpine AS build
WORKDIR /app

# Install dependencies in their own layer so code changes don't reinstall them
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npx ng build --configuration production

# Runtime stage: nginx as a non-root user serves the files and proxies /api to the backend
FROM nginxinc/nginx-unprivileged:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 8080
