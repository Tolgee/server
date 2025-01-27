import { default as React, FunctionComponent } from 'react';
import { T } from '@tolgee/react';
import {
  NotificationItem,
  NotificationItemProps,
} from 'tg.component/layout/Notifications/NotificationItem';
import { LINKS } from 'tg.constants/links';

type MfaEnabledItemProps = NotificationItemProps;

export const MfaEnabledItem: FunctionComponent<MfaEnabledItemProps> = ({
  notification,
  ...props
}) => {
  return (
    <NotificationItem
      notification={notification}
      destinationUrl={LINKS.USER_ACCOUNT_SECURITY}
      {...props}
    >
      <b>{notification.originatingUser?.name}</b>&nbsp;
      <T keyName="notifications-mfa-enabled" />
    </NotificationItem>
  );
};
