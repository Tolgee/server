import { FC } from 'react';
import { components } from 'tg.service/billingApiSchema.generated';
import { SelfHostedEeSubscriptionActions } from '../../SelfHostedEeSubscriptionActions';
import { IncludedFeatures } from './IncludedFeatures';
import { Plan, PlanContent, PlanSubtitle } from '../common/Plan';
import { PlanPrice } from '../cloud/Plans/PlanPrice';
import { PlanInfoArea } from '../common/PlanInfo';
import { SelfHostedEeEstimatedCosts } from './SelfHostedEeEstimatedCosts';
import { ActivePlanTitle } from './ActivePlanTitle';
import { useTranslate } from '@tolgee/react';
import { Box } from '@mui/material';
import { PlanDescription } from './PlanDescription';

type SelfHostedEeSubscriptionModel =
  components['schemas']['SelfHostedEeSubscriptionModel'];

type Props = {
  subscription: SelfHostedEeSubscriptionModel;
  isNew: boolean;
};

export const SelfHostedEeActiveSubscription: FC<Props> = ({
  subscription,
  isNew,
}) => {
  const period = subscription.currentBillingPeriod;
  const { t } = useTranslate();

  const hasPrice = Boolean(
    subscription.plan.prices.subscriptionMonthly ||
      subscription.plan.prices.subscriptionYearly
  );

  return (
    <Plan
      sx={(theme) => ({
        border: `1px solid #c39dae`,
      })}
      data-cy="self-hosted-ee-active-plan"
    >
      <PlanContent>
        {isNew && <PlanSubtitle>{t('billing_subscription_new')}</PlanSubtitle>}
        <ActivePlanTitle
          name={subscription.plan.name}
          status={subscription.status}
          createdAt={subscription.createdAt}
          periodStart={subscription.currentPeriodStart}
          periodEnd={subscription.currentPeriodEnd}
        />

        <SelfHostedEeEstimatedCosts subscription={subscription} />

        <PlanInfoArea>
          <Box>
            <PlanDescription
              free={subscription.plan.free}
              hasPrice={hasPrice}
            />
          </Box>
          <IncludedFeatures
            features={subscription.plan.enabledFeatures}
            includedUsage={subscription.plan.includedUsage}
          />
        </PlanInfoArea>

        <PlanPrice prices={subscription.plan.prices} period={period} />

        <SelfHostedEeSubscriptionActions
          id={subscription.id}
          licenceKey={subscription.licenseKey}
          isNew={isNew}
        />
      </PlanContent>
    </Plan>
  );
};
