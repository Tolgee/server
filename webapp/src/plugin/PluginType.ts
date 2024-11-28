import {
  HierarchyItem,
  LanguageModel,
  PermissionAdvancedState,
} from 'tg.component/PermissionsSettings/types';
import { FC } from 'react';
import { components } from 'tg.service/apiSchema.generated';
import { BatchOperationAdder } from 'tg.views/projects/translations/BatchOperations/operations';
import { addPanelAfter } from 'tg.views/projects/translations/ToolsPanel/panelsList';

export interface PluginType {
  ee?: {
    PermissionsAdvanced: FC<PermissionsAdvancedEeProps>;
    billing: {
      billingMenuItems: FC<BillingMenuItemsProps>[];
      GlobalLimitPopover: FC;
    };
    activity: {
      TaskReference: FC<TaskReferenceProps>;
    };
    organization: {
      apps: FC;
      Usage: FC;
    };
    tasks: {
      useUserTaskCount: () => number;
      TranslationTaskIndicator: FC<TranslationTaskIndicatorProps>;
      PrefilterTask: FC<PrefilterTaskProps>;
      TranslationsTaskDetail: FC;
    };
    routes: {
      Root: FC;
      Administration: FC;
      Organization: FC;
      Project: FC;
    };
    useAddBatchOperations: () => BatchOperationAdder;
    translationPanelAdder: ReturnType<typeof addPanelAfter>;
  };
}

export type PermissionsAdvancedEeProps = {
  dependencies: HierarchyItem;
  state: PermissionAdvancedState;
  onChange: (value: PermissionAdvancedState) => void;
  allLangs?: LanguageModel[];
};

export type BillingMenuItemsProps = {
  onClose: () => void;
};

export type TaskModel = components['schemas']['TaskModel'];

export type TaskReferenceData = {
  type: 'task';
  name: string;
  taskType: TaskModel['type'];
  number: number;
};

export type TaskReferenceProps = {
  data: TaskReferenceData;
};

export type TranslationTaskIndicatorProps = {
  task?: components['schemas']['KeyTaskViewModel'];
};

export type PrefilterTaskProps = {
  taskNumber: number;
};