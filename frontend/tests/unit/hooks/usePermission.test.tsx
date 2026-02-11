import { describe, it, expect } from 'vitest';
import { renderHook } from '@testing-library/react';
import { usePermission } from '@/hooks/usePermission';
import { AuthProvider, AuthContext } from '@/contexts/AuthContext';
import { ROLES } from '@/lib/constants/roles';
import type { UserResponse } from '@/lib/api/generated/models';
import type { ReactNode } from 'react';

describe('usePermission', () => {
  it('認証されていない場合、全権限がfalseでisReadOnlyがtrueになる', () => {
    const wrapper = ({ children }: { children: ReactNode }) => (
      <AuthProvider>{children}</AuthProvider>
    );

    const { result } = renderHook(() => usePermission(), { wrapper });

    expect(result.current.canCreate).toBe(false);
    expect(result.current.canEdit).toBe(false);
    expect(result.current.canDelete).toBe(false);
    expect(result.current.isReadOnly).toBe(true);
  });

  it('ADMINロールの場合、全権限がtrueになる（カスタムプロバイダー使用）', () => {
    const adminUser: UserResponse = { username: 'admin', role: ROLES.ADMIN };

    const TestWrapper = ({ children }: { children: ReactNode }) => (
      <AuthContext.Provider
        value={{
          user: adminUser,
          isLoading: false,
          error: null,
          refetch: () => {},
        }}
      >
        {children}
      </AuthContext.Provider>
    );

    const { result } = renderHook(() => usePermission(), { wrapper: TestWrapper });

    expect(result.current.canCreate).toBe(true);
    expect(result.current.canEdit).toBe(true);
    expect(result.current.canDelete).toBe(true);
    expect(result.current.isReadOnly).toBe(false);
  });

  it('VIEWERロールの場合、全権限がfalseでisReadOnlyがtrueになる', () => {
    const viewerUser: UserResponse = { username: 'viewer', role: ROLES.VIEWER };

    const TestWrapper = ({ children }: { children: ReactNode }) => (
      <AuthContext.Provider
        value={{
          user: viewerUser,
          isLoading: false,
          error: null,
          refetch: () => {},
        }}
      >
        {children}
      </AuthContext.Provider>
    );

    const { result } = renderHook(() => usePermission(), { wrapper: TestWrapper });

    expect(result.current.canCreate).toBe(false);
    expect(result.current.canEdit).toBe(false);
    expect(result.current.canDelete).toBe(false);
    expect(result.current.isReadOnly).toBe(true);
  });
});
