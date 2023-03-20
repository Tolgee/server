import { ComponentProps, default as React, FunctionComponent } from 'react';
import { Box, CircularProgress, Fade, styled } from '@mui/material';
import { T } from '@tolgee/react';

import { SadEmotionMessage, SadEmotionMessageProps } from './SadEmotionMessage';
import { useLoadingRegister } from 'tg.component/GlobalLoading';

const ProgressWrapper = styled('div')`
  position: absolute;
  display: flex;
  top: 0px;
  height: ${(props: any) => props.height || '400px'};
  left: 0;
  right: 0;
  align-items: center;
  justify-content: center;
  pointer-events: none;
`;

type Props = {
  loading?: boolean;
  wrapperProps?: ComponentProps<typeof Box>;
} & SadEmotionMessageProps;

export const EmptyListMessage: FunctionComponent<Props> = ({
  loading,
  children,
  wrapperProps,
  ...otherProps
}) => {
  useLoadingRegister(loading);

  wrapperProps = {
    ...wrapperProps,
    py: wrapperProps?.py || 8,
    'data-cy': wrapperProps?.['data-cy'] || 'empty-list-message',
  } as any;

  return (
    <Box data-cy="global-empty-list" {...wrapperProps}>
      <Fade in={!loading} mountOnEnter unmountOnExit>
        <div>
          <SadEmotionMessage {...otherProps}>
            {children || <T>global_empty_list_message</T>}
          </SadEmotionMessage>
        </div>
      </Fade>
      <Fade in={loading} mountOnEnter unmountOnExit>
        <ProgressWrapper>
          <CircularProgress />
        </ProgressWrapper>
      </Fade>
    </Box>
  );
};
