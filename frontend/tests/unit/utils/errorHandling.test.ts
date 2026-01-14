import { describe, it, expect } from 'vitest';
import { getApiErrorMessage } from '@/lib/utils/errorHandling';

describe('getApiErrorMessage', () => {
  it('null または undefined を返す（error が null の場合）', () => {
    expect(getApiErrorMessage(null)).toBeNull();
  });

  it('409 Conflict エラーメッセージを返す', () => {
    const error = {
      response: {
        status: 409,
        data: {},
      },
    };

    expect(getApiErrorMessage(error)).toBe(
      'A message with this code already exists. Please use a different code.'
    );
  });

  it('404 Not Found エラーメッセージを返す', () => {
    const error = {
      response: {
        status: 404,
        data: {},
      },
    };

    expect(getApiErrorMessage(error)).toBe('Message not found. It may have been deleted.');
  });

  it('400 Bad Request エラーメッセージを返す（カスタムメッセージあり）', () => {
    const error = {
      response: {
        status: 400,
        data: {
          message: 'Invalid code format',
        },
      },
    };

    expect(getApiErrorMessage(error)).toBe('Invalid code format');
  });

  it('400 Bad Request エラーメッセージを返す（カスタムメッセージなし）', () => {
    const error = {
      response: {
        status: 400,
        data: {},
      },
    };

    expect(getApiErrorMessage(error)).toBe('Invalid input. Please check your data.');
  });

  it('500 Server Error エラーメッセージを返す', () => {
    const error = {
      response: {
        status: 500,
        data: {},
      },
    };

    expect(getApiErrorMessage(error)).toBe('Server error. Please try again later.');
  });

  it('ECONNABORTED エラーメッセージを返す', () => {
    const error = {
      code: 'ECONNABORTED',
      message: 'timeout of 1000ms exceeded',
    };

    expect(getApiErrorMessage(error)).toBe(
      'Network error. Please check your connection and try again.'
    );
  });

  it('ERR_NETWORK エラーメッセージを返す', () => {
    const error = {
      code: 'ERR_NETWORK',
      message: 'Network Error',
    };

    expect(getApiErrorMessage(error)).toBe(
      'Network error. Please check your connection and try again.'
    );
  });

  it('カスタムメッセージを持つエラーオブジェクトからメッセージを抽出する', () => {
    const error = {
      response: {
        data: {
          message: 'Custom error message',
        },
      },
    };

    expect(getApiErrorMessage(error)).toBe('Custom error message');
  });

  it('response.data.message がない場合、error.message を使用する', () => {
    const error = {
      message: 'Something went wrong',
    };

    expect(getApiErrorMessage(error)).toBe('Something went wrong');
  });

  it('不明なエラーの場合、デフォルトメッセージを返す', () => {
    const error = {
      some: 'unknown error',
    };

    expect(getApiErrorMessage(error)).toBe('An unexpected error occurred. Please try again.');
  });

  it('文字列エラーの場合、デフォルトメッセージを返す', () => {
    const error = 'Some error string';

    expect(getApiErrorMessage(error)).toBe('An unexpected error occurred. Please try again.');
  });

  it('その他のステータスコードの場合、カスタムメッセージまたはデフォルトメッセージを返す', () => {
    const error = {
      response: {
        status: 403,
        data: {
          message: 'Forbidden',
        },
      },
    };

    expect(getApiErrorMessage(error)).toBe('Forbidden');
  });
});
