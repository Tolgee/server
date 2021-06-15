import { default as React, FunctionComponent, useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { LINKS, PARAMS } from '../../../../constants/links';
import { Button, TextField } from '@material-ui/core';
import { BaseView } from '../../../layout/BaseView';
import { useSelector } from 'react-redux';
import { AppState } from '../../../../store';
import { container } from 'tsyringe';
import { ProjectInvitationActions } from '../../../../store/project/invitations/ProjectInvitationActions';
import { projectPermissionTypes } from '../../../../constants/projectPermissionTypes';
import { StandardForm } from '../../../common/form/StandardForm';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import { BoxLoading } from '../../../common/BoxLoading';
import { EmptyListMessage } from '../../../common/EmptyListMessage';
import { T, useTranslate } from '@tolgee/react';
import { PermissionSelect } from '../../../security/PermissionSelect';
import { useProject } from '../../../../hooks/useProject';
import { Navigation } from '../../../navigation/Navigation';

const actions = container.resolve(ProjectInvitationActions);
export const ProjectInviteView: FunctionComponent<Record<string, unknown>> = (
  props
) => {
  const match = useRouteMatch();
  const projectId = match.params[PARAMS.PROJECT_ID];

  const generateLoadable = useSelector(
    (state: AppState) => state.projectInvitation.loadables.generateInvitation
  );

  const state = useSelector((state: AppState) => state.projectInvitation);

  useEffect(() => {
    actions.loadableActions.list.dispatch(projectId);
  }, [generateLoadable.data]);

  const project = useProject();

  const t = useTranslate();

  const onCancel = (id: number) => {
    actions.loadableActions.delete.dispatch(id);
  };

  return (
    <BaseView
      navigation={
        <Navigation
          path={[
            [
              project.name,
              LINKS.PROJECT_TRANSLATIONS.build({
                [PARAMS.PROJECT_ID]: project.id,
              }),
            ],
            [
              t('invite_user_title'),
              LINKS.PROJECT_TRANSLATIONS.build({
                [PARAMS.PROJECT_ID]: project.id,
              }),
            ],
          ]}
        />
      }
      xs={12}
      md={8}
      lg={6}
    >
      {() => (
        <>
          <StandardForm
            saveActionLoadable={generateLoadable}
            submitButtons={
              <Button
                variant="contained"
                color="primary"
                type="submit"
                size="large"
              >
                <T>invite_user_generate_invitation_link</T>
              </Button>
            }
            onSubmit={(v) =>
              actions.loadableActions.generateInvitation.dispatch({
                path: {
                  projectId: projectId,
                },
                content: {
                  'application/json': v,
                },
              })
            }
            initialValues={{ type: 'MANAGE' }}
          >
            <PermissionSelect
              label={<T>invite_user_permission_label</T>}
              name="type"
              fullWidth
            />
          </StandardForm>

          {generateLoadable.data && (
            <Box mt={2}>
              <TextField
                fullWidth
                multiline
                InputProps={{
                  readOnly: true,
                }}
                value={LINKS.ACCEPT_INVITATION.buildWithOrigin({
                  [PARAMS.INVITATION_CODE]: generateLoadable.data,
                })}
                label={<T>invite_user_invitation_code</T>}
              />
            </Box>
          )}

          {(state.loadables.list.loading && <BoxLoading />) ||
            (state.loadables.list.data &&
              !!state.loadables.list.data.length && (
                <Box mt={4}>
                  <Typography variant="h6">
                    <T>invite_user_active_invitation_codes</T>
                  </Typography>
                  <List>
                    {state.loadables.list.data.map((i) => (
                      <ListItem key={i.id}>
                        <ListItemText>
                          {i.code.substr(0, 10)}...
                          {i.code.substr(i.code.length - 10, 10)}
                          &nbsp;[
                          <i>
                            <T>invite_user_permission_label</T>:
                            <T>{`permission_type_${
                              projectPermissionTypes[i.type]
                            }`}</T>
                          </i>
                          ]
                        </ListItemText>
                        <ListItemSecondaryAction>
                          <Button
                            color="secondary"
                            onClick={() => onCancel(i.id)}
                          >
                            <T>invite_user_invitation_cancel_button</T>
                          </Button>
                        </ListItemSecondaryAction>
                      </ListItem>
                    ))}
                  </List>
                </Box>
              )) || <EmptyListMessage />}
        </>
      )}
    </BaseView>
  );
};
