import React, {
  FunctionComponent,
  ReactNode,
  useEffect,
  useState,
} from 'react';
import {
  Alert,
  AlertTitle,
  Box,
  Button,
  Collapse,
  IconButton,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { T } from '@tolgee/react';

import { components } from 'tg.service/apiSchema.generated';
import { useImportDataHelper } from './hooks/useImportDataHelper';

export const ImportAlertError: FunctionComponent<{
  error: components['schemas']['ImportAddFilesResultModel']['errors'][0];
}> = (props) => {
  const [moreOpen, setMoreOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);
  const importDataHelper = useImportDataHelper();

  let text = undefined as ReactNode | undefined;
  let params = [] as string[];

  if (props.error?.code === 'cannot_parse_file') {
    text = <T>import_error_cannot_parse_file</T>;
    params = props.error.params as any as string[];
  }

  useEffect(() => {
    setCollapsed(true);
    if (
      importDataHelper.addFilesMutation.isSuccess &&
      !importDataHelper.addFilesMutation.isLoading
    ) {
      setCollapsed(false);
      setMoreOpen(false);
    }
  }, [importDataHelper.addFilesMutation.isLoading]);

  const open = !collapsed && !!text;

  return (
    <Collapse in={open}>
      <Box mt={4} data-cy="import-file-error">
        <Alert
          severity="error"
          action={
            <>
              <Box display="inline" mr={1}>
                <Button
                  color="inherit"
                  size="small"
                  onClick={() => setMoreOpen(!moreOpen)}
                  data-cy="import-file-error-more-less-button"
                >
                  {moreOpen ? (
                    <T>import_error_less_button</T>
                  ) : (
                    <T>import_error_more_button</T>
                  )}
                </Button>
              </Box>
              <IconButton
                data-cy="import-file-error-collapse-button"
                aria-label="close"
                color="inherit"
                size="small"
                onClick={() => {
                  setCollapsed(true);
                }}
              >
                <CloseIcon fontSize="inherit" />
              </IconButton>
            </>
          }
        >
          <AlertTitle>{text}</AlertTitle>
          {params[0] && (
            <T
              parameters={{
                name: params[0],
              }}
            >
              import_cannot_parse_file_message
            </T>
          )}
          <Box pt={2}>{moreOpen && params[1]}</Box>
        </Alert>
      </Box>
    </Collapse>
  );
};
