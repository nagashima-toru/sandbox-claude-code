# Docker Development Environment

Start the Docker development environment with hot reload.

## Command

```bash
docker compose up
```

## Features

- Hot reload for frontend and backend
- Source code mounted as volumes
- Debug port exposed (5005)
- Frontend: http://localhost:3000
- Backend: http://localhost:8080

## Common Tasks

```bash
# Start in background
docker compose up -d

# View logs
docker compose logs -f

# Stop
docker compose down
```

See [docs/DOCKER_DEPLOYMENT.md](../docs/DOCKER_DEPLOYMENT.md) for detailed documentation.