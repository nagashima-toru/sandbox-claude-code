import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { MessageTableRow, MessageCard } from '@/components/messages/MessageTableRow';
import { MessageResponse } from '@/lib/api/generated/models';

describe('MessageTableRow', () => {
  const mockMessage: MessageResponse = {
    id: 1,
    code: 'TEST001',
    content: 'Test content',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  };

  it('メッセージ情報が表示される', () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    render(
      <table>
        <tbody>
          <MessageTableRow message={mockMessage} onEdit={onEdit} onDelete={onDelete} />
        </tbody>
      </table>
    );

    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('TEST001')).toBeInTheDocument();
    expect(screen.getByText('Test content')).toBeInTheDocument();
  });

  it('編集ボタンクリックで onEdit が呼ばれる', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    render(
      <table>
        <tbody>
          <MessageTableRow message={mockMessage} onEdit={onEdit} onDelete={onDelete} />
        </tbody>
      </table>
    );

    const editButton = screen.getByRole('button', { name: /edit message TEST001/i });
    await user.click(editButton);

    expect(onEdit).toHaveBeenCalledWith(mockMessage);
    expect(onEdit).toHaveBeenCalledTimes(1);
  });

  it('削除ボタンクリックで onDelete が呼ばれる', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    render(
      <table>
        <tbody>
          <MessageTableRow message={mockMessage} onEdit={onEdit} onDelete={onDelete} />
        </tbody>
      </table>
    );

    const deleteButton = screen.getByRole('button', { name: /delete message TEST001/i });
    await user.click(deleteButton);

    expect(onDelete).toHaveBeenCalledWith(mockMessage);
    expect(onDelete).toHaveBeenCalledTimes(1);
  });

  it('onEdit が未指定の場合でもエラーが発生しない', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <table>
        <tbody>
          <MessageTableRow message={mockMessage} onDelete={onDelete} />
        </tbody>
      </table>
    );

    const editButton = screen.getByRole('button', { name: /edit message TEST001/i });
    await user.click(editButton);

    // エラーが発生しないことを確認
    expect(onDelete).not.toHaveBeenCalled();
  });

  it('onDelete が未指定の場合でもエラーが発生しない', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();

    render(
      <table>
        <tbody>
          <MessageTableRow message={mockMessage} onEdit={onEdit} />
        </tbody>
      </table>
    );

    const deleteButton = screen.getByRole('button', { name: /delete message TEST001/i });
    await user.click(deleteButton);

    // エラーが発生しないことを確認
    expect(onEdit).not.toHaveBeenCalled();
  });
});

describe('MessageCard', () => {
  const mockMessage: MessageResponse = {
    id: 2,
    code: 'MOBILE001',
    content: 'Mobile view content',
    createdAt: '2024-02-01T00:00:00Z',
    updatedAt: '2024-02-01T00:00:00Z',
  };

  it('メッセージ情報が表示される', () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    render(<MessageCard message={mockMessage} onEdit={onEdit} onDelete={onDelete} />);

    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('MOBILE001')).toBeInTheDocument();
    expect(screen.getByText('Mobile view content')).toBeInTheDocument();
    expect(screen.getByText(/ID:/i)).toBeInTheDocument();
    expect(screen.getByText(/Code:/i)).toBeInTheDocument();
    expect(screen.getByText(/Content:/i)).toBeInTheDocument();
  });

  it('編集ボタンクリックで onEdit が呼ばれる', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    render(<MessageCard message={mockMessage} onEdit={onEdit} onDelete={onDelete} />);

    const editButton = screen.getByRole('button', { name: /edit message MOBILE001/i });
    await user.click(editButton);

    expect(onEdit).toHaveBeenCalledWith(mockMessage);
    expect(onEdit).toHaveBeenCalledTimes(1);
  });

  it('削除ボタンクリックで onDelete が呼ばれる', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    render(<MessageCard message={mockMessage} onEdit={onEdit} onDelete={onDelete} />);

    const deleteButton = screen.getByRole('button', { name: /delete message MOBILE001/i });
    await user.click(deleteButton);

    expect(onDelete).toHaveBeenCalledWith(mockMessage);
    expect(onDelete).toHaveBeenCalledTimes(1);
  });

  it('onEdit が未指定の場合でもエラーが発生しない', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(<MessageCard message={mockMessage} onDelete={onDelete} />);

    const editButton = screen.getByRole('button', { name: /edit message MOBILE001/i });
    await user.click(editButton);

    expect(onDelete).not.toHaveBeenCalled();
  });

  it('onDelete が未指定の場合でもエラーが発生しない', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();

    render(<MessageCard message={mockMessage} onEdit={onEdit} />);

    const deleteButton = screen.getByRole('button', { name: /delete message MOBILE001/i });
    await user.click(deleteButton);

    expect(onEdit).not.toHaveBeenCalled();
  });
});
