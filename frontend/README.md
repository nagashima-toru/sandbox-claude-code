# Frontend - Message Management Application

Next.js-based message management application with full CRUD functionality.

## Quick Start

### Prerequisites

- Node.js 20+ (recommended: use volta or nvm)
- pnpm (package manager)
- Backend API running on port 8080

### Installation

```bash
# Install dependencies
pnpm install

# Copy environment file
cp .env.local.example .env.local

# Generate API client from OpenAPI spec
pnpm generate:api

# Start development server
pnpm dev
```

Visit [http://localhost:3000](http://localhost:3000)

## Scripts

```bash
pnpm dev              # Start development server
pnpm build            # Build for production
pnpm start            # Start production server
pnpm lint             # Run ESLint
pnpm type-check       # Run TypeScript type checking
pnpm generate:api     # Generate API client from OpenAPI spec
pnpm test             # Run unit & component tests
pnpm test:ui          # Run tests with UI
pnpm test:e2e         # Run E2E tests
pnpm test:e2e:ui      # Run E2E tests with UI
```

## Tech Stack

- **Framework**: Next.js 15+ (App Router)
- **Language**: TypeScript
- **UI Library**: shadcn/ui
- **Styling**: Tailwind CSS
- **API Code Generation**: Orval
- **HTTP Client**: axios
- **State Management**: TanStack Query (React Query)
- **Form Management**: React Hook Form + Zod
- **Testing**: Vitest + Testing Library + Playwright

## API Code Generation

This project uses Orval to automatically generate TypeScript API client code from the backend's OpenAPI specification.

### Generate API Client

```bash
# Make sure backend is generating OpenAPI spec
cd ../backend
./mvnw verify

# Generate frontend API client
cd ../frontend
pnpm generate:api
```

Generated files will be in `src/lib/api/generated/` (DO NOT EDIT manually).

## Project Structure

```
frontend/
├── src/
│   ├── app/                    # Next.js App Router
│   ├── components/             # React components
│   │   ├── ui/                 # shadcn/ui components
│   │   ├── messages/           # Message-specific components
│   │   └── common/             # Common components
│   ├── lib/                    # Utility functions
│   │   ├── api/
│   │   │   ├── generated/      # Orval generated (DO NOT EDIT)
│   │   │   └── client.ts       # Axios instance
│   │   ├── validations/        # Zod schemas
│   │   └── utils.ts
│   ├── hooks/                  # Custom React hooks
│   └── types/                  # TypeScript types
├── tests/                      # Test files
├── public/                     # Static files
├── orval.config.ts             # Orval configuration
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

## Development Workflow

1. **Backend changes** → Regenerate OpenAPI: `cd backend && ./mvnw verify`
2. **Frontend updates** → Regenerate client: `cd frontend && pnpm generate:api`
3. **Type safety** → TypeScript will catch API contract changes

## Environment Variables

Create `.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Documentation

For detailed documentation, see [CLAUDE.md](./CLAUDE.md).

## Features

- Message CRUD operations (Create, Read, Update, Delete)
- Search and filter messages
- Sort by ID, Code, Content
- Pagination
- Responsive design (mobile-friendly)
- Form validation with real-time feedback
- Error handling

## License

MIT