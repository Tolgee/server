import { default as React, useEffect } from 'react';
import { List, ListItem, styled } from '@mui/material';
import Menu from '@mui/material/Menu';
import {
  useApiInfiniteQuery,
  useApiMutation,
} from 'tg.service/http/useQueryApi';
import { T } from '@tolgee/react';
import { useGlobalContext } from 'tg.globalContext/GlobalContext';
import { useUser } from 'tg.globalContext/helpers';
import { BoxLoading } from 'tg.component/common/BoxLoading';
import { PopoverProps } from '@mui/material/Popover';
import { notificationComponents } from 'tg.component/layout/Notifications/NotificationTypeMap';
import { NotificationsChanged } from 'tg.websocket-client/WebsocketClient';

const FETCH_NEXT_PAGE_SCROLL_THRESHOLD_IN_PIXELS = 100;

const StyledMenu = styled(Menu)`
  .MuiPaper-root {
    margin-top: 5px;
  }
`;

const StyledListItemHeader = styled(ListItem)`
  font-weight: bold;
`;

type NotificationsPopupProps = {
  onClose: () => void;
  onNotificationsChanged: (event: NotificationsChanged) => void;
  anchorEl: PopoverProps['anchorEl'];
};

export const NotificationsPopup: React.FC<NotificationsPopupProps> = ({
  onClose,
  onNotificationsChanged,
  anchorEl,
}) => {
  const user = useUser();
  const client = useGlobalContext((c) => c.wsClient.client);

  const query = { size: 10 };
  const notificationsLoadable = useApiInfiniteQuery({
    url: '/v2/notifications',
    method: 'get',
    query: query,
    options: {
      enabled: false,
      getNextPageParam: (lastPage) => {
        if (
          lastPage.page &&
          lastPage.page.number! < lastPage.page.totalPages! - 1
        ) {
          return {
            query: {
              ...query,
              page: lastPage.page!.number! + 1,
            },
          };
        } else {
          return null;
        }
      },
    },
  });

  const markSeenMutation = useApiMutation({
    url: '/v2/notifications-mark-seen',
    method: 'put',
  });

  const notifications = notificationsLoadable.data?.pages
    .flatMap((it) => it?._embedded?.notificationModelList)
    .filter((it) => it !== undefined);

  useEffect(() => {
    if (!anchorEl) return;

    const data = notificationsLoadable.data;
    if (!data) {
      if (!notificationsLoadable.isFetching) {
        notificationsLoadable.refetch();
      }
      return;
    }

    const markAsSeenIds = notifications?.map((it) => it.id);
    if (!markAsSeenIds) return;

    markSeenMutation.mutate({
      content: {
        'application/json': {
          notificationIds: markAsSeenIds,
        },
      },
    });
  }, [notificationsLoadable.data, anchorEl]);

  useEffect(() => {
    if (client && user) {
      return client.subscribe(
        `/users/${user.id}/notifications-changed`,
        (event) => {
          if (event.data.newNotification) {
            notificationsLoadable.remove();
          }
          onNotificationsChanged(event);
        }
      );
    }
  }, [user, client]);

  return (
    <StyledMenu
      keepMounted
      open={Boolean(anchorEl)}
      anchorEl={anchorEl}
      onClose={onClose}
      anchorOrigin={{
        vertical: 'bottom',
        horizontal: 'right',
      }}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      slotProps={{
        paper: {
          style: {
            maxHeight: 500,
            minWidth: 400,
          },
          onScroll: (event) => {
            const target = event.target as HTMLDivElement;
            if (
              notificationsLoadable?.hasNextPage &&
              !notificationsLoadable.isFetching &&
              target.scrollHeight - target.clientHeight - target.scrollTop <
                FETCH_NEXT_PAGE_SCROLL_THRESHOLD_IN_PIXELS
            ) {
              notificationsLoadable.fetchNextPage();
            }
          },
        },
      }}
    >
      <List id="notifications-list" data-cy="notifications-list">
        <StyledListItemHeader divider>
          <T keyName="notifications-header" />
        </StyledListItemHeader>
        {notifications?.map((notification, i) => {
          const Component = notificationComponents[notification.type]!;
          return (
            <Component
              notification={notification}
              key={notification.id}
              isLast={i === notifications.length - 1}
            />
          );
        })}
        {notifications?.length === 0 && (
          <ListItem data-cy="notifications-empty-message">
            <T keyName="notifications-empty" />
          </ListItem>
        )}
        {(notificationsLoadable.isFetching ||
          notificationsLoadable.hasNextPage) && (
          <ListItem>
            <BoxLoading width="100%" />
          </ListItem>
        )}
      </List>
    </StyledMenu>
  );
};
