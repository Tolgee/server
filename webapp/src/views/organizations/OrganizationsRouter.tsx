import { Box } from '@mui/material';
import { Switch } from 'react-router-dom';

import { BoxLoading } from 'tg.component/common/BoxLoading';
import { PrivateRoute } from 'tg.component/common/PrivateRoute';
import { DashboardPage } from 'tg.component/layout/DashboardPage';
import { LINKS } from 'tg.constants/links';
import { useIsAdmin } from 'tg.globalContext/helpers';

import { OrganizationCreateView } from './OrganizationCreateView';
import { OrganizationMemberPrivilegesView } from './OrganizationMemberPrivilegesView';
import { OrganizationMembersView } from './members/OrganizationMembersView';
import { OrganizationProfileView } from './OrganizationProfileView';
import { useOrganization } from './useOrganization';
import { OrganizationAppsView } from './apps/OrganizationAppsView';
import { getEe } from '../../plugin/getEe';

const SpecificOrganizationRouter = () => {
  const organization = useOrganization();
  const isAdmin = useIsAdmin();
  const isAdminAccess =
    organization && organization?.currentUserRole !== 'OWNER' && isAdmin;

  return (
    <DashboardPage isAdminAccess={isAdminAccess}>
      {organization ? (
        <>
          <PrivateRoute exact path={LINKS.ORGANIZATION_PROFILE.template}>
            <OrganizationProfileView />
          </PrivateRoute>
          <PrivateRoute exact path={LINKS.ORGANIZATION_MEMBERS.template}>
            <OrganizationMembersView />
          </PrivateRoute>
          <PrivateRoute
            exact
            path={LINKS.ORGANIZATION_MEMBER_PRIVILEGES.template}
          >
            <OrganizationMemberPrivilegesView />
          </PrivateRoute>

          <PrivateRoute path={LINKS.ORGANIZATION_APPS.template}>
            <OrganizationAppsView />
          </PrivateRoute>
        </>
      ) : (
        <Box
          width="100%"
          height="100%"
          display="flex"
          alignItems="center"
          justifyContent="center"
        >
          <BoxLoading />
        </Box>
      )}
    </DashboardPage>
  );
};

export const OrganizationsRouter = () => {
  const {
    routes: { Organization: EeOrganization },
  } = getEe();

  return (
    <Switch>
      <PrivateRoute exact path={LINKS.ORGANIZATIONS_ADD.template}>
        <OrganizationCreateView />
      </PrivateRoute>

      <EeOrganization />

      <PrivateRoute path={LINKS.ORGANIZATION.template}>
        <SpecificOrganizationRouter />
      </PrivateRoute>
    </Switch>
  );
};
