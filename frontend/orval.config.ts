import { defineConfig } from 'orval';

export default defineConfig({
  api: {
    input: {
      target: '../specs/openapi/openapi.yaml',
    },
    output: {
      target: './src/lib/api/generated/messages.ts',
      schemas: './src/lib/api/generated/models',
      client: 'react-query',
      httpClient: 'axios',
      mode: 'tags-split',
      override: {
        mutator: {
          path: './src/lib/api/client.ts',
          name: 'customInstance',
        },
      },
      mock: true,
    },
    hooks: {
      afterAllFilesWrite: 'prettier --write',
    },
  },
});
