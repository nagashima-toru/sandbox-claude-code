'use client';

import { createContext, useEffect, type ReactNode } from 'react';
import { useRouter, usePathname } from 'next/navigation';
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
 *
 * Error handling:
 * - 401 Unauthorized: Redirect to login page
 * - Other errors: Default to read-only mode (user = null)
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const router = useRouter();
  const pathname = usePathname();
  const { data: user, isLoading, error, refetch } = useCurrentUser();

  // Handle authentication errors
  useEffect(() => {
    if (error && 'status' in error && error.status === 401) {
      // Redirect to login on 401 Unauthorized, but only if not already on login page
      if (pathname !== '/login') {
        router.push('/login');
      }
    }
    // For other errors, we default to read-only mode (user = null)
    // which is handled by usePermission Hook
  }, [error, router, pathname]);

  const value: AuthContextValue = {
    user: user ?? null,
    isLoading,
    error: error ?? null,
    refetch,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
