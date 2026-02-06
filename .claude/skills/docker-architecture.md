# Docker Architecture

## Two-Tier Configuration

- `docker-compose.yml` - Base/production configuration
- `docker-compose.override.yml` - Development overrides (auto-applied)

## Development Mode

**Recommended**:
```bash
./scripts/docker-dev.sh
```

**Direct command** (alternative):
```bash
docker compose up
```

| Service | Port | Features |
|---------|------|----------|
| Frontend | 3000 | Hot reload, pnpm dev |
| Backend | 8080 | Spring Boot DevTools |
| Debug | 5005 | Remote debugging |
| Nginx | 80 | Optional |

## Production Mode

**Recommended**:
```bash
./scripts/docker-prod.sh
```

**Direct command** (alternative):
```bash
docker compose -f docker-compose.yml up
```

| Service | Port | Features |
|---------|------|----------|
| Access | 80 | Via Nginx only |
| Frontend | Internal | pnpm start |
| Backend | Internal | Optimized build |

## Nginx Reverse Proxy

Routes: `/` → frontend:3000, `/api` → backend:8080

Enables relative URLs in browser (no CORS issues).

## Key Environment Variables

- `NEXT_PUBLIC_API_URL`: Backend URL (build-time)
- `COMPOSE_PROJECT_NAME`: Project namespace
- `PORT_OFFSET`: Multi-environment support

See [docs/DOCKER_DEPLOYMENT.md](../../docs/DOCKER_DEPLOYMENT.md)
