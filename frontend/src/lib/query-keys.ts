/**
 * Centralized query keys for TanStack Query.
 * These wrap the auto-generated keys from Orval for easier management.
 */
import {
  getGetAllMessagesQueryKey,
  getGetMessageByIdQueryKey,
} from '@/lib/api/generated/message/message';

export const queryKeys = {
  messages: {
    all: getGetAllMessagesQueryKey,
    byId: (id: number) => getGetMessageByIdQueryKey(id),
  },
} as const;

/**
 * Invalidation helpers for common operations
 */
export const invalidationKeys = {
  allMessages: () => queryKeys.messages.all(),
  messageById: (id: number) => queryKeys.messages.byId(id),
} as const;
