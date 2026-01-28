# Docker Production Test

Test the production Docker build locally before deployment.

## Command

```bash
docker compose -f docker-compose.yml up
```

## Features

- Production-optimized build
- Nginx reverse proxy
- Access via http://localhost (port 80)
- No hot reload
- Non-root user for security

## Common Tasks

```bash
# Build and start
docker compose -f docker-compose.yml up --build

# Stop
docker compose down
```

See [docs/DOCKER_DEPLOYMENT.md](../docs/DOCKER_DEPLOYMENT.md) for detailed documentation.
