import React, { FunctionComponent } from 'react';
import { Box, Typography } from '@mui/material';
import { T, useTranslate } from '@tolgee/react';

import { useApiMutation } from 'tg.service/http/useQueryApi';
import { UserUpdatePasswordDTO } from 'tg.service/request.types';
import { StandardForm } from 'tg.component/common/form/StandardForm';
import { TextField } from 'tg.component/common/form/fields/TextField';
import { NewPasswordLabel } from 'tg.component/security/SetPasswordField';
import { useUser } from 'tg.globalContext/helpers';
import { Validation } from 'tg.constants/GlobalValidationSchema';
import { messageService } from 'tg.service/MessageService';
import { securityService } from 'tg.service/SecurityService';

const PasswordFieldWithValidation = React.lazy(
  () => import('tg.component/security/PasswordFieldWithValidation')
);


export const ChangePassword: FunctionComponent = () => {
  const user = useUser();

  const { t } = useTranslate();

  const updatePassword = useApiMutation({
    url: '/v2/user/password',
    method: 'put',
    fetchOptions: {
      disableAutoErrorHandle: true,
    },
  });

  const handleSubmit = (v: UserUpdatePasswordDTO) => {
    updatePassword.mutate(
      { content: { 'application/json': v } },
      {
        onSuccess(r) {
          securityService.setToken(r.accessToken!);
          messageService.success(<T keyName="password-updated" />);
        },
      }
    );
  };

  if (!user) return null;

  return (
    <Box>
      <Typography variant="h6">
        <T keyName="Password" />
      </Typography>
      <StandardForm
        saveActionLoadable={updatePassword}
        initialValues={
          {
            currentPassword: '',
            password: '',
          } as UserUpdatePasswordDTO
        }
        validationSchema={Validation.USER_PASSWORD_CHANGE(t)}
        onSubmit={handleSubmit}
      >
        <TextField
          name="currentPassword"
          type="password"
          label={<T keyName="current-password" />}
          variant="standard"
        />
        <PasswordFieldWithValidation label={<NewPasswordLabel />} />
      </StandardForm>
    </Box>
  );
};
