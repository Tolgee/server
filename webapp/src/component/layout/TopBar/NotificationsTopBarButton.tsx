import {
  default as React,
  FunctionComponent,
  useEffect,
  useState,
} from 'react';
import { Badge, IconButton, styled } from '@mui/material';
import { useApiQuery } from 'tg.service/http/useQueryApi';
import { Bell01 } from '@untitled-ui/icons-react';
import { NotificationsPopup } from 'tg.component/layout/TopBar/NotificationsPopup';
import { NotificationsChanged } from 'tg.websocket-client/WebsocketClient';
import { PopoverProps } from '@mui/material/Popover';

const StyledIconButton = styled(IconButton)`
  width: 40px;
  height: 40px;

  img {
    user-drag: none;
  }
`;

export const NotificationsTopBarButton: FunctionComponent<{
  className?: string;
}> = () => {
  const [anchorEl, setAnchorEl] = useState<
    PopoverProps['anchorEl'] | undefined
  >(undefined);
  const [unseenCount, setUnseenCount] = useState<number | undefined>(undefined);

  const unseenNotificationsLoadable = useApiQuery({
    url: '/v2/notifications',
    method: 'get',
    query: { size: 1, filterSeen: false },
    options: {
      enabled: unseenCount === undefined,
      refetchOnMount: false,
      keepPreviousData: true,
    },
  });

  const handleOpen = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  useEffect(() => {
    if (unseenCount !== undefined) return;
    setUnseenCount(
      (prevState) =>
        unseenNotificationsLoadable.data?.page?.totalElements || prevState
    );
  }, [unseenNotificationsLoadable.data]);

  const onNotificationsChanged = function (event: NotificationsChanged) {
    setUnseenCount(event.data.currentlyUnseenCount);
    unseenNotificationsLoadable.remove();
  };

  return (
    <>
      <StyledIconButton
        color="inherit"
        aria-controls="notifications-button"
        aria-haspopup="true"
        data-cy="notifications-button"
        onClick={handleOpen}
        size="large"
      >
        <Badge
          badgeContent={unseenCount}
          color="secondary"
          slotProps={{
            badge: {
              //@ts-ignore
              'data-cy': 'notifications-count',
            },
          }}
        >
          <Bell01 />
        </Badge>
      </StyledIconButton>
      <NotificationsPopup
        onClose={handleClose}
        onNotificationsChanged={onNotificationsChanged}
        anchorEl={anchorEl}
      />
    </>
  );
};
