import { default as React, FunctionComponent, useState } from 'react';
import { Button, MenuProps } from '@material-ui/core';
import { container } from 'tsyringe';
import { GlobalActions } from '../../store/global/GlobalActions';
import { useSelector } from 'react-redux';
import { AppState } from '../../store';
import { useConfig } from '../../hooks/useConfig';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import { useUser } from '../../hooks/useUser';
import { Link } from 'react-router-dom';
import KeyboardArrowDownIcon from '@material-ui/icons/KeyboardArrowDown';
import PersonIcon from '@material-ui/icons/Person';
import withStyles from '@material-ui/core/styles/withStyles';
import { T } from '@tolgee/react';
import { useUserMenuItems } from '../../hooks/useUserMenuItems';

interface UserMenuProps {
  variant: 'small' | 'expanded';
}

const globalActions = container.resolve(GlobalActions);

export const UserMenu: FunctionComponent<UserMenuProps> = (props) => {
  const userLogged = useSelector(
    (state: AppState) => state.global.security.allowPrivate
  );

  const authentication = useConfig().authentication;

  const handleOpen = (event: React.MouseEvent<HTMLButtonElement>) => {
    //@ts-ignore
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const [anchorEl, setAnchorEl] = useState(null);

  const user = useUser();
  const userMenuItems = useUserMenuItems();

  if (!authentication || !user) {
    return null;
  }

  const StyledMenu = withStyles({
    paper: {
      border: '1px solid #d3d4d5',
    },
  })((props: MenuProps) => (
    <Menu
      elevation={0}
      getContentAnchorEl={null}
      anchorOrigin={{
        vertical: 'bottom',
        horizontal: 'right',
      }}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      {...props}
    />
  ));

  return (
    <>
      {userLogged && (
        <div>
          <Button
            style={{ padding: 0 }}
            endIcon={<KeyboardArrowDownIcon />}
            color="inherit"
            data-cy="global-user-menu-button"
            aria-controls="user-menu"
            aria-haspopup="true"
            onClick={handleOpen}
          >
            {props.variant == 'expanded' ? user.name : <PersonIcon />}
          </Button>
          <StyledMenu
            id="user-menu"
            keepMounted
            open={!!anchorEl}
            anchorEl={anchorEl}
            onClose={handleClose}
          >
            <MenuItem onClick={() => globalActions.logout.dispatch()}>
              <T noWrap>user_menu_logout</T>
            </MenuItem>
            {userMenuItems.map((item, index) => (
              //@ts-ignore
              <MenuItem key={index} component={Link} to={item.link}>
                <T noWrap>{item.nameTranslationKey}</T>
              </MenuItem>
            ))}
          </StyledMenu>
        </div>
      )}
    </>
  );
};
