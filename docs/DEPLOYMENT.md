# Deployment Guide

This document describes the Continuous Delivery (CD) pipeline for this project.

## Overview

The CD pipeline automatically builds Docker images and pushes them to GitHub Container Registry (GHCR) when changes are merged to the main branch. The pipeline is designed to support both staging and production deployments.

## Architecture

```
┌─────────────────┐
│   Git Push to   │
│   main/master   │
└────────┬────────┘
         │
         v
┌─────────────────────────────────────────┐
│   GitHub Actions Workflow (deploy.yml)  │
├─────────────────────────────────────────┤
│  1. Build Backend (Spring Boot)         │
│     - Compile JAR with Maven            │
│     - Build Docker image                │
│     - Push to GHCR                      │
│                                         │
│  2. Build Frontend (Next.js)            │
│     - Generate API client from OpenAPI  │
│     - Build Next.js production bundle   │
│     - Build Docker image                │
│     - Push to GHCR                      │
└─────────────────────────────────────────┘
         │
         v
┌─────────────────────────────────────────┐
│   GitHub Container Registry (GHCR)      │
├─────────────────────────────────────────┤
│  - ghcr.io/[org]/[repo]/backend:latest  │
│  - ghcr.io/[org]/[repo]/frontend:latest │
│  - Tagged versions (SHA, semver, etc.)  │
└─────────────────────────────────────────┘
         │
         v
┌─────────────────────────────────────────┐
│   Deployment (Manual or Automated)      │
├─────────────────────────────────────────┤
│  Staging:                               │
│    - Auto-deploy on main branch push    │
│    - Environment: staging               │
│                                         │
│  Production:                            │
│    - Deploy on release tag              │
│    - Environment: production            │
│    - Requires approval (optional)       │
└─────────────────────────────────────────┘
```

## Workflow Triggers

The deploy workflow (`.github/workflows/deploy.yml`) is triggered by:

1. **Push to main/master branch**
   - Automatically builds and pushes Docker images
   - Triggers staging deployment (when configured)

2. **Release published**
   - Builds images with release tag
   - Triggers production deployment (when configured)

3. **Manual dispatch**
   - Can be triggered manually from GitHub Actions UI

## Docker Images

### Backend Image

- **Base Image**: `eclipse-temurin:21-jre-alpine`
- **Build Process**:
  1. Multi-stage build with Maven
  2. Compile Spring Boot application
  3. Create minimal runtime image
- **Image Tags**:
  - `latest` (main branch)
  - `<branch-name>-<sha>` (branch builds)
  - `<version>` (release tags)

### Frontend Image

- **Base Image**: `node:20-alpine`
- **Build Process**:
  1. Install dependencies with pnpm
  2. Generate API client from OpenAPI spec
  3. Build Next.js production bundle
  4. Create minimal runtime image
- **Image Tags**: Same as backend

## GitHub Environments

To enable deployments, create the following environments in GitHub repository settings:

### 1. Staging Environment

**Purpose**: Automatic deployment for testing before production

**Settings** (Repository > Settings > Environments > New environment: `staging`):

- **Environment name**: `staging`
- **Deployment branches**: `main` or `master` only
- **Protection rules**:
  - ✅ Required reviewers: (optional for staging)
  - ✅ Wait timer: 0 minutes
- **Environment secrets**:
  - `NEXT_PUBLIC_API_URL`: Staging API URL (e.g., `https://api-staging.example.com`)
  - Additional secrets as needed (database URLs, API keys, etc.)

### 2. Production Environment

**Purpose**: Controlled deployment to production with approval

**Settings** (Repository > Settings > Environments > New environment: `production`):

- **Environment name**: `production`
- **Deployment branches**: `main` or `master` only
- **Protection rules**:
  - ✅ Required reviewers: Add 1-2 reviewers (mandatory)
  - ✅ Wait timer: 0-30 minutes (optional delay before approval request)
  - ✅ Prevent self-review: Recommended
- **Environment secrets**:
  - `NEXT_PUBLIC_API_URL`: Production API URL (e.g., `https://api.example.com`)
  - Additional production secrets (database URLs, API keys, etc.)

## Image Tagging Strategy

The pipeline uses multiple tags for flexibility:

| Tag Format | Description | Example | Use Case |
|------------|-------------|---------|----------|
| `latest` | Latest build from main branch | `backend:latest` | Quick testing, development |
| `<branch>-<sha>` | Branch name + commit SHA | `backend:main-a1b2c3d` | Specific commit deployment |
| `<version>` | Semantic version from release | `backend:v1.2.3` | Production releases |
| `<major>.<minor>` | Major.minor version | `backend:1.2` | Rolling updates |

## Deployment Options

### Option 1: Manual Deployment

After images are built and pushed to GHCR, deploy manually:

```bash
# Pull images from GHCR
docker pull ghcr.io/[org]/[repo]/backend:latest
docker pull ghcr.io/[org]/[repo]/frontend:latest

# Run with docker-compose (update docker-compose.yml to use GHCR images)
docker-compose up -d
```

### Option 2: Cloud Platform Deployment

Choose a cloud platform for automated deployment:

#### Render (Recommended for Beginners)

- **Pros**: Easy setup, free tier, automatic SSL
- **Setup**:
  1. Connect GitHub repository
  2. Configure services (backend, frontend, database)
  3. Set environment variables
  4. Auto-deploy on push

**Documentation**: https://render.com/docs

#### Railway

- **Pros**: Simple, great free tier, good DX
- **Setup**: Similar to Render

**Documentation**: https://docs.railway.app

#### Fly.io

- **Pros**: Global edge deployment, generous free tier
- **Setup**: Use `fly.toml` configuration

**Documentation**: https://fly.io/docs

#### AWS ECS (Advanced)

- **Pros**: Highly scalable, full AWS integration
- **Setup**:
  1. Create ECS cluster
  2. Define task definitions (use GHCR images)
  3. Create services with load balancers
  4. Configure auto-scaling

**Documentation**: https://docs.aws.amazon.com/ecs

#### Google Cloud Run (Advanced)

- **Pros**: Serverless, pay-per-use, scales to zero
- **Setup**:
  1. Push images to GHCR (already done)
  2. Deploy to Cloud Run from GHCR
  3. Configure environment variables

**Documentation**: https://cloud.google.com/run/docs

### Option 3: Kubernetes Deployment

For production-grade orchestration:

1. **Setup**: Create Kubernetes cluster (GKE, EKS, AKS, or local k3s)
2. **Configure**: Use Helm charts or kubectl manifests
3. **Deploy**: Pull images from GHCR

Example Kubernetes deployment:

```yaml
# backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
      - name: backend
        image: ghcr.io/[org]/[repo]/backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres:5432/sandbox
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
```

## Security Considerations

### Secrets Management

- **GitHub Secrets**: Store sensitive data in repository or environment secrets
- **Never commit**: API keys, passwords, tokens to git
- **Use**: GitHub Environments for environment-specific secrets
- **Rotate**: Regularly update credentials

### Image Security

- **Base Images**: Use official, minimal images (alpine-based)
- **Scanning**: Enable GitHub Dependabot for vulnerability scanning
- **Updates**: Regularly update base images and dependencies
- **Non-root**: Run containers as non-root users (already configured)

### Access Control

- **GHCR**: Images are private by default (good for production)
- **Public**: Can make images public if needed (Settings > Packages)
- **Tokens**: Use fine-grained personal access tokens for external access

## Monitoring and Logging

### Health Checks

Both Docker images include health checks:

- **Backend**: `http://localhost:8080/actuator/health`
- **Frontend**: `http://localhost:3000/api/health`

### Deployment Verification

After deployment, verify:

```bash
# Check backend health
curl https://api.example.com/actuator/health

# Check frontend
curl https://app.example.com

# Check Docker logs
docker logs <container-id>
```

### Rollback Procedure

If deployment fails:

1. **Identify**: Previous working version tag
2. **Redeploy**: Use specific image tag
   ```bash
   docker pull ghcr.io/[org]/[repo]/backend:v1.2.2
   docker-compose up -d
   ```
3. **Verify**: Health checks and functionality
4. **Fix**: Address issues in code, then redeploy

## Workflow Customization

### Adding Deployment Steps

To implement actual deployment (currently placeholder):

1. **Edit** `.github/workflows/deploy.yml`
2. **Add** deployment steps in `deploy-staging` or `deploy-production` jobs

Example for Render:

```yaml
- name: Deploy to Render
  uses: johnbeynon/render-deploy-action@v0.0.8
  with:
    service-id: ${{ secrets.RENDER_SERVICE_ID }}
    api-key: ${{ secrets.RENDER_API_KEY }}
```

Example for AWS ECS:

```yaml
- name: Deploy to ECS
  uses: aws-actions/amazon-ecs-deploy-task-definition@v1
  with:
    task-definition: task-definition.json
    service: my-service
    cluster: my-cluster
```

### Notification Setup

Add notifications for deployment events:

```yaml
- name: Notify Slack on success
  if: success()
  uses: 8398a7/action-slack@v3
  with:
    status: success
    text: 'Deployment to staging successful!'
    webhook_url: ${{ secrets.SLACK_WEBHOOK_URL }}

- name: Notify Slack on failure
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: failure
    text: 'Deployment to staging failed!'
    webhook_url: ${{ secrets.SLACK_WEBHOOK_URL }}
```

## Troubleshooting

### Build Failures

**Issue**: Docker build fails

**Solutions**:
- Check Dockerfile syntax
- Verify base image availability
- Ensure all dependencies are installed
- Check logs in GitHub Actions

**Issue**: Maven build fails (backend)

**Solutions**:
- Verify Java version (21 required)
- Check pom.xml for errors
- Ensure tests pass locally

**Issue**: Next.js build fails (frontend)

**Solutions**:
- Verify Node.js version (20 required)
- Check for TypeScript errors
- Ensure API client generation works
- Check environment variables

### Image Push Failures

**Issue**: Cannot push to GHCR

**Solutions**:
- Check `GITHUB_TOKEN` permissions (write:packages required)
- Verify workflow permissions in settings
- Check GHCR status: https://www.githubstatus.com

### Deployment Failures

**Issue**: Container fails to start

**Solutions**:
- Check health check configuration
- Verify environment variables
- Check container logs
- Ensure database is accessible

**Issue**: Application returns errors

**Solutions**:
- Verify environment variables match deployment environment
- Check database connection strings
- Review application logs
- Test locally with production-like configuration

## Next Steps

1. **Choose Deployment Platform**: Select from options above (Render, Railway, etc.)
2. **Create Accounts**: Sign up for chosen platform
3. **Configure Environments**: Set up GitHub Environments with secrets
4. **Implement Deployment**: Add platform-specific deployment steps
5. **Test**: Verify staging deployment works
6. **Production**: Deploy to production with approval
7. **Monitor**: Set up logging and monitoring
8. **Document**: Update this file with platform-specific instructions

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Environments](https://docs.github.com/en/actions/deployment/targeting-different-environments)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot Docker](https://spring.io/guides/topicals/spring-boot-docker/)
- [Next.js Docker](https://nextjs.org/docs/deployment#docker-image)
