# Heroku commands
- heroku logout
- heroku login
- heroku whoami (Shows the current user logged in as)


# Docker commands
- docker compose -f.\docker\docker-compose.yml build (uses cache, fast and may use old layers)
- docker compose -f.\docker\docker-compose.yml build --no-cache (slower, but forces rebuild)

- docker compose -f.\docker\docker-compose.yml up -d --build --wait (starts all containers and waits until 
the health checks passes before returning)

# Frontend start-up
- npm run build (runs once, and then exits)
- npm run build:watch (starts the same build, but stays running. Watches the source files for changes, rebuilds and writes new files to dist/)