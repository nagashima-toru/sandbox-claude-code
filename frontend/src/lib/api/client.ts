import axios, { AxiosRequestConfig, AxiosError } from 'axios';

/**
 * Axios instance with base configuration
 */
export const AXIOS_INSTANCE = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor
 * Add authentication token, logging, etc.
 */
AXIOS_INSTANCE.interceptors.request.use(
  (config) => {
    // Add authorization header if needed
    // const token = getAuthToken();
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }

    // Log request in development
    if (process.env.NODE_ENV === 'development') {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`);
    }

    return config;
  },
  (error) => {
    console.error('[API Request Error]', error);
    return Promise.reject(error);
  }
);

/**
 * Response interceptor
 * Handle common errors globally
 */
AXIOS_INSTANCE.interceptors.response.use(
  (response) => {
    // Log response in development
    if (process.env.NODE_ENV === 'development') {
      console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, response.status);
    }
    return response;
  },
  (error: AxiosError) => {
    // Handle common errors
    if (error.response) {
      const status = error.response.status;

      switch (status) {
        case 401:
          // Handle unauthorized - redirect to login
          console.error('[API Error] Unauthorized');
          // router.push('/login');
          break;
        case 403:
          // Handle forbidden
          console.error('[API Error] Forbidden');
          break;
        case 404:
          // Handle not found
          console.error('[API Error] Not Found');
          break;
        case 409:
          // Handle conflict (e.g., duplicate code)
          console.error('[API Error] Conflict');
          break;
        case 500:
          // Handle server error
          console.error('[API Error] Server Error');
          break;
        default:
          console.error(`[API Error] ${status}`, error.message);
      }
    } else if (error.request) {
      // Request was made but no response received
      console.error('[API Error] No response received', error.message);
    } else {
      // Something else happened
      console.error('[API Error]', error.message);
    }

    return Promise.reject(error);
  }
);

/**
 * Custom instance for Orval
 * This function is used as the mutator in orval.config.ts
 */
export const customInstance = <T>(config: AxiosRequestConfig, options?: AxiosRequestConfig): Promise<T> => {
  const source = axios.CancelToken.source();

  const promise = AXIOS_INSTANCE({
    ...config,
    ...options,
    cancelToken: source.token,
  }).then(({ data }) => data);

  // @ts-expect-error
  promise.cancel = () => {
    source.cancel('Query was cancelled');
  };

  return promise;
};

export default customInstance;