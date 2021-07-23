import React, { ReactNode, useEffect } from 'react';
import {
  Box,
  Button,
  Container,
  LinearProgress,
  useTheme,
} from '@material-ui/core';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import grey from '@material-ui/core/colors/grey';

import { useLoading } from 'tg.hooks/loading';

import { SecondaryBar } from './SecondaryBar';
import { useConfig } from 'tg.hooks/useConfig';
import { SecondaryBarSearchField } from 'tg.component/layout/SecondaryBarSearchField';
import { Add } from '@material-ui/icons';
import { T } from '@tolgee/react';
import { Link } from 'react-router-dom';

export interface BaseViewProps {
  windowTitle?: string;
  loading?: boolean;
  title?: ReactNode;
  onAdd?: () => void;
  addLinkTo?: string;
  children: (() => ReactNode) | ReactNode;
  xs?: boolean | 'auto' | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12;
  sm?: boolean | 'auto' | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12;
  md?: boolean | 'auto' | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12;
  lg?: boolean | 'auto' | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12;
  onSearch?: (string) => void;
  navigation?: ReactNode;
  customHeader?: ReactNode;
  hideChildrenOnLoading?: boolean;
  containerMaxWidth?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | false;
}

export const BaseView = (props: BaseViewProps) => {
  const theme = useTheme();
  const hideChildrenOnLoading =
    props.hideChildrenOnLoading === undefined || props.hideChildrenOnLoading;

  const globalLoading = useLoading();
  const config = useConfig();

  useEffect(() => {
    if (props.windowTitle) {
      const oldTitle = window.document.title;
      window.document.title = `${config.appName} - ${props.windowTitle}`;
      return () => {
        window.document.title = oldTitle;
      };
    }
  }, []);

  return (
    <>
      <Box position="absolute" width="100%" top={0} zIndex={theme.zIndex.modal}>
        {(globalLoading || props.loading) && (
          <LinearProgress
            data-cy="global-base-view-loading"
            style={{ height: '3px' }}
          />
        )}
      </Box>

      <Container
        data-cy="global-base-view-content-scrollable"
        maxWidth={false}
        style={{
          backgroundColor: 'white',
          borderBottom: `1px solid ${grey[100]}`,
          padding: 0,
          minHeight: '100%',
        }}
      >
        <Box minHeight="100%">
          {props.navigation}
          {(props.title || props.customHeader) && (
            <SecondaryBar>
              <Container
                maxWidth={props.containerMaxWidth || false}
                style={{ padding: 0 }}
              >
                <Grid container justify="center" alignItems="center">
                  <Grid
                    data-cy="global-base-view-title"
                    item
                    xs={props.xs || 12}
                    md={props.md || 12}
                    lg={props.lg || 12}
                    sm={props.sm || 12}
                  >
                    {props.customHeader || (
                      <Box display="flex" justifyContent="space-between">
                        <Box display="flex" alignItems={'center'}>
                          <Typography variant="h5">{props.title}</Typography>
                          {typeof props.onSearch === 'function' && (
                            <Box ml={2}>
                              <SecondaryBarSearchField
                                onSearch={props.onSearch}
                              />
                            </Box>
                          )}
                        </Box>
                        <Box display="flex">
                          {(props.onAdd || props.addLinkTo) && (
                            <Button
                              component={props.addLinkTo ? Link : Button}
                              to={props.addLinkTo}
                              startIcon={<Add />}
                              color="primary"
                              size="small"
                              variant="contained"
                              aria-label="add"
                              onClick={props.onAdd}
                              data-cy="translations-add-button"
                            >
                              <T>global_add_button</T>
                            </Button>
                          )}
                        </Box>
                      </Box>
                    )}
                  </Grid>
                </Grid>
              </Container>
            </SecondaryBar>
          )}
          <Box pl={4} pr={4} pt={2} pb={2}>
            <Container
              maxWidth={props.containerMaxWidth || false}
              style={{ padding: 0 }}
            >
              <Grid container justify="center" alignItems="center">
                <Grid
                  item
                  xs={props.xs || 12}
                  md={props.md || 12}
                  lg={props.lg || 12}
                  sm={props.sm || 12}
                >
                  {!props.loading || !hideChildrenOnLoading ? (
                    <Box data-cy="global-base-view-content">
                      {typeof props.children === 'function'
                        ? props.children()
                        : props.children}
                    </Box>
                  ) : (
                    <></>
                  )}
                </Grid>
              </Grid>
            </Container>
          </Box>
        </Box>
      </Container>
    </>
  );
};
