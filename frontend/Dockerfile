# Stage 1: Build the Flutter web application
FROM ghcr.io/cirruslabs/flutter:3.24.0 AS build

# Set working directory
WORKDIR /app

# Copy pubspec files and fetch dependencies
COPY pubspec.yaml pubspec.lock ./
RUN flutter pub get

# Copy the rest of the application code
COPY . .

# Build the web application
RUN flutter build web --release

# Stage 2: Serve the application using Nginx
FROM nginx:alpine

# Copy the build output to Nginx's html directory
COPY --from=build /app/build/web /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
