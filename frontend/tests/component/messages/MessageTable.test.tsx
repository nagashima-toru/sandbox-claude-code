import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import MessageTable from '@/components/messages/MessageTable';
import { MessageResponse } from '@/lib/api/generated/models';
import * as messageApi from '@/lib/api/generated/message/message';

// Mock the API module
vi.mock('@/lib/api/generated/message/message', () => ({
  useGetAllMessages: vi.fn(),
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );

  Wrapper.displayName = 'QueryClientWrapper';

  return Wrapper;
};

describe('MessageTable', () => {
  const mockMessages: MessageResponse[] = [
    { id: 1, code: 'MSG001', content: 'First message' },
    { id: 2, code: 'MSG002', content: 'Second message' },
    { id: 3, code: 'MSG003', content: 'Third message' },
  ];

  const mockOnEdit = vi.fn();
  const mockOnDelete = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('データ表示', () => {
    it('メッセージ一覧が正しく表示される', () => {
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.getAllByText('MSG001').length).toBeGreaterThan(0);
      expect(screen.getAllByText('First message').length).toBeGreaterThan(0);
      expect(screen.getAllByText('MSG002').length).toBeGreaterThan(0);
      expect(screen.getAllByText('Second message').length).toBeGreaterThan(0);
      expect(screen.getAllByText('MSG003').length).toBeGreaterThan(0);
      expect(screen.getAllByText('Third message').length).toBeGreaterThan(0);
    });

    it('IDが表示される', () => {
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.getAllByText('1').length).toBeGreaterThan(0);
      expect(screen.getAllByText('2').length).toBeGreaterThan(0);
      expect(screen.getAllByText('3').length).toBeGreaterThan(0);
    });

    it('編集ボタンと削除ボタンが表示される', () => {
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      const editButtons = screen.getAllByLabelText(/edit message/i);
      const deleteButtons = screen.getAllByLabelText(/delete message/i);

      // Both desktop and mobile views are rendered, so expect 2x the number of messages
      expect(editButtons).toHaveLength(mockMessages.length * 2);
      expect(deleteButtons).toHaveLength(mockMessages.length * 2);
    });

    it('検索バーが表示される', () => {
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.getByPlaceholderText(/search by code or content/i)).toBeInTheDocument();
    });

    it('ページネーションが表示される', () => {
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.getByText(/showing 1 to 3 of 3 results/i)).toBeInTheDocument();
    });
  });

  describe('ローディング状態', () => {
    it('ローディング中はローディングインジケーターを表示する', () => {
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: undefined,
        isLoading: true,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.getByText(/loading messages/i)).toBeInTheDocument();
    });

    it('ローディング中はメッセージテーブルを表示しない', () => {
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: undefined,
        isLoading: true,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.queryByRole('table')).not.toBeInTheDocument();
    });
  });

  describe('エラー状態', () => {
    it('エラー発生時はエラーメッセージを表示する', () => {
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: undefined,
        isLoading: false,
        error: new Error('Failed to fetch'),
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.getByText(/failed to load messages/i)).toBeInTheDocument();
    });

    it('エラー発生時はメッセージテーブルを表示しない', () => {
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: undefined,
        isLoading: false,
        error: new Error('Failed to fetch'),
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.queryByRole('table')).not.toBeInTheDocument();
    });
  });

  describe('空の状態', () => {
    it('メッセージが0件の場合、空のメッセージを表示する', () => {
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: [],
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      expect(screen.getByText(/no messages found/i)).toBeInTheDocument();
      expect(screen.getByText(/create your first message to get started/i)).toBeInTheDocument();
    });
  });

  describe('検索機能', () => {
    it('検索クエリでフィルタリングできる', async () => {
      const user = userEvent.setup();
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      const searchInput = screen.getByPlaceholderText(/search by code or content/i);
      await user.type(searchInput, 'MSG001');

      await waitFor(() => {
        expect(screen.getAllByText('MSG001').length).toBeGreaterThan(0);
        expect(screen.queryByText('MSG002')).not.toBeInTheDocument();
        expect(screen.queryByText('MSG003')).not.toBeInTheDocument();
      });
    });

    it('検索結果が0件の場合、メッセージを表示する', async () => {
      const user = userEvent.setup();
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      const searchInput = screen.getByPlaceholderText(/search by code or content/i);
      await user.type(searchInput, 'NOTFOUND');

      await waitFor(() => {
        expect(screen.getByText(/no messages match your search/i)).toBeInTheDocument();
      });
    });

    it('contentで検索できる', async () => {
      const user = userEvent.setup();
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      const searchInput = screen.getByPlaceholderText(/search by code or content/i);
      await user.type(searchInput, 'First');

      await waitFor(() => {
        expect(screen.getAllByText('First message').length).toBeGreaterThan(0);
        expect(screen.queryByText('Second message')).not.toBeInTheDocument();
        expect(screen.queryByText('Third message')).not.toBeInTheDocument();
      });
    });
  });

  describe('ソート機能', () => {
    it('IDでソートできる', async () => {
      const user = userEvent.setup();
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      const idHeader = screen.getByRole('button', { name: /id/i });
      await user.click(idHeader);

      // 降順にソート
      const rows = screen.getAllByRole('row');
      const firstDataRow = within(rows[1]);
      expect(firstDataRow.getAllByText('3').length).toBeGreaterThan(0);
    });

    it('Codeでソートできる', async () => {
      const user = userEvent.setup();
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: [
          { id: 1, code: 'CCC', content: 'Content C' },
          { id: 2, code: 'AAA', content: 'Content A' },
          { id: 3, code: 'BBB', content: 'Content B' },
        ],
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      const codeHeader = screen.getByRole('button', { name: /code/i });
      await user.click(codeHeader);

      await waitFor(() => {
        const rows = screen.getAllByRole('row');
        const firstDataRow = within(rows[1]);
        expect(firstDataRow.getAllByText('AAA').length).toBeGreaterThan(0);
      });
    });
  });

  describe('ボタンアクション', () => {
    it('編集ボタンをクリックするとonEditが呼ばれる', async () => {
      const user = userEvent.setup();
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      const editButtons = screen.getAllByLabelText(/edit message/i);
      await user.click(editButtons[0]);

      expect(mockOnEdit).toHaveBeenCalledWith(mockMessages[0]);
    });

    it('削除ボタンをクリックするとonDeleteが呼ばれる', async () => {
      const user = userEvent.setup();
       
      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: mockMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnDelete} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      const deleteButtons = screen.getAllByLabelText(/delete message/i);
      await user.click(deleteButtons[0]);

      expect(mockOnDelete).toHaveBeenCalledWith(mockMessages[0]);
    });
  });

  describe('ページネーション', () => {
    it('10件以上のデータがある場合、ページネーションが機能する', async () => {
      const user = userEvent.setup();
      const manyMessages: MessageResponse[] = Array.from({ length: 15 }, (_, i) => ({
        id: i + 1,
        code: `MSG${String(i + 1).padStart(3, '0')}`,
        content: `Message ${i + 1}`,
      }));

      vi.mocked(messageApi.useGetAllMessages).mockReturnValue({
        data: manyMessages,
        isLoading: false,
        error: null,
      } as any);

      render(<MessageTable onEdit={mockOnEdit} onDelete={mockOnDelete} />, {
        wrapper: createWrapper(),
      });

      // 最初のページには10件表示
      expect(screen.getAllByText('MSG001').length).toBeGreaterThan(0);
      expect(screen.getAllByText('MSG010').length).toBeGreaterThan(0);
      expect(screen.queryByText('MSG011')).not.toBeInTheDocument();

      // 次のページに移動
      const nextButton = screen.getByRole('button', { name: /next/i });
      await user.click(nextButton);

      await waitFor(() => {
        expect(screen.queryByText('MSG001')).not.toBeInTheDocument();
        expect(screen.getAllByText('MSG011').length).toBeGreaterThan(0);
        expect(screen.getAllByText('MSG015').length).toBeGreaterThan(0);
      });
    });
  });
});
