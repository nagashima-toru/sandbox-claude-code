'use client';

import { createContext, useState, type ReactNode } from 'react';
import type { Role } from '@/lib/constants/roles';

/**
 * User information
 */
export interface User {
  id: number;
  username: string;
  role: Role;
}

/**
 * Authentication context value
 */
export interface AuthContextValue {
  /**
   * Current authenticated user
   */
  user: User | null;

  /**
   * Whether user information is being loaded
   */
  isLoading: boolean;

  /**
   * Update user information
   */
  setUser: (user: User | null) => void;
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
 * Provides authentication state to child components.
 * API integration will be added in Story 3.
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const value: AuthContextValue = {
    user,
    isLoading,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
