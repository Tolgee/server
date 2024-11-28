import { useProjectPermissions } from 'tg.hooks/useProjectPermissions';
import { useTranslate } from '@tolgee/react';
import { OperationDelete } from './OperationDelete';
import { OperationMachineTranslate } from './OperationMachineTranslate';
import { OperationPreTranslate } from './OperationPreTranslate';
import { OperationMarkAsTranslated } from './OperationMarkAsTranslated';
import { OperationMarkAsReviewed } from './OperationMarkAsReviewed';
import { OperationAddTags } from './OperationAddTags';
import { OperationRemoveTags } from './OperationRemoveTags';
import { OperationChangeNamespace } from './OperationChangeNamespace';
import { OperationCopyTranslations } from './OperationCopyTranslations';
import { OperationClearTranslations } from './OperationClearTranslations';
import { OperationExportTranslations } from './OperationExportTranslations';
import { FC } from 'react';
import { OperationProps } from './types';
import { getEe } from '../../../../plugin/getEe';

export type BatchOperation = {
  id: string;
  label: string;
  enabled: boolean;
  hidden?: boolean;
  divider?: boolean;
  component: FC<OperationProps>;
};

export function addOperationAfter(
  operations: BatchOperation[],
  afterId: string
) {
  return (existingOperations: BatchOperation[]) => {
    const newOperations: BatchOperation[] = [];
    existingOperations.forEach((operation) => {
      newOperations.push(operation);
      if (operation.id === afterId) {
        newOperations.push(...operations);
      }
    });
    return newOperations;
  };
}

export type BatchOperationAdder = ReturnType<typeof addOperationAfter>;

export const useBatchOperations = () => {
  const { useAddBatchOperations: useAddEeBatchOperations } = getEe();

  const { satisfiesPermission } = useProjectPermissions();

  const { t } = useTranslate();

  const canEditKey = satisfiesPermission('keys.edit');
  const canDeleteKey = satisfiesPermission('keys.delete');
  const canMachineTranslate = satisfiesPermission('translations.batch-machine');
  const canPretranslate = satisfiesPermission('translations.batch-by-tm');
  const canChangeState = satisfiesPermission('translations.state-edit');
  const canViewTranslations = satisfiesPermission('translations.view');
  const canEditTranslations = satisfiesPermission('translations.edit');

  const publicOperations: BatchOperation[] = [
    {
      id: 'machine_translate',
      label: t('batch_operations_machine_translate'),
      enabled: canMachineTranslate,
      component: OperationMachineTranslate,
    },
    {
      id: 'pre_translate',
      label: t('batch_operations_pre_translate'),
      enabled: canPretranslate,
      component: OperationPreTranslate,
    },
    {
      id: 'mark_as_translated',
      label: t('batch_operations_mark_as_translated'),
      enabled: canChangeState,
      component: OperationMarkAsTranslated,
    },
    {
      id: 'mark_as_reviewed',
      label: t('batch_operations_mark_as_reviewed'),
      enabled: canChangeState,
      component: OperationMarkAsReviewed,
    },
    {
      id: 'copy_translations',
      label: t('batch_operations_copy_translations'),
      enabled: canEditTranslations,
      component: OperationCopyTranslations,
    },
    {
      id: 'clear_translations',
      label: t('batch_operation_clear_translations'),
      enabled: canEditTranslations,
      component: OperationClearTranslations,
    },
    {
      id: 'export_translations',
      label: t('batch_operations_export_translations'),
      enabled: canViewTranslations,
      component: OperationExportTranslations,
    },
    {
      id: 'add_tags',
      label: t('batch_operations_add_tags'),
      divider: true,
      enabled: canEditKey,
      component: OperationAddTags,
    },
    {
      id: 'remove_tags',
      label: t('batch_operations_remove_tags'),
      enabled: canEditKey,
      component: OperationRemoveTags,
    },
    {
      id: 'change_namespace',
      label: t('batch_operations_change_namespace'),
      enabled: canEditKey,
      component: OperationChangeNamespace,
    },
    {
      id: 'delete',
      label: t('batch_operations_delete'),
      enabled: canDeleteKey,
      component: OperationDelete,
    },
  ];

  const addEeBatchOperations = useAddEeBatchOperations();

  const operations = addEeBatchOperations(publicOperations);

  function findOperation(id?: string) {
    return operations.find((o) => o.id === id);
  }

  return {
    operations,
    findOperation,
  };
};