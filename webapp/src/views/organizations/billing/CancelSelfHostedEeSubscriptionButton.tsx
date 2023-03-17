import { T, useTranslate } from '@tolgee/react';
import { useBillingApiMutation } from 'tg.service/http/useQueryApi';
import { useSuccessMessage } from 'tg.hooks/useSuccessMessage';
import { useOrganization } from '../useOrganization';
import { confirmation } from 'tg.hooks/confirmation';
import { PlanActionButton } from './Subscriptions/cloud/Plans/PlanActionButton';

export const CancelSelfHostedEeSubscriptionButton = (props: { id: number }) => {
  const { t } = useTranslate();

  const successMessage = useSuccessMessage();

  const organization = useOrganization();

  const cancelMutation = useBillingApiMutation({
    url: '/v2/organizations/{organizationId}/billing/self-hosted-ee/subscriptions/{subscriptionId}',
    method: 'delete',
    invalidatePrefix: '/v2/organizations/{organizationId}/billing',
    options: {
      onSuccess: () => {
        successMessage(
          <T keyName="organization-billing-self-hosted-subscription-cancelled-message" />
        );
      },
    },
  });

  function onClick() {
    confirmation({
      message: (
        <T keyName="organization-billing-self-hosted-subscription-cancel-confirmation" />
      ),
      onConfirm: () => {
        cancelMutation.mutate({
          path: { subscriptionId: props.id, organizationId: organization!.id },
        });
      },
    });
  }

  return (
    <PlanActionButton
      onClick={onClick}
      variant="outlined"
      loading={cancelMutation.isLoading}
    >
      {t('organization-billing-self-hosted-cancel-subscription-button')}
    </PlanActionButton>
  );
};
