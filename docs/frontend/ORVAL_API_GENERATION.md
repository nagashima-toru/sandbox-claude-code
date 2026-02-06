# Orval API Code Generation Guide

This document provides detailed guidance for using Orval to generate TypeScript API client code from OpenAPI specifications.

## Table of Contents

- [Overview](#overview)
- [Configuration](#configuration)
- [Axios Instance](#axios-instance)
- [Generated Code Structure](#generated-code-structure)
- [Usage in Components](#usage-in-components)
- [Workflow](#workflow)
- [Important Notes](#important-notes)

## Overview

This project uses **Orval** to automatically generate TypeScript API client code and React Query hooks from the backend's OpenAPI specification.

**Benefits**:

- Type-safe API calls with zero manual typing
- Automatic React Query hooks generation
- Synced with backend API changes
- MSW handlers for testing

**Command**:

```bash
pnpm generate:api
```

## Configuration

Orval is configured in `orval.config.ts`:

```typescript
import { defineConfig } from 'orval';

export default defineConfig({
  api: {
    input: '../backend/src/main/resources/api/openapi.yaml',
    output: {
      target: './src/lib/api/generated/messages.ts',
      client: 'react-query',
      httpClient: 'axios',
      mode: 'single',
      override: {
        mutator: {
          path: './src/lib/api/client.ts',
          name: 'customInstance',
        },
      },
    },
  },
});
```

**Configuration Options**:

| Option | Description |
|--------|-------------|
| `input` | Path to OpenAPI specification file |
| `output.target` | Output file for generated code |
| `output.client` | Client library (`react-query`, `swr`, etc.) |
| `output.httpClient` | HTTP client (`axios`, `fetch`) |
| `output.mode` | Generation mode (`single`, `split`, etc.) |
| `override.mutator` | Custom axios instance configuration |

## Axios Instance

Create a custom axios instance with base URL and interceptors in `src/lib/api/client.ts`:

```typescript
import axios, { AxiosRequestConfig } from 'axios';

export const AXIOS_INSTANCE = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
});

// Request interceptor (add auth token, etc.)
AXIOS_INSTANCE.interceptors.request.use(
  (config) => {
    // Add authorization header if needed
    // const token = getAuthToken();
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor (handle errors globally)
AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle common errors (401, 403, 500, etc.)
    if (error.response?.status === 401) {
      // Handle unauthorized
    }
    return Promise.reject(error);
  }
);

// Custom instance for Orval
export const customInstance = <T>(config: AxiosRequestConfig): Promise<T> => {
  const source = axios.CancelToken.source();
  const promise = AXIOS_INSTANCE({
    ...config,
    cancelToken: source.token,
  }).then(({ data }) => data);

  // @ts-ignore
  promise.cancel = () => {
    source.cancel('Query was cancelled');
  };

  return promise;
};
```

## Generated Code Structure

After running `pnpm generate:api`, Orval generates:

### Type Definitions

```typescript
// src/lib/api/generated/messages.ts

// Auto-generated from OpenAPI schema
export interface MessageResponse {
  id: number;
  code: string;
  content: string;
}

export interface MessageRequest {
  code: string;
  content: string;
}
```

### API Functions

```typescript
// Auto-generated API functions
export const getAllMessages = (options?: AxiosRequestConfig) =>
  customInstance<MessageResponse[]>({
    url: '/messages',
    method: 'GET',
    ...options,
  });

export const getMessageById = (id: number, options?: AxiosRequestConfig) =>
  customInstance<MessageResponse>({
    url: `/messages/${id}`,
    method: 'GET',
    ...options,
  });

export const createMessage = (
  messageRequest: MessageRequest,
  options?: AxiosRequestConfig
) =>
  customInstance<MessageResponse>({
    url: '/messages',
    method: 'POST',
    data: messageRequest,
    ...options,
  });

export const updateMessage = (
  id: number,
  messageRequest: MessageRequest,
  options?: AxiosRequestConfig
) =>
  customInstance<MessageResponse>({
    url: `/messages/${id}`,
    method: 'PUT',
    data: messageRequest,
    ...options,
  });

export const deleteMessage = (id: number, options?: AxiosRequestConfig) =>
  customInstance<void>({
    url: `/messages/${id}`,
    method: 'DELETE',
    ...options,
  });
```

### React Query Hooks

```typescript
// Auto-generated React Query hooks
export const useGetAllMessages = <TData = MessageResponse[]>(
  options?: UseQueryOptions<MessageResponse[], Error, TData>
) => useQuery(['getAllMessages'], () => getAllMessages(), options);

export const useGetMessageById = <TData = MessageResponse>(
  id: number,
  options?: UseQueryOptions<MessageResponse, Error, TData>
) => useQuery(['getMessageById', id], () => getMessageById(id), options);

export const useCreateMessage = <TError = Error>(
  options?: UseMutationOptions<MessageResponse, TError, { data: MessageRequest }>
) => useMutation((params) => createMessage(params.data), options);

export const useUpdateMessage = <TError = Error>(
  options?: UseMutationOptions<
    MessageResponse,
    TError,
    { id: number; data: MessageRequest }
  >
) => useMutation((params) => updateMessage(params.id, params.data), options);

export const useDeleteMessage = <TError = Error>(
  options?: UseMutationOptions<void, TError, { id: number }>
) => useMutation((params) => deleteMessage(params.id), options);
```

## Usage in Components

Import the generated hooks directly:

```typescript
// components/messages/MessageTable.tsx
import {
  useGetAllMessages,
  useDeleteMessage,
} from '@/lib/api/generated/messages';

export function MessageTable() {
  const { data: messages, isLoading, error } = useGetAllMessages();
  const deleteMutation = useDeleteMessage({
    onSuccess: () => {
      // Invalidate and refetch
      queryClient.invalidateQueries(['getAllMessages']);
    },
  });

  const handleDelete = (id: number) => {
    deleteMutation.mutate({ id });
  };

  if (isLoading) return <Loading />;
  if (error) return <ErrorMessage error={error} />;

  return (
    <table>
      {messages?.map((message) => (
        <tr key={message.id}>
          <td>{message.code}</td>
          <td>{message.content}</td>
          <td>
            <button onClick={() => handleDelete(message.id)}>Delete</button>
          </td>
        </tr>
      ))}
    </table>
  );
}
```

### Using Mutations with Forms

```typescript
// components/messages/MessageForm.tsx
import { useCreateMessage, useUpdateMessage } from '@/lib/api/generated/messages';

export function MessageForm({ existingMessage }) {
  const createMutation = useCreateMessage({
    onSuccess: () => {
      queryClient.invalidateQueries(['getAllMessages']);
    },
  });

  const updateMutation = useUpdateMessage({
    onSuccess: () => {
      queryClient.invalidateQueries(['getAllMessages']);
    },
  });

  const onSubmit = (data: MessageRequest) => {
    if (existingMessage) {
      updateMutation.mutate({ id: existingMessage.id, data });
    } else {
      createMutation.mutate({ data });
    }
  };

  // ...
}
```

## Workflow

### After Backend API Changes

1. **Backend**: Regenerate OpenAPI spec

   ```bash
   cd backend
   ./mvnw verify
   ```

2. **Frontend**: Regenerate API client

   ```bash
   cd frontend
   pnpm generate:api
   ```

3. **TypeScript** will now show type errors if API contract changed

4. **Update components** to match new API types

### Development Flow

```
Backend OpenAPI Changes
         ↓
  ./mvnw verify (Backend)
         ↓
  pnpm generate:api (Frontend)
         ↓
  TypeScript type checking
         ↓
  Update components if needed
```

## Important Notes

### Do NOT Edit Generated Files

- **DO NOT** manually edit files in `src/lib/api/generated/`
- Changes will be overwritten on next generation
- If you need custom behavior, extend in separate files

### Always Use Generated Hooks

```typescript
// ✅ Good: Use generated hooks
import { useGetAllMessages } from '@/lib/api/generated/messages';

// ❌ Bad: Manual axios calls
import axios from 'axios';
axios.get('/api/messages');
```

### Re-run After Backend Changes

```bash
# After any backend API changes:
cd frontend && pnpm generate:api
```

### Gitignore Generated Code

Generated code is typically gitignored (add to `.gitignore`):

```gitignore
# Orval generated code
src/lib/api/generated/
```

This ensures:

- Fresh generation on each build
- No merge conflicts in generated code
- Smaller repository size

### MSW Handlers

Orval can also generate MSW handlers for testing:

```typescript
// orval.config.ts
export default defineConfig({
  api: {
    // ... other config
    output: {
      // ... other output config
      mock: true, // Generate MSW handlers
    },
  },
});
```

Generated handlers are in `src/lib/api/generated/messages.msw.ts`.

## Related Documentation

- [Storybook Guide](STORYBOOK.md) - MSW usage in Storybook
- [Orval Documentation](https://orval.dev/)
- [React Query Documentation](https://tanstack.com/query/latest)
