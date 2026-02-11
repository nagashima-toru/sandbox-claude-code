'use client';

import { createContext, type ReactNode } from 'react';
import type { UserResponse } from '@/lib/api/generated/models';
import type { UnauthorizedResponse } from '@/lib/api/generated/models';
import { useCurrentUser } from '@/hooks/useCurrentUser';

/**
 * Authentication context value
 */
export interface AuthContextValue {
  /**
   * Current authenticated user
   */
  user: UserResponse | null;

  /**
   * Whether user information is being loaded
   */
  isLoading: boolean;

  /**
   * Error that occurred during user information fetch
   */
  error: UnauthorizedResponse | null;

  /**
   * Refetch user information
   */
  refetch: () => void;
}

/**
 * Authentication context
 *
 * Provides user information across the application
 */
export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

/**
 * AuthProvider props
 */
export interface AuthProviderProps {
  children: ReactNode;
}

/**
 * AuthProvider component
 *
 * Provides authentication state to child components using /api/users/me endpoint.
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const { data: user, isLoading, error, refetch } = useCurrentUser();

  const value: AuthContextValue = {
    user: user ?? null,
    isLoading,
    error: error ?? null,
    refetch,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
