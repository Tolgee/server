import { FC } from 'react';
import { PlanEstimatedCostsArea } from 'tg.component/billing/plan/Plan';
import { EstimatedCosts, EstimatedCostsProps } from './EstimatedCosts';

export const PlanUsageEstimatedCosts: FC<EstimatedCostsProps> = (props) => {
  return (
    <PlanEstimatedCostsArea>
      <EstimatedCosts {...props} />
    </PlanEstimatedCostsArea>
  );
};
