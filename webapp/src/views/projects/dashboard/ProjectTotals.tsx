import React, { useRef, useState } from 'react';
import clsx from 'clsx';
import { useCurrentLanguage, useTranslate } from '@tolgee/react';
import { Box, styled, Typography, Menu, MenuItem } from '@mui/material';
import { Edit } from '@mui/icons-material';
import { useHistory } from 'react-router-dom';

import { components } from 'tg.service/apiSchema.generated';
import { useProject } from 'tg.hooks/useProject';
import { AvatarImg } from 'tg.component/common/avatar/AvatarImg';
import { LINKS, PARAMS } from 'tg.constants/links';
import { useApiQuery } from 'tg.service/http/useQueryApi';
import { useProjectPermissions } from 'tg.hooks/useProjectPermissions';
import { ProjectPermissionType } from 'tg.service/response.types';

const StyledTiles = styled(Box)`
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  grid-template-areas: 'languages text text progress progress users users tags';
  gap: 10px;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(4, 1fr);
    grid-template-areas:
      'languages text     text   tags'
      'progress  progress users  users';
  }

  @media (max-width: 800px) {
    grid-template-columns: repeat(2, 1fr);
    grid-template-areas:
      'languages tags'
      'text      text'
      'progress  progress'
      'users     users';
  }
`;

const StyledTile = styled(Box)`
  background-color: ${({ theme }) => theme.palette.emphasis[100]};
  border-radius: 20px;
  height: 120px;
  display: grid;
  gap: 10px;
  grid-auto-flow: column;
  grid-auto-columns: 1fr;
  position: relative;
  text-align: center;

  align-items: stretch;
  color: ${({ theme: { palette } }) =>
    palette.mode === 'dark' ? palette.text.primary : palette.primary.main};

  &.clickable {
    transition: background-color 0.1s ease-out;
    cursor: pointer;
    &:hover {
      background-color: ${({ theme }) => theme.palette.emphasis[200]};
      transition: background-color 0.2s ease-in;
    }
  }

  @media (max-width: 1200px) {
    height: 100px;
  }

  @media (max-width: 800px) {
    height: 80px;
  }
`;

const StyledTileDataItem = styled(Box)`
  display: grid;
  grid-template-rows: 1fr auto auto 1fr;
  grid-template-areas:
    '.'
    'data'
    'label'
    'menu';
  border-radius: 20px;
`;

const StyledTileValue = styled(Box)`
  grid-area: data;
  font-size: 28px;
  display: flex;
  justify-content: center;
  @media (max-width: 800px) {
    font-size: 24px;
  }
`;

const StyledTileDescription = styled('span')`
  grid-area: label;
  font-size: 18px;
  display: flex;
  align-items: center;
  flex-direction: column;
  @media (max-width: 800px) {
    font-size: 14px;
  }
`;

const StyledTileSubDescription = styled(Typography)`
  font-size: 13px;
  text-align: center;
  color: ${({ theme }) => theme.palette.text.secondary};
  @media (max-width: 800px) {
    font-size: 12px;
  }
`;

const StyledTileEdit = styled(Box)`
  position: absolute;
  top: 0px;
  right: 0px;
  padding: 10px;
  color: ${({ theme }) => theme.palette.text.secondary};
`;

export const ProjectTotals: React.FC<{
  stats: components['schemas']['ProjectStatsModel'];
}> = ({ stats }) => {
  const t = useTranslate();
  const project = useProject();
  const history = useHistory();
  const tags = useApiQuery({
    url: '/v2/projects/{projectId}/tags',
    method: 'get',
    path: { projectId: project.id },
    query: { size: 1000 },
  });
  const getLang = useCurrentLanguage();
  const locale = getLang();

  const [anchorEl, setAnchorEl] = useState<HTMLDivElement | null>(null);
  const anchorWidth = useRef();
  const open = Boolean(anchorEl);
  const handleMenuOpen = (event) => {
    anchorWidth.current = event.currentTarget.offsetWidth;
    setAnchorEl(event.currentTarget);
  };
  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const redirectToLanguages = () => {
    history.push(
      LINKS.PROJECT_LANGUAGES.build({ [PARAMS.PROJECT_ID]: project.id })
    );
  };

  const redirectToPermissions = () => {
    history.push(
      LINKS.PROJECT_PERMISSIONS.build({ [PARAMS.PROJECT_ID]: project.id })
    );
  };

  const redirectToTranslations = () => {
    history.push(
      LINKS.PROJECT_TRANSLATIONS.build({ [PARAMS.PROJECT_ID]: project.id })
    );
  };

  const redirectToTag = (tag: string) => () => {
    history.push(
      LINKS.PROJECT_TRANSLATIONS.build({ [PARAMS.PROJECT_ID]: project.id }) +
        `?filters=${encodeURIComponent(JSON.stringify({ filterTag: [tag] }))}`
    );
  };

  const permissions = useProjectPermissions();

  const canManage = permissions.satisfiesPermission(
    ProjectPermissionType.MANAGE
  );

  const tagsPresent = Boolean(stats.tagCount);

  return (
    <>
      <StyledTiles>
        <StyledTile
          gridArea="languages"
          onClick={canManage ? redirectToLanguages : undefined}
          className={clsx({ clickable: canManage })}
          data-cy="project-dashboard-language-count"
        >
          <StyledTileDataItem>
            <StyledTileValue>
              {Number(stats.languageCount).toLocaleString(locale)}
            </StyledTileValue>
            <StyledTileDescription>
              {t('project_dashboard_language_count', 'Languages', {
                count: stats.languageCount,
              })}
            </StyledTileDescription>
          </StyledTileDataItem>
          {canManage && (
            <StyledTileEdit>
              <Edit fontSize="small" />
            </StyledTileEdit>
          )}
        </StyledTile>

        <StyledTile
          gridArea="text"
          onClick={redirectToTranslations}
          className="clickable"
        >
          <StyledTileDataItem data-cy="project-dashboard-key-count">
            <StyledTileValue>
              {Number(stats.keyCount).toLocaleString(locale)}
            </StyledTileValue>
            <StyledTileDescription>
              {t('project_dashboard_key_count', 'Keys', {
                count: stats.keyCount,
              })}
            </StyledTileDescription>
          </StyledTileDataItem>
          <StyledTileDataItem data-cy="project-dashboard-base-word-count">
            <StyledTileValue>
              {Number(stats.baseWordsCount).toLocaleString(locale)}
            </StyledTileValue>
            <StyledTileDescription>
              {t('project_dashboard_base_words_count', 'Base words', {
                count: stats.baseWordsCount,
              })}
            </StyledTileDescription>
          </StyledTileDataItem>
        </StyledTile>

        <StyledTile
          gridArea="progress"
          onClick={redirectToTranslations}
          className="clickable"
        >
          <StyledTileDataItem data-cy="project-dashboard-translated-percentage">
            <StyledTileValue>
              {!isNaN(stats.translatedPercentage)
                ? t(
                    'project_dashboard_percent_count',
                    '{percentage, number, :: % }',
                    {
                      percentage: stats.translatedPercentage / 100,
                    }
                  )
                : t('project_dashboard_percent_nan', '-')}
            </StyledTileValue>
            <StyledTileDescription>
              {t('project_dashboard_translated_percent', 'Translated')}
            </StyledTileDescription>
          </StyledTileDataItem>
          <StyledTileDataItem data-cy="project-dashboard-reviewed-percentage">
            <StyledTileValue>
              {!isNaN(stats.reviewedPercentage)
                ? t(
                    'project_dashboard_percent_count',
                    '{percentage, number, :: % }',
                    {
                      percentage: stats.reviewedPercentage / 100,
                    }
                  )
                : t('project_dashboard_percent_nan', '-')}
            </StyledTileValue>
            <StyledTileDescription>
              {t('project_dashboard_reviewed_percent', 'Reviewed')}
            </StyledTileDescription>
          </StyledTileDataItem>
        </StyledTile>

        <StyledTile
          gridArea="users"
          data-cy="project-dashboard-members"
          onClick={canManage ? redirectToPermissions : undefined}
          className={clsx({ clickable: canManage })}
        >
          <StyledTileDataItem>
            <StyledTileValue>
              <OwnerAvatar
                userOwner={project.userOwner}
                organizationOwner={project.organizationOwner}
              />
            </StyledTileValue>
            <StyledTileDescription>
              {project.userOwner?.name || project.organizationOwner?.name}
              <StyledTileSubDescription>
                {t('project_dashboard_project_owner', 'Project Owner')}
              </StyledTileSubDescription>
            </StyledTileDescription>
          </StyledTileDataItem>
          <StyledTileDataItem data-cy="project-dashboard-members-count">
            <StyledTileValue>
              {Number(stats.membersCount).toLocaleString(locale)}
            </StyledTileValue>
            <StyledTileDescription>
              {t('project_dashboard_member_count', 'Members')}
            </StyledTileDescription>
          </StyledTileDataItem>
          {canManage && (
            <StyledTileEdit>
              <Edit fontSize="small" />
            </StyledTileEdit>
          )}
        </StyledTile>

        <StyledTile
          gridArea="tags"
          className={clsx({ clickable: tagsPresent })}
          onClick={tagsPresent ? handleMenuOpen : undefined}
          aria-controls={open ? 'basic-menu' : undefined}
          aria-haspopup="true"
          aria-expanded={open ? 'true' : undefined}
          data-cy="project-dashboard-tags"
        >
          <StyledTileDataItem>
            <StyledTileValue>
              {Number(stats.tagCount).toLocaleString(locale)}
            </StyledTileValue>
            <StyledTileDescription>
              {t('project_dashboard_tag_count', 'Tags')}
            </StyledTileDescription>
          </StyledTileDataItem>
        </StyledTile>
        <Menu
          PaperProps={{ sx: { minWidth: anchorWidth.current + 'px' } }}
          anchorEl={anchorEl}
          open={open}
          onClose={handleMenuClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'center',
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'center',
          }}
        >
          {tags.data?._embedded?.tags?.map((tag) => (
            <MenuItem key={tag.id} onClick={redirectToTag(tag.name)}>
              {tag.name}
            </MenuItem>
          ))}
        </Menu>
      </StyledTiles>
    </>
  );
};

type OwnerAvatarProps = {
  userOwner?: components['schemas']['UserAccountModel'];
  organizationOwner?: components['schemas']['SimpleOrganizationModel'];
};

const OwnerAvatar = (props: OwnerAvatarProps) => (
  <AvatarImg
    size={32}
    owner={{
      avatar: props.userOwner?.avatar || props.organizationOwner?.avatar,
      id: props.userOwner?.id || props.organizationOwner!.id,
      name: props.userOwner?.name || props.organizationOwner?.name,
      type: props.userOwner ? 'PROJECT' : 'ORG',
    }}
    autoAvatarType={props.userOwner ? 'IDENTICON' : 'INITIALS'}
    circle={true}
  />
);
