/**
 * Extract user-friendly error message from API error
 */
export function getApiErrorMessage(error: unknown): string | null {
  if (!error) return null;

  // Type guard for axios-like error structure
  const hasResponse = typeof error === 'object' && true && 'response' in error;
  const response = hasResponse
    ? (error as { response?: { status?: number; data?: { message?: string } } }).response
    : undefined;
  const status = response?.status;

  const hasMessage = typeof error === 'object' && true && 'message' in error;
  const errorMessage = hasMessage ? (error as { message?: string }).message : undefined;
  const message = response?.data?.message || errorMessage;

  if (status === 409) {
    return 'A message with this code already exists. Please use a different code.';
  }
  if (status === 404) {
    return 'Message not found. It may have been deleted.';
  }
  if (status === 400) {
    return message || 'Invalid input. Please check your data.';
  }
  if (status === 500) {
    return 'Server error. Please try again later.';
  }
  const hasCode = typeof error === 'object' && true && 'code' in error;
  const code = hasCode ? (error as { code?: string }).code : undefined;
  if (code === 'ECONNABORTED' || code === 'ERR_NETWORK') {
    return 'Network error. Please check your connection and try again.';
  }

  return message || 'An unexpected error occurred. Please try again.';
}
