import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { MessageTableHeader } from '@/components/messages/MessageTableHeader';

describe('MessageTableHeader', () => {
  it('すべてのカラムヘッダーが表示される', () => {
    const onSort = vi.fn();

    render(
      <table>
        <MessageTableHeader sortField="id" sortDirection="asc" onSort={onSort} />
      </table>
    );

    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Code')).toBeInTheDocument();
    expect(screen.getByText('Content')).toBeInTheDocument();
    expect(screen.getByText('Actions')).toBeInTheDocument();
  });

  it('IDカラムクリックで onSort が呼ばれる', async () => {
    const user = userEvent.setup();
    const onSort = vi.fn();

    render(
      <table>
        <MessageTableHeader sortField="code" sortDirection="asc" onSort={onSort} />
      </table>
    );

    const idButton = screen.getByRole('button', { name: /sort by id/i });
    await user.click(idButton);

    expect(onSort).toHaveBeenCalledWith('id');
    expect(onSort).toHaveBeenCalledTimes(1);
  });

  it('Codeカラムクリックで onSort が呼ばれる', async () => {
    const user = userEvent.setup();
    const onSort = vi.fn();

    render(
      <table>
        <MessageTableHeader sortField="id" sortDirection="asc" onSort={onSort} />
      </table>
    );

    const codeButton = screen.getByRole('button', { name: /sort by code/i });
    await user.click(codeButton);

    expect(onSort).toHaveBeenCalledWith('code');
    expect(onSort).toHaveBeenCalledTimes(1);
  });

  it('Contentカラムクリックで onSort が呼ばれる', async () => {
    const user = userEvent.setup();
    const onSort = vi.fn();

    render(
      <table>
        <MessageTableHeader sortField="id" sortDirection="asc" onSort={onSort} />
      </table>
    );

    const contentButton = screen.getByRole('button', { name: /sort by content/i });
    await user.click(contentButton);

    expect(onSort).toHaveBeenCalledWith('content');
    expect(onSort).toHaveBeenCalledTimes(1);
  });

  it('選択中のカラムにはaria-pressed=trueが設定される', () => {
    const onSort = vi.fn();

    render(
      <table>
        <MessageTableHeader sortField="code" sortDirection="asc" onSort={onSort} />
      </table>
    );

    const idButton = screen.getByRole('button', { name: /sort by id/i });
    const codeButton = screen.getByRole('button', { name: /sort by code/i });
    const contentButton = screen.getByRole('button', { name: /sort by content/i });

    expect(idButton).toHaveAttribute('aria-pressed', 'false');
    expect(codeButton).toHaveAttribute('aria-pressed', 'true');
    expect(contentButton).toHaveAttribute('aria-pressed', 'false');
  });

  it('昇順の場合、適切なaria-labelが設定される', () => {
    const onSort = vi.fn();

    render(
      <table>
        <MessageTableHeader sortField="id" sortDirection="asc" onSort={onSort} />
      </table>
    );

    const idButton = screen.getByRole('button', {
      name: /sort by id, currently ascending/i,
    });
    expect(idButton).toBeInTheDocument();
  });

  it('降順の場合、適切なaria-labelが設定される', () => {
    const onSort = vi.fn();

    render(
      <table>
        <MessageTableHeader sortField="code" sortDirection="desc" onSort={onSort} />
      </table>
    );

    const codeButton = screen.getByRole('button', {
      name: /sort by code, currently descending/i,
    });
    expect(codeButton).toBeInTheDocument();
  });

  it('未選択のカラムには基本的なaria-labelが設定される', () => {
    const onSort = vi.fn();

    render(
      <table>
        <MessageTableHeader sortField="id" sortDirection="asc" onSort={onSort} />
      </table>
    );

    const codeButton = screen.getByRole('button', { name: 'Sort by Code' });
    const contentButton = screen.getByRole('button', { name: 'Sort by Content' });

    expect(codeButton).toBeInTheDocument();
    expect(contentButton).toBeInTheDocument();
  });

  it('sortFieldとsortDirectionを変更すると表示が更新される', () => {
    const onSort = vi.fn();
    const { rerender } = render(
      <table>
        <MessageTableHeader sortField="id" sortDirection="asc" onSort={onSort} />
      </table>
    );

    // 初期状態: ID昇順
    expect(
      screen.getByRole('button', { name: /sort by id, currently ascending/i })
    ).toBeInTheDocument();

    // 再レンダリング: Code降順
    rerender(
      <table>
        <MessageTableHeader sortField="code" sortDirection="desc" onSort={onSort} />
      </table>
    );

    expect(
      screen.getByRole('button', { name: /sort by code, currently descending/i })
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Sort by ID' })).toBeInTheDocument();
  });
});
