'use client';

import { useState, useCallback } from 'react';
import { PageHeader } from '@/components/common/PageHeader';
import MessageTable from '@/components/messages/MessageTable';
import MessageModal from '@/components/messages/MessageModal';
import DeleteConfirmDialog from '@/components/messages/DeleteConfirmDialog';
import { Button } from '@/components/ui/button';
import { Plus, LogOut } from 'lucide-react';
import { useMessageMutations } from '@/hooks/useMessageMutations';
import { MessageFormData } from '@/lib/validations/message';
import { MessageResponse } from '@/lib/api/generated/models';
import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';

/**
 * Home page component for message management.
 * Provides CRUD operations (Create, Read, Update, Delete) for messages.
 * Manages modal states for creating, editing, and deleting messages.
 */
export default function Home() {
  const router = useRouter();
  const { logout } = useAuth();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [selectedMessage, setSelectedMessage] = useState<MessageResponse | null>(null);

  const {
    createMessage: createMutation,
    updateMessage: updateMutation,
    deleteMessage: deleteMutation,
  } = useMessageMutations({
    onCreateSuccess: () => {
      setIsCreateModalOpen(false);
    },
    onUpdateSuccess: () => {
      setIsEditModalOpen(false);
      setSelectedMessage(null);
    },
    onDeleteSuccess: () => {
      setIsDeleteDialogOpen(false);
      setSelectedMessage(null);
    },
    onCreateError: (error: unknown) => {
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { status?: number } };
        if (axiosError.response?.status === 409) {
          console.error('Duplicate code error');
        }
      }
    },
    onUpdateError: (error: unknown) => {
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { status?: number } };
        if (axiosError.response?.status === 404) {
          console.error('Message not found');
        }
      }
    },
    onDeleteError: (error: unknown) => {
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { status?: number } };
        if (axiosError.response?.status === 404) {
          console.error('Message not found');
        }
      }
    },
  });

  const handleCreateMessage = useCallback(
    (data: MessageFormData) => {
      createMutation.mutate({ data });
    },
    [createMutation]
  );

  const handleEditMessage = useCallback(
    (data: MessageFormData) => {
      if (selectedMessage?.id) {
        updateMutation.mutate({ id: selectedMessage.id, data });
      }
    },
    [selectedMessage, updateMutation]
  );

  const handleEditClick = useCallback(
    (message: MessageResponse) => {
      updateMutation.reset(); // Clear previous errors
      setSelectedMessage(message);
      setIsEditModalOpen(true);
    },
    [updateMutation]
  );

  const handleDeleteClick = useCallback(
    (message: MessageResponse) => {
      deleteMutation.reset(); // Clear previous errors
      setSelectedMessage(message);
      setIsDeleteDialogOpen(true);
    },
    [deleteMutation]
  );

  const handleDeleteConfirm = useCallback(() => {
    if (selectedMessage?.id) {
      deleteMutation.mutate({ id: selectedMessage.id });
    }
  }, [selectedMessage, deleteMutation]);

  const handleLogout = useCallback(async () => {
    try {
      await logout();
      router.push('/login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  }, [logout, router]);

  return (
    <main className="min-h-screen p-8">
      <div className="max-w-7xl mx-auto space-y-6">
        <PageHeader
          title="Message Management"
          description="Manage all your messages in one place"
          action={
            <Button
              onClick={() => {
                createMutation.reset(); // Clear previous errors
                setIsCreateModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4 mr-2" />
              New Message
            </Button>
          }
          rightContent={
            <Button variant="outline" onClick={handleLogout}>
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          }
        />
        <MessageTable onEdit={handleEditClick} onDelete={handleDeleteClick} />

        <MessageModal
          open={isCreateModalOpen}
          onOpenChange={setIsCreateModalOpen}
          onSubmit={handleCreateMessage}
          isSubmitting={createMutation.isPending}
          mode="create"
          error={createMutation.error}
        />

        <MessageModal
          open={isEditModalOpen}
          onOpenChange={setIsEditModalOpen}
          onSubmit={handleEditMessage}
          initialData={selectedMessage || undefined}
          isSubmitting={updateMutation.isPending}
          mode="edit"
          error={updateMutation.error}
        />

        <DeleteConfirmDialog
          open={isDeleteDialogOpen}
          onOpenChange={setIsDeleteDialogOpen}
          onConfirm={handleDeleteConfirm}
          message={selectedMessage}
          isDeleting={deleteMutation.isPending}
          error={deleteMutation.error}
        />
      </div>
    </main>
  );
}
