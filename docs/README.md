# Documentation Index

This directory contains all project documentation organized by category.

## 📚 Quick Navigation

### Development Process
Documentation for Epic-based development workflow and specifications.

- [Epic Documents](development/EPIC_DOCUMENTS.md) - Epic planning and management
- [Spec PR Guide](development/SPEC_PR_GUIDE.md) - Creating specification PRs
- [Git Workflow](development/GIT_WORKFLOW.md) - Branch strategy and PR workflow

### Environment Setup
Guides for setting up development environments.

- [Docker Deployment](environment/DOCKER_DEPLOYMENT.md) - Docker dev/prod modes
- [Git Worktree](environment/GIT_WORKTREE.md) - Multi-environment development
- [.gitignore Rules](environment/GITIGNORE_RULES.md) - .gitignore management

### Quality & Testing
Documentation for code quality, testing, and security.

- [Local CI Verification](quality/LOCAL_CI_VERIFICATION.md) - CI checks before push
- [Security](quality/SECURITY.md) - Security guidelines and Dependabot

### Frontend
Frontend-specific documentation.

- [Storybook](frontend/STORYBOOK.md) - Component development with Storybook
- [Orval API Generation](frontend/ORVAL_API_GENERATION.md) - API client generation
- [Frontend Performance](frontend/FRONTEND_PERFORMANCE_MONITORING.md) - Bundle size monitoring

### Deployment & Operations
Guides for deployment and production operations.

- [Deployment](deployment/DEPLOYMENT.md) - CD pipeline and deployment
- [Dependabot Docs Update](deployment/DEPENDABOT_DOCS_UPDATE.md) - Dependency update workflows

### Architecture & Design
System architecture and design documentation.

- [Architecture Overview](architecture/README.md) - System architecture overview
- [C4 Context Diagram](architecture/c4-context.md) - System context
- [C4 Container Diagram](architecture/c4-container.md) - Container architecture
- [API Design Guidelines](architecture/api/README.md) - API design standards
- [Error Handling](architecture/api/error-handling.md) - RFC 7807 error handling
- [ADR Index](adr/) - Architectural Decision Records

### ADR (Architectural Decision Records)
- [ADR-0001: OpenAPI-First](adr/0001-use-openapi-first.md) - OpenAPI-First approach

---

## 📖 Documentation by Audience

### For All Developers
- Epic Documents, Spec PR Guide, Git Workflow
- Docker Deployment, Local CI Verification, Security
- .gitignore Rules, Dependabot Docs Update

### For Frontend Developers
- Storybook, Orval API Generation, Frontend Performance
- API Design Guidelines, Error Handling

### For Backend Developers
- API Design Guidelines, Error Handling
- [Backend Test Strategy](../backend/docs/TEST_STRATEGY.md)

### For Architects & Tech Leads
- Architecture Overview, C4 Diagrams
- API Design Guidelines, ADRs
- Deployment

---

## 🔗 Related Documentation

- [Root CLAUDE.md](../CLAUDE.md) - Project-wide guidance
- [Backend CLAUDE.md](../backend/CLAUDE.md) - Backend-specific guidance
- [Frontend CLAUDE.md](../frontend/CLAUDE.md) - Frontend-specific guidance

---

## 📝 Documentation Conventions

- **File Naming**: `UPPER_SNAKE_CASE.md` for root-level docs
- **Organization**: Categorized by purpose (development, environment, quality, etc.)
- **Links**: Use relative paths from the document location
- **Updates**: Keep this index updated when adding new documentation
