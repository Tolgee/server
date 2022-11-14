import {
  Dialog,
  DialogTitle,
  DialogContent,
  TextField,
  DialogActions,
  Button,
  Box,
} from '@mui/material';
import { Field, Formik, Form } from 'formik';
import { useTranslate } from '@tolgee/react';
import { Validation } from 'tg.constants/GlobalValidationSchema';
import { FieldError } from 'tg.component/FormField';

type Props = {
  namespace: string;
  onClose: () => void;
};

export const NamespaceRenameDialog: React.FC<Props> = ({
  namespace,
  onClose,
}) => {
  const t = useTranslate();

  return (
    <Dialog open onClose={onClose} fullWidth>
      <Formik
        initialValues={{ namespace }}
        validationSchema={Validation.NAMESPACE_FORM}
        onSubmit={(values) => {
          // missing endpoint for renaming
        }}
      >
        <Form>
          <DialogTitle>{t('namespace_rename_title')}</DialogTitle>

          <DialogContent>
            <Field name="namespace">
              {({ field, meta }) => (
                <Box mt={1}>
                  <TextField
                    data-cy="namespaces-rename-text-field"
                    placeholder={t('namespace_rename_placeholder')}
                    fullWidth
                    size="small"
                    autoFocus
                    {...field}
                  />
                  <FieldError error={meta.touched && meta.error} />
                </Box>
              )}
            </Field>
          </DialogContent>
          <DialogActions>
            <Button data-cy="global-confirmation-cancel" onClick={onClose}>
              {t('namespace_rename_cancel')}
            </Button>
            <Button
              data-cy="global-confirmation-confirm"
              color="primary"
              type="submit"
            >
              {t('namespace_rename_confirm')}
            </Button>
          </DialogActions>
        </Form>
      </Formik>
    </Dialog>
  );
};
