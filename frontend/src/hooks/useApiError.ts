import { useState, useCallback } from 'react';
import { getApiErrorMessage } from '@/lib/utils/errorHandling';

interface UseApiErrorReturn {
  error: string | null;
  setError: (error: unknown) => void;
  clearError: () => void;
  handleError: (error: unknown) => string;
}

/**
 * Custom hook for handling API errors with user-friendly messages.
 * Centralizes error handling logic for mutations and API calls.
 *
 * @example
 * const { error, setError, clearError, handleError } = useApiError();
 *
 * const mutation = useCreateMessage({
 *   mutation: {
 *     onError: (err) => setError(err),
 *   },
 * });
 */
export function useApiError(): UseApiErrorReturn {
  const [error, setErrorState] = useState<string | null>(null);

  const setError = useCallback((err: unknown) => {
    const message = getApiErrorMessage(err);
    setErrorState(message);
  }, []);

  const clearError = useCallback(() => {
    setErrorState(null);
  }, []);

  const handleError = useCallback((err: unknown): string => {
    const message = getApiErrorMessage(err) ?? 'An unexpected error occurred';
    setErrorState(message);
    return message;
  }, []);

  return {
    error,
    setError,
    clearError,
    handleError,
  };
}
