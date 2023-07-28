import { useTranslate } from '@tolgee/react';
import { components } from 'tg.service/apiSchema.generated';

type BatchJobType = components['schemas']['BatchJobModel']['type'];

export function useBatchOperationTypeTranslate() {
  const { t } = useTranslate();

  return (type: BatchJobType) => {
    switch (type) {
      case 'DELETE_KEYS':
        return t('batch_operation_type_delete_keys');
      case 'MACHINE_TRANSLATE':
        return t('batch_operation_type_machine_translation');
      case 'PRE_TRANSLATE_BY_MT':
        return t('batch_operation_type_machine_pre_translate_by_tm');
      case 'COPY_TRANSLATIONS':
        return t('batch_operation_type_copy_translations');
      case 'CLEAR_TRANSLATIONS':
        return t('batch_operation_type_clear_translations');
      case 'SET_TRANSLATIONS_STATE':
        return t('batch_operation_type_set_translations_state');
      case 'SET_KEYS_NAMESPACE':
        return t('batch_operation_type_set_keys_namespace');
      case 'TAG_KEYS':
        return t('batch_operation_type_tag_keys');
      case 'UNTAG_KEYS':
        return t('batch_operation_type_untag_keys');
      default:
        return type;
    }
  };
}
