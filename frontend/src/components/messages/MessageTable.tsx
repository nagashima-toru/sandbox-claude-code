'use client';

import { useGetAllMessages } from '@/lib/api/generated/message/message';
import { MessageResponse } from '@/lib/api/generated/models';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Edit, Trash2 } from 'lucide-react';

interface MessageTableProps {
  onEdit?: (message: MessageResponse) => void;
  onDelete?: (message: MessageResponse) => void;
}

export default function MessageTable({ onEdit, onDelete }: MessageTableProps) {
  const { data: messages, isLoading, error } = useGetAllMessages();

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <Loading size="lg" text="Loading messages..." />
      </div>
    );
  }

  if (error) {
    return (
      <ErrorMessage
        message="Failed to load messages. Please try again later."
        variant="card"
      />
    );
  }

  if (!messages || messages.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground text-lg">No messages found.</p>
        <p className="text-muted-foreground text-sm mt-2">
          Create your first message to get started.
        </p>
      </div>
    );
  }

  return (
    <div className="border rounded-lg">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[100px]">ID</TableHead>
            <TableHead className="w-[200px]">Code</TableHead>
            <TableHead>Content</TableHead>
            <TableHead className="w-[150px] text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {messages.map((message: MessageResponse) => (
            <TableRow key={message.id}>
              <TableCell className="font-medium">{message.id}</TableCell>
              <TableCell>
                <code className="bg-muted px-2 py-1 rounded text-sm">
                  {message.code}
                </code>
              </TableCell>
              <TableCell className="max-w-md truncate">
                {message.content}
              </TableCell>
              <TableCell className="text-right">
                <div className="flex justify-end gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onEdit?.(message)}
                    title="Edit message"
                  >
                    <Edit className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onDelete?.(message)}
                    title="Delete message"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
