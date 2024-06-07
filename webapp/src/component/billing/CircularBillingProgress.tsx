import clsx from 'clsx';
import { styled } from '@mui/material';

import { BILLING_CRITICAL_FRACTION } from './constants';

const RADIUS = 45;
const CIRCUIT = RADIUS * Math.PI * 2;

const StyledCircleBackground = styled('circle')`
  fill: none;
  stroke-width: 17px;
  stroke: ${({ theme }) => theme.palette.billingProgress.background};
  &.extra {
    stroke: transparent;
  }
`;

const StyledCircleContent = styled('circle')`
  fill: none;
  stroke-width: 17px;
  stroke-linecap: round;
  transform-origin: 50% 50%;
  stroke-dasharray: ${CIRCUIT};
  stroke: ${({ theme }) => theme.palette.billingProgress.sufficient};
  &.critical {
    stroke: ${({ theme }) => theme.palette.billingProgress.low};
  }
`;

const StyledCircleContentOver = styled(StyledCircleContent)`
  stroke-width: 17px;
  stroke-linecap: unset;
  stroke-linecap: round;
  stroke: ${({ theme }) => theme.palette.billingProgress.overForbidden};
  &.canGoOver {
    stroke: ${({ theme }) => theme.palette.billingProgress.over};
  }
`;

type Props = {
  value: number;
  maxValue: number;
  canGoOver: boolean;
  size?: number;
};

export const CircularBillingProgress = ({
  value,
  maxValue = 100,
  canGoOver,
  size = 28,
}: Props) => {
  const normalized = value > maxValue ? maxValue : value < 0 ? 0 : value;
  const critical =
    normalized > BILLING_CRITICAL_FRACTION * maxValue && !canGoOver;

  const extra = value > maxValue ? value - maxValue : 0;

  const fullLength = value > maxValue ? value : maxValue;
  let progressLength = CIRCUIT - (normalized / fullLength) * CIRCUIT;
  let extraProgressLength = CIRCUIT - (extra / fullLength) * CIRCUIT;
  let rotation = 0;

  if (extra) {
    // make bars separated
    progressLength += 20;
    extraProgressLength += 20;
    rotation = 12;
  }

  return (
    <svg viewBox="0 0 114 114" style={{ width: size, height: size }}>
      <StyledCircleBackground
        className={clsx({ extra })}
        cx="57"
        cy="57"
        r={RADIUS}
      />
      <StyledCircleContent
        className={clsx({ critical })}
        cx="57"
        cy="57"
        r={RADIUS}
        sx={{
          strokeDashoffset: progressLength,
          transform: `rotate(${-90 + rotation}deg)`,
        }}
      />
      {extra && (
        <StyledCircleContentOver
          cx="57"
          cy="57"
          r={RADIUS}
          sx={{
            strokeDashoffset: extraProgressLength,
            transform: `scale(1, -1) rotate(${90 + rotation}deg)`,
          }}
          className={clsx({ canGoOver })}
        />
      )}
    </svg>
  );
};
