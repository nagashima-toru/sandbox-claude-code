/**
 * Extract user-friendly error message from API error
 */
export function getApiErrorMessage(error: any): string | null {
  if (!error) return null;

  const status = error?.response?.status;
  const message = error?.response?.data?.message || error?.message;

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
  if (error?.code === 'ECONNABORTED' || error?.code === 'ERR_NETWORK') {
    return 'Network error. Please check your connection and try again.';
  }

  return message || 'An unexpected error occurred. Please try again.';
}
