/* eslint-disable no-restricted-imports */
export { TaskReference } from '../ee/task/components/TaskReference';
export { GlobalLimitPopover } from '../ee/billing/limitPopover/GlobalLimitPopover';
export { Usage } from '../ee/billing/component/Usage';
export { TranslationTaskIndicator } from '../ee/task/components/TranslationTaskIndicator';
export { PermissionsAdvancedEe } from '../ee/PermissionsAdvanced/PermissionsAdvancedEe';
export { TranslationsTaskDetail } from '../ee/task/components/TranslationsTaskDetail';
export { PrefilterTask } from '../ee/task/components/PrefilterTask';

import React from 'react';
import { addUserMenuItems } from '../component/security/UserMenu/UserMenuItems';
import { BillingMenuItem } from '../ee/billing/component/UserMenu/BillingMenuItem';
import { PrivateRoute } from '../component/common/PrivateRoute';
import { LINKS } from '../constants/links';
import { MyTasksView } from '../ee/task/views/myTasks/MyTasksView';
import { useGlobalContext } from '../globalContext/GlobalContext';
import { useUserTasks } from '../globalContext/useUserTasks';
import { AdministrationCloudPlansView } from '../ee/billing/administration/AdministrationCloudPlansView';
import { AdministrationCloudPlanCreateView } from '../ee/billing/administration/AdministrationCloudPlanCreateView';
import { AdministrationCloudPlanEditView } from '../ee/billing/administration/AdministrationCloudPlanEditView';
import { AdministrationEePlansView } from '../ee/billing/administration/AdministrationEePlansView';
import { AdministrationEePlanCreateView } from '../ee/billing/administration/AdministrationEePlanCreateView';
import { AdministrationEePlanEditView } from '../ee/billing/administration/AdministrationEePlanEditView';
import { AdministrationEeLicenseView } from '../ee/billing/administration/AdministrationEeLicenseView';
import { SlackApp } from '../ee/organizationApps/SlackApp';
import { useConfig, useEnabledFeatures } from '../globalContext/helpers';
import { OrganizationSubscriptionsView } from '../ee/billing/Subscriptions/OrganizationSubscriptionsView';
import { OrganizationInvoicesView } from '../ee/billing/Invoices/OrganizationInvoicesView';
import { OrganizationBillingView } from '../ee/billing/OrganizationBillingView';
import { OrganizationBillingTestClockHelperView } from '../ee/billing/OrganizationBillingTestClockHelperView';
import { Link, Route, Switch } from 'react-router-dom';
import { ProjectTasksView } from '../ee/task/views/projectTasks/ProjectTasksView';
import { addOperations } from '../views/projects/translations/BatchOperations/operations';
import { OperationTaskCreate } from '../ee/batchOperations/OperationTaskCreate';
import { OperationTaskAddKeys } from '../ee/batchOperations/OperationTaskAddKeys';
import { OperationTaskRemoveKeys } from '../ee/batchOperations/OperationTaskRemoveKeys';
import { useTranslationsSelector } from '../views/projects/translations/context/TranslationsContext';
import { useProjectPermissions } from '../hooks/useProjectPermissions';
import { T, useTranslate } from '@tolgee/react';
import { addPanel } from '../views/projects/translations/ToolsPanel/panelsList';
import { ClipboardCheck } from '@untitled-ui/icons-react';
import { tasksCount, TasksPanel } from '../ee/task/components/TasksPanel';
import { addDeveloperViewItems } from '../views/projects/developer/developerViewItems';
import { StorageList } from '../ee/developer/storage/StorageList';
import { WebhookList } from '../ee/developer/webhook/WebhookList';
import { Badge, Box, MenuItem } from '@mui/material';
import { addProjectMenuItems } from '../views/projects/projectMenu/ProjectMenu';
import { addAdministrationMenuItems } from '../views/administration/components/BaseAdministrationView';

export const billingMenuItems = [BillingMenuItem];
export const apps = [SlackApp];

export const routes = {
  Root: () => {
    return (
      <Switch>
        <PrivateRoute exact path={LINKS.MY_TASKS.template}>
          <MyTasksView />
        </PrivateRoute>
      </Switch>
    );
  },
  Administration: () => (
    <Switch>
      <PrivateRoute exact path={LINKS.ADMINISTRATION_EE_LICENSE.template}>
        <AdministrationEeLicenseView />
      </PrivateRoute>
      <PrivateRoute
        exact
        path={LINKS.ADMINISTRATION_BILLING_CLOUD_PLANS.template}
      >
        <AdministrationCloudPlansView />
      </PrivateRoute>
      <PrivateRoute
        exact
        path={LINKS.ADMINISTRATION_BILLING_CLOUD_PLAN_CREATE.template}
      >
        <AdministrationCloudPlanCreateView />
      </PrivateRoute>
      <PrivateRoute
        exact
        path={LINKS.ADMINISTRATION_BILLING_CLOUD_PLAN_EDIT.template}
      >
        <AdministrationCloudPlanEditView />
      </PrivateRoute>
      <PrivateRoute exact path={LINKS.ADMINISTRATION_BILLING_EE_PLANS.template}>
        <AdministrationEePlansView />
      </PrivateRoute>
      <PrivateRoute
        exact
        path={LINKS.ADMINISTRATION_BILLING_EE_PLAN_CREATE.template}
      >
        <AdministrationEePlanCreateView />
      </PrivateRoute>
      <PrivateRoute
        exact
        path={LINKS.ADMINISTRATION_BILLING_EE_PLAN_EDIT.template}
      >
        <AdministrationEePlanEditView />
      </PrivateRoute>
    </Switch>
  ),
  Organization: () => {
    const config = useConfig();
    return (
      <>
        {config.billing.enabled && (
          <Switch>
            <PrivateRoute path={LINKS.ORGANIZATION_SUBSCRIPTIONS.template}>
              <OrganizationSubscriptionsView />
            </PrivateRoute>
            <PrivateRoute path={LINKS.ORGANIZATION_INVOICES.template}>
              <OrganizationInvoicesView />
            </PrivateRoute>
            <PrivateRoute path={LINKS.ORGANIZATION_BILLING.template}>
              <OrganizationBillingView />
            </PrivateRoute>
            {config.internalControllerEnabled && (
              <PrivateRoute
                path={LINKS.ORGANIZATION_BILLING_TEST_CLOCK_HELPER.template}
              >
                <OrganizationBillingTestClockHelperView />
              </PrivateRoute>
            )}
          </Switch>
        )}
      </>
    );
  },
  Project: () => (
    <Switch>
      <Route path={LINKS.PROJECT_TASKS.template}>
        <ProjectTasksView />
      </Route>
    </Switch>
  ),
};

export function useUserTaskCount() {
  const userInfo = useGlobalContext((context) => context.initialData.userInfo);
  const loadable = useUserTasks({ enabled: !!userInfo });
  return loadable.data?.page?.totalElements ?? 0;
}

export const useAddBatchOperations = () => {
  const { satisfiesPermission } = useProjectPermissions();
  const prefilteredTask = useTranslationsSelector(
    (c) => c.prefilter?.task !== undefined
  );
  const { features } = useEnabledFeatures();
  const canEditTasks = satisfiesPermission('tasks.edit');
  const taskFeature = features.includes('TASKS');
  const { t } = useTranslate();

  return addOperations(
    [
      {
        id: 'task_create',
        label: t('batch_operations_create_task'),
        divider: true,
        enabled: canEditTasks,
        hidden: !taskFeature,
        component: OperationTaskCreate,
      },
      {
        id: 'task_add_keys',
        label: t('batch_operations_task_add_keys'),
        enabled: canEditTasks,
        hidden: prefilteredTask || !taskFeature,
        component: OperationTaskAddKeys,
      },
      {
        id: 'task_remove_keys',
        label: t('batch_operations_task_remove_keys'),
        enabled: canEditTasks,
        hidden: !prefilteredTask || !taskFeature,
        component: OperationTaskRemoveKeys,
      },
    ],
    { position: 'after', value: 'export_translations' }
  );
};

export const translationPanelAdder = addPanel(
  [
    {
      id: 'tasks',
      icon: <ClipboardCheck />,
      name: <T keyName="translation_tools_tasks" />,
      component: TasksPanel,
      itemsCountFunction: tasksCount,
      displayPanel: ({ projectPermissions }) =>
        projectPermissions.satisfiesPermission('tasks.view'),
      hideWhenCountZero: true,
      hideCount: true,
    },
  ],
  { position: 'after', value: 'history' }
);

export const useAddDeveloperViewItems = () => {
  const { t } = useTranslate();
  return addDeveloperViewItems(
    [
      {
        value: 'storage',
        tab: {
          label: t('developer_menu_storage'),
          dataCy: 'developer-menu-storage',
          condition: ({ satisfiesPermission }) =>
            satisfiesPermission('content-delivery.manage'),
        },
        link: LINKS.PROJECT_STORAGE,
        component: StorageList,
      },
      {
        value: 'webhooks',
        tab: {
          label: t('developer_menu_webhooks'),
          dataCy: 'developer-menu-webhooks',
          condition: ({ satisfiesPermission }) =>
            satisfiesPermission('webhooks.manage'),
        },
        link: LINKS.PROJECT_WEBHOOKS,
        component: WebhookList,
      },
    ],
    { position: 'after', value: 'content-delivery' }
  );
};

export const useAddUserMenuItems = () => {
  const taskCount = useUserTaskCount();
  return addUserMenuItems(
    [
      {
        Component: ({ onClose }) => {
          const { t } = useTranslate();

          return (
            <MenuItem
              component={Link}
              to={LINKS.MY_TASKS.build()}
              selected={location.pathname === LINKS.MY_TASKS.build()}
              onClick={onClose}
              data-cy="user-menu-my-tasks"
              sx={{
                display: 'flex',
                justifyContent: 'space-between',
                paddingRight: 3,
              }}
            >
              <Box>{t('user_menu_my_tasks')}</Box>
              <Badge badgeContent={taskCount} color="primary" />
            </MenuItem>
          );
        },
        enabled: true,
        id: 'mu-tasks',
      },
    ],
    { position: 'start' }
  );
};

export const useAddProjectMenuItems = () => {
  const { t } = useTranslate();

  return addProjectMenuItems(
    [
      {
        id: 'tasks',
        condition: ({ satisfiesPermission }) =>
          satisfiesPermission('tasks.view'),
        link: LINKS.PROJECT_TASKS,
        icon: ClipboardCheck,
        text: t('project_menu_tasks'),
        dataCy: 'project-menu-item-tasks',
        matchAsPrefix: true,
      },
    ],
    {
      position: 'after',
      value: 'translations',
    }
  );
};

export const useAddAdministrationMenuItems = () => {
  const { t } = useTranslate();
  const config = useConfig();

  return addAdministrationMenuItems(
    [
      {
        id: 'license',
        link: LINKS.ADMINISTRATION_EE_LICENSE,
        label: t('administration_ee_license'),
        condition: () => true,
      },
      {
        id: 'cloud_plans',
        link: LINKS.ADMINISTRATION_BILLING_CLOUD_PLANS,
        label: t('administration_cloud_plans'),
        condition: () => config.billing.enabled,
      },
      {
        id: 'self_hosted_plans',
        link: LINKS.ADMINISTRATION_BILLING_EE_PLANS,
        label: t('administration_ee_plans'),
        condition: () => config.billing.enabled,
      },
    ],
    { position: 'after', value: 'users' }
  );
};
