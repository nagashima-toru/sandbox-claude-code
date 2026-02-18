import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MessageForm from '@/components/messages/MessageForm';
import { MessageResponse } from '@/lib/api/generated/models';

describe('MessageForm', () => {
  const mockOnSubmit = vi.fn();
  const mockOnCancel = vi.fn();

  const defaultProps = {
    onSubmit: mockOnSubmit,
    onCancel: mockOnCancel,
    isSubmitting: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('レンダリング', () => {
    it('フォームが正しくレンダリングされる', () => {
      render(<MessageForm {...defaultProps} />);

      expect(screen.getByLabelText(/code/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/content/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /save/i })).toBeInTheDocument();
    });

    it('初期データが設定されている場合、フォームに値が表示される', () => {
      const initialData: MessageResponse = {
        id: 1,
        code: 'TEST001',
        content: 'Test message content',
        createdAt: '2026-01-29T00:00:00Z',
        updatedAt: '2026-01-29T00:00:00Z',
      };

      render(<MessageForm {...defaultProps} initialData={initialData} />);

      expect(screen.getByLabelText(/code/i)).toHaveValue('TEST001');
      expect(screen.getByLabelText(/content/i)).toHaveValue('Test message content');
    });

    it('エラーメッセージが表示される', () => {
      const error = new Error('Something went wrong');
      render(<MessageForm {...defaultProps} error={error} />);

      expect(screen.getByText(/something went wrong/i)).toBeInTheDocument();
    });

    it('送信中の場合、ボタンが無効化される', () => {
      render(<MessageForm {...defaultProps} isSubmitting={true} />);

      expect(screen.getByRole('button', { name: /saving/i })).toBeDisabled();
      expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
      expect(screen.getByLabelText(/code/i)).toBeDisabled();
      expect(screen.getByLabelText(/content/i)).toBeDisabled();
    });
  });

  describe('フォーム入力', () => {
    it('codeフィールドに入力できる', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const codeInput = screen.getByLabelText(/code/i);
      await user.type(codeInput, 'TEST001');

      expect(codeInput).toHaveValue('TEST001');
    });

    it('contentフィールドに入力できる', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const contentInput = screen.getByLabelText(/content/i);
      await user.type(contentInput, 'Test content');

      expect(contentInput).toHaveValue('Test content');
    });

    it('両方のフィールドに入力できる', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const codeInput = screen.getByLabelText(/code/i);
      const contentInput = screen.getByLabelText(/content/i);

      await user.type(codeInput, 'TEST001');
      await user.type(contentInput, 'Test content');

      expect(codeInput).toHaveValue('TEST001');
      expect(contentInput).toHaveValue('Test content');
    });
  });

  describe('バリデーション', () => {
    it('codeが空の場合、エラーメッセージを表示する', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const submitButton = screen.getByRole('button', { name: /save/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/code is required/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('contentが空の場合、エラーメッセージを表示する', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const codeInput = screen.getByLabelText(/code/i);
      await user.type(codeInput, 'TEST001');

      const submitButton = screen.getByRole('button', { name: /save/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/content is required/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('codeが51文字以上の場合、エラーメッセージを表示する', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const codeInput = screen.getByLabelText(/code/i);
      await user.type(codeInput, 'A'.repeat(51));

      const submitButton = screen.getByRole('button', { name: /save/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/code must be 50 characters or less/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('contentが501文字以上の場合、エラーメッセージを表示する', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const codeInput = screen.getByLabelText(/code/i);
      const contentInput = screen.getByLabelText(/content/i);

      await user.type(codeInput, 'TEST001');
      await user.type(contentInput, 'A'.repeat(501));

      const submitButton = screen.getByRole('button', { name: /save/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/content must be 500 characters or less/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('両方のフィールドが無効な場合、複数のエラーメッセージを表示する', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const submitButton = screen.getByRole('button', { name: /save/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/code is required/i)).toBeInTheDocument();
        expect(screen.getByText(/content is required/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  describe('送信処理', () => {
    it('有効なデータを送信できる', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const codeInput = screen.getByLabelText(/code/i);
      const contentInput = screen.getByLabelText(/content/i);

      await user.type(codeInput, 'TEST001');
      await user.type(contentInput, 'Test content');

      const submitButton = screen.getByRole('button', { name: /save/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          code: 'TEST001',
          content: 'Test content',
        });
      });
    });

    it('キャンセルボタンをクリックするとonCancelが呼ばれる', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      expect(mockOnCancel).toHaveBeenCalledOnce();
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('送信中はフォームを再送信できない', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} isSubmitting={true} />);

      const submitButton = screen.getByRole('button', { name: /saving/i });
      expect(submitButton).toBeDisabled();

      await user.click(submitButton);
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('初期データがある場合、編集して送信できる', async () => {
      const user = userEvent.setup();
      const initialData: MessageResponse = {
        id: 1,
        code: 'TEST001',
        content: 'Original content',
        createdAt: '2026-01-29T00:00:00Z',
        updatedAt: '2026-01-29T00:00:00Z',
      };

      render(<MessageForm {...defaultProps} initialData={initialData} />);

      const contentInput = screen.getByLabelText(/content/i);
      await user.clear(contentInput);
      await user.type(contentInput, 'Updated content');

      const submitButton = screen.getByRole('button', { name: /save/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          code: 'TEST001',
          content: 'Updated content',
        });
      });
    });

    it('最大文字数ちょうどのデータを送信できる', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} />);

      const codeInput = screen.getByLabelText(/code/i);
      const contentInput = screen.getByLabelText(/content/i);

      const maxCode = 'A'.repeat(50);
      const maxContent = 'B'.repeat(500);

      await user.type(codeInput, maxCode);
      await user.type(contentInput, maxContent);

      const submitButton = screen.getByRole('button', { name: /save/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          code: maxCode,
          content: maxContent,
        });
      });
    });
  });

  describe('エラー表示', () => {
    it('APIエラーが発生した場合、エラーメッセージを表示する', () => {
      const error = { response: { data: { message: 'Code already exists' } } };
      render(<MessageForm {...defaultProps} error={error} />);

      expect(screen.getByText(/code already exists/i)).toBeInTheDocument();
    });

    it('一般的なエラーが発生した場合、デフォルトメッセージを表示する', () => {
      const error = new Error('Network error');
      render(<MessageForm {...defaultProps} error={error} />);

      expect(screen.getByText(/network error/i)).toBeInTheDocument();
    });
  });

  describe('読み取り専用モード', () => {
    it('disabled が true の場合、全ての入力欄が無効化される', () => {
      render(<MessageForm {...defaultProps} disabled={true} />);

      expect(screen.getByLabelText(/code/i)).toBeDisabled();
      expect(screen.getByLabelText(/content/i)).toBeDisabled();
    });

    it('disabled が true の場合、Saveボタンが非表示になる', () => {
      render(<MessageForm {...defaultProps} disabled={true} />);

      expect(screen.queryByRole('button', { name: /save/i })).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /saving/i })).not.toBeInTheDocument();
    });

    it('disabled が true の場合、Cancelボタンが"Close"に変わる', () => {
      render(<MessageForm {...defaultProps} disabled={true} />);

      expect(screen.getByRole('button', { name: /close/i })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /cancel/i })).not.toBeInTheDocument();
    });

    it('disabled が true の場合、aria-readonly属性が設定される', () => {
      render(<MessageForm {...defaultProps} disabled={true} />);

      expect(screen.getByLabelText(/code/i)).toHaveAttribute('aria-readonly', 'true');
      expect(screen.getByLabelText(/content/i)).toHaveAttribute('aria-readonly', 'true');
    });

    it('disabled が true の場合でも、Closeボタンはクリックできる', async () => {
      const user = userEvent.setup();
      render(<MessageForm {...defaultProps} disabled={true} />);

      const closeButton = screen.getByRole('button', { name: /close/i });
      expect(closeButton).not.toBeDisabled();

      await user.click(closeButton);
      expect(mockOnCancel).toHaveBeenCalledOnce();
    });

    it('isSubmittingとdisabledの両方がtrueの場合、入力欄が無効化される', () => {
      render(<MessageForm {...defaultProps} isSubmitting={true} disabled={true} />);

      expect(screen.getByLabelText(/code/i)).toBeDisabled();
      expect(screen.getByLabelText(/content/i)).toBeDisabled();
    });
  });
});
