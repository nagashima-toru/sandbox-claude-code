import { describe, it, expect } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useAuthContext } from '@/hooks/useAuthContext';
import { AuthProvider } from '@/contexts/AuthContext';
import { ROLES } from '@/lib/constants/roles';
import type { ReactNode } from 'react';

describe('useAuthContext', () => {
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
      <AuthProvider>{children}</AuthProvider>
    );

    const { result } = renderHook(() => useAuthContext(), { wrapper });

    expect(result.current).toBeDefined();
    expect(result.current.user).toBeNull();
    expect(result.current.isLoading).toBe(false);
    expect(result.current.setUser).toBeDefined();
  });

  it('setUser関数が提供される', () => {
    const wrapper = ({ children }: { children: ReactNode }) => (
      <AuthProvider>{children}</AuthProvider>
    );

    const { result } = renderHook(() => useAuthContext(), { wrapper });

    expect(result.current.user).toBeNull();
    expect(typeof result.current.setUser).toBe('function');

    // NOTE: setUserの動作検証は、後続のStoryでAPI統合時に実施する
    // 現時点では関数が提供されていることのみ確認
  });
});
