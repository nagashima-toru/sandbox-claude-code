'use client';

import { TableCell, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Edit, Trash2 } from 'lucide-react';
import { MessageResponse } from '@/lib/api/generated/models';
import { RoleBasedComponent } from '@/components/common/RoleBasedComponent';
import { ROLES } from '@/lib/constants/roles';

interface MessageTableRowProps {
  message: MessageResponse;
  onEdit?: (message: MessageResponse) => void;
  onDelete?: (message: MessageResponse) => void;
}

export function MessageTableRow({ message, onEdit, onDelete }: MessageTableRowProps) {
  return (
    <TableRow data-testid={`message-row-${message.id}`}>
      <TableCell className="font-medium">{message.id}</TableCell>
      <TableCell>
        <code className="bg-muted px-2 py-1 rounded text-sm">{message.code}</code>
      </TableCell>
      <TableCell className="max-w-md truncate">{message.content}</TableCell>
      <TableCell className="text-right">
        <div className="flex justify-end gap-2">
          <RoleBasedComponent allowedRoles={[ROLES.ADMIN]}>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onEdit?.(message)}
              aria-label={`Edit message ${message.code}`}
              data-testid={`edit-message-button-${message.id}`}
            >
              <Edit className="h-4 w-4" />
            </Button>
          </RoleBasedComponent>
          <RoleBasedComponent allowedRoles={[ROLES.ADMIN]}>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onDelete?.(message)}
              aria-label={`Delete message ${message.code}`}
              data-testid={`delete-message-button-${message.id}`}
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          </RoleBasedComponent>
        </div>
      </TableCell>
    </TableRow>
  );
}

interface MessageCardProps {
  message: MessageResponse;
  onEdit?: (message: MessageResponse) => void;
  onDelete?: (message: MessageResponse) => void;
}

export function MessageCard({ message, onEdit, onDelete }: MessageCardProps) {
  return (
    <div data-testid={`message-row-${message.id}`} className="p-4 space-y-3">
      <div className="flex items-start justify-between">
        <div className="space-y-1 flex-1">
          <div className="flex items-center gap-2">
            <span className="text-xs text-muted-foreground">ID:</span>
            <span className="font-medium">{message.id}</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs text-muted-foreground">Code:</span>
            <code className="bg-muted px-2 py-1 rounded text-sm">{message.code}</code>
          </div>
        </div>
        <div className="flex gap-2">
          <RoleBasedComponent allowedRoles={[ROLES.ADMIN]}>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onEdit?.(message)}
              aria-label={`Edit message ${message.code}`}
              data-testid={`edit-message-button-${message.id}`}
            >
              <Edit className="h-4 w-4" />
            </Button>
          </RoleBasedComponent>
          <RoleBasedComponent allowedRoles={[ROLES.ADMIN]}>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onDelete?.(message)}
              aria-label={`Delete message ${message.code}`}
              data-testid={`delete-message-button-${message.id}`}
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          </RoleBasedComponent>
        </div>
      </div>
      <div>
        <span className="text-xs text-muted-foreground">Content:</span>
        <p className="mt-1 text-sm">{message.content}</p>
      </div>
    </div>
  );
}
