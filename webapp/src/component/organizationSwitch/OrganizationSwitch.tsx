import { useRef, useState } from 'react';
import { Box, Link, styled } from '@mui/material';
import { ArrowDropDown } from '@mui/icons-material';

import { components } from 'tg.service/apiSchema.generated';
import { useApiQuery } from 'tg.service/http/useQueryApi';
import { OrganizationItem } from './OrganizationItem';
import { useHistory } from 'react-router-dom';
import { LINKS } from 'tg.constants/links';
import { usePreferredOrganization } from 'tg.globalContext/helpers';
import { OrganizationPopover } from './OrganizationPopover';

type OrganizationModel = components['schemas']['OrganizationModel'];

const StyledLink = styled(Link)`
  display: flex;
`;

type Props = {
  onSelect?: (organization: OrganizationModel) => void;
  ownedOnly?: boolean;
};

export const OrganizationSwitch: React.FC<Props> = ({
  onSelect,
  ownedOnly,
}) => {
  const anchorEl = useRef<HTMLAnchorElement>(null);
  const [isOpen, setIsOpen] = useState(false);
  const { preferredOrganization } = usePreferredOrganization();
  const { updatePreferredOrganization } = usePreferredOrganization();
  const history = useHistory();

  const handleClose = () => {
    setIsOpen(false);
  };

  const handleClick = () => {
    setIsOpen(true);
  };

  const handleSelectOrganization = (organization: OrganizationModel) => {
    handleClose();
    updatePreferredOrganization(organization);
    onSelect?.(organization);
  };

  const handleCreateNewOrg = () => {
    handleClose();
    history.push(LINKS.ORGANIZATIONS_ADD.build());
  };

  const organizationsLoadable = useApiQuery({
    url: '/v2/organizations',
    method: 'get',
    query: {
      params: { filterCurrentUserOwner: false },
      size: 1000,
      sort: ['name'],
    },
  });

  const selected = organizationsLoadable.data?._embedded?.organizations?.find(
    (org) => org.id === preferredOrganization.id
  );

  return (
    <>
      <Box display="flex" data-cy="organization-switch" overflow="hidden">
        <StyledLink
          ref={anchorEl}
          style={{
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            flexWrap: 'wrap',
            flexShrink: 1,
          }}
          onClick={handleClick}
        >
          {selected && <OrganizationItem data={selected} size={18} />}
          <ArrowDropDown fontSize={'small'} sx={{ marginRight: '-6px' }} />
        </StyledLink>

        <OrganizationPopover
          ownedOnly={ownedOnly}
          open={isOpen}
          onClose={handleClose}
          selected={preferredOrganization}
          onSelect={handleSelectOrganization}
          anchorEl={anchorEl.current!}
          onAddNew={handleCreateNewOrg}
        />
      </Box>
    </>
  );
};
