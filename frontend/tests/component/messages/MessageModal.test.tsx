import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import MessageModal from '@/components/messages/MessageModal';
import { MessageResponse } from '@/lib/api/generated/models';

// Mock MessageForm component
vi.mock('@/components/messages/MessageForm', () => ({
  default: ({
    onSubmit,
    initialData,
    isSubmitting,
    onCancel,
    error,
  }: {
    onSubmit: (data: { code: string; content: string }) => void;
    initialData?: MessageResponse;
    isSubmitting: boolean;
    onCancel: () => void;
    error?: unknown;
  }) => (
    <div data-testid="message-form">
      <div data-testid="form-initial-data">{JSON.stringify(initialData)}</div>
      <div data-testid="form-is-submitting">{String(isSubmitting)}</div>
      <div data-testid="form-error">{error ? 'has-error' : 'no-error'}</div>
      <button onClick={() => onSubmit({ code: 'TEST', content: 'Test' })}>Submit</button>
      <button onClick={onCancel}>Cancel</button>
    </div>
  ),
}));

describe('MessageModal', () => {
  const mockOnOpenChange = vi.fn();
  const mockOnSubmit = vi.fn();

  const defaultProps = {
    open: true,
    onOpenChange: mockOnOpenChange,
    onSubmit: mockOnSubmit,
    mode: 'create' as const,
  };

  it('create モードで正しいタイトルと説明を表示する', () => {
    render(<MessageModal {...defaultProps} mode="create" />);

    expect(screen.getByText('Create New Message')).toBeInTheDocument();
    expect(screen.getByText('Fill in the details to create a new message.')).toBeInTheDocument();
  });

  it('edit モードで正しいタイトルと説明を表示する', () => {
    render(<MessageModal {...defaultProps} mode="edit" />);

    expect(screen.getByText('Edit Message')).toBeInTheDocument();
    expect(screen.getByText('Update the message details below.')).toBeInTheDocument();
  });

  it('MessageForm に initialData を渡す', () => {
    const initialData: MessageResponse = {
      id: 1,
      code: 'TEST',
      content: 'Test content',
      createdAt: '2026-01-29T00:00:00Z',
      updatedAt: '2026-01-29T00:00:00Z',
    };

    render(<MessageModal {...defaultProps} initialData={initialData} />);

    const formInitialData = screen.getByTestId('form-initial-data');
    expect(formInitialData).toHaveTextContent(JSON.stringify(initialData));
  });

  it('MessageForm に isSubmitting を渡す', () => {
    render(<MessageModal {...defaultProps} isSubmitting={true} />);

    const formIsSubmitting = screen.getByTestId('form-is-submitting');
    expect(formIsSubmitting).toHaveTextContent('true');
  });

  it('MessageForm に error を渡す', () => {
    const error = new Error('Test error');

    render(<MessageModal {...defaultProps} error={error} />);

    const formError = screen.getByTestId('form-error');
    expect(formError).toHaveTextContent('has-error');
  });

  it('MessageForm の onCancel が呼ばれたとき onOpenChange(false) を呼ぶ', () => {
    render(<MessageModal {...defaultProps} />);

    const cancelButton = screen.getByText('Cancel');
    cancelButton.click();

    expect(mockOnOpenChange).toHaveBeenCalledWith(false);
  });

  it('MessageForm の onSubmit が呼ばれたとき onSubmit を呼ぶ', () => {
    render(<MessageModal {...defaultProps} />);

    const submitButton = screen.getByText('Submit');
    submitButton.click();

    expect(mockOnSubmit).toHaveBeenCalledWith({ code: 'TEST', content: 'Test' });
  });

  it('open=false のときモーダルを表示しない', () => {
    render(<MessageModal {...defaultProps} open={false} />);

    // Dialog が閉じているときは MessageForm も表示されない
    expect(screen.queryByTestId('message-form')).not.toBeInTheDocument();
  });

  it('isSubmitting がデフォルトで false', () => {
    render(<MessageModal {...defaultProps} />);

    const formIsSubmitting = screen.getByTestId('form-is-submitting');
    expect(formIsSubmitting).toHaveTextContent('false');
  });
});
