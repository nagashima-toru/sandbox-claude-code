import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useAuthContext } from '@/hooks/useAuthContext';
import { AuthProvider } from '@/contexts/AuthContext';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';

// Mock useRouter and usePathname
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    prefetch: vi.fn(),
  }),
  usePathname: () => '/',
}));

// Mock useCurrentUser
vi.mock('@/hooks/useCurrentUser', () => ({
  useCurrentUser: vi.fn(() => ({
    data: null,
    isLoading: false,
    error: null,
    refetch: vi.fn(),
  })),
}));

describe('useAuthContext', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });
  });

  it('AuthProvider外で使用した場合、エラーをスローする', () => {
    // Suppress console.error for this test
    const originalError = console.error;
    console.error = () => {};

    expect(() => {
      renderHook(() => useAuthContext());
    }).toThrow('useAuthContext must be used within AuthProvider');

    console.error = originalError;
  });

  it('AuthProvider内で使用した場合、Contextの値を取得できる', () => {
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>
        <AuthProvider>{children}</AuthProvider>
      </QueryClientProvider>
    );

    const { result } = renderHook(() => useAuthContext(), { wrapper });

    expect(result.current).toBeDefined();
    expect(result.current.user).toBeDefined();
    expect(result.current.isLoading).toBeDefined();
    expect(result.current.error).toBeDefined();
    expect(result.current.refetch).toBeDefined();
  });

  it('refetch関数が提供される', () => {
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>
        <AuthProvider>{children}</AuthProvider>
      </QueryClientProvider>
    );

    const { result } = renderHook(() => useAuthContext(), { wrapper });

    expect(typeof result.current.refetch).toBe('function');

    // NOTE: refetchの動作検証は、統合テストで実施する
    // 現時点では関数が提供されていることのみ確認
  });
});
