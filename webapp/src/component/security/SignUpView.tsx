import { FunctionComponent, useCallback } from 'react';
import Box from '@mui/material/Box';
import { T, useTranslate } from '@tolgee/react';
import { useSelector } from 'react-redux';
import { Redirect } from 'react-router-dom';
import { container } from 'tsyringe';
import { Button, Link, Typography } from '@mui/material';
import { useGoogleReCaptcha } from 'react-google-recaptcha-v3';

import { Validation } from 'tg.constants/GlobalValidationSchema';
import { LINKS } from 'tg.constants/links';
import { useConfig } from 'tg.globalContext/helpers';
import { SignUpActions } from 'tg.store/global/SignUpActions';
import { AppState } from 'tg.store/index';
import { InvitationCodeService } from 'tg.service/InvitationCodeService';
import { StandardForm } from 'tg.component/common/form/StandardForm';
import { CompactView } from 'tg.component/layout/CompactView';
import LoadingButton from 'tg.component/common/form/LoadingButton';

import { Alert } from '../common/Alert';
import { TextField } from '../common/form/fields/TextField';
import { DashboardPage } from '../layout/DashboardPage';
import { SetPasswordFields } from './SetPasswordFields';
import { useOAuthServices } from 'tg.hooks/useOAuthServices';

const actions = container.resolve(SignUpActions);
const invitationService = container.resolve(InvitationCodeService);

export type SignUpType = {
  name: string;
  email: string;
  password: string;
  passwordRepeat?: string;
  organizationName: string;
  invitationCode?: string;
};

const SignUpView: FunctionComponent = () => {
  const security = useSelector((state: AppState) => state.global.security);
  const state = useSelector((state: AppState) => state.signUp.loadables.signUp);
  const config = useConfig();
  const remoteConfig = useConfig();
  const { t } = useTranslate();
  const oAuthServices = useOAuthServices();

  const WithRecaptcha = () => {
    const { executeRecaptcha } = useGoogleReCaptcha();

    const handleReCaptchaVerify = useCallback(async () => {
      if (!executeRecaptcha) {
        throw Error('Execute recaptcha not yet available');
      }

      return await executeRecaptcha('sign_up');
    }, [executeRecaptcha]);

    return (
      <View
        onSubmit={async (v: SignUpType) => {
          const recaptchaToken = await handleReCaptchaVerify();
          actions.loadableActions.signUp.dispatch({
            ...v,
            recaptchaToken: recaptchaToken,
          });
        }}
      />
    );
  };

  const WithoutRecaptcha = () => {
    return (
      <View
        onSubmit={async (v: SignUpType) => {
          actions.loadableActions.signUp.dispatch({
            ...v,
          });
        }}
      />
    );
  };

  if (
    !remoteConfig.authentication ||
    security.allowPrivate ||
    !security.allowRegistration
  ) {
    return <Redirect to={LINKS.AFTER_LOGIN.build()} />;
  }

  const orgRequired = !invitationService.getCode();

  const View = (props: { onSubmit: (v) => void }) => (
    <DashboardPage>
      <CompactView
        windowTitle={t('sign_up_title')}
        title={t('sign_up_title')}
        backLink={LINKS.LOGIN.build()}
        content={
          state.loaded && config.needsEmailVerification ? (
            <Alert severity="success">
              <T>sign_up_success_needs_verification_message</T>
            </Alert>
          ) : (
            <StandardForm
              saveActionLoadable={state}
              initialValues={
                {
                  password: '',
                  passwordRepeat: '',
                  name: '',
                  email: '',
                  organizationName: orgRequired ? '' : undefined,
                } as SignUpType
              }
              validationSchema={Validation.SIGN_UP(t, orgRequired)}
              submitButtons={
                <Box display="flex" flexDirection="column" alignItems="stretch">
                  <Box display="flex" justifyContent="flex-end">
                    <LoadingButton
                      data-cy="sign-up-submit-button"
                      color="primary"
                      type="submit"
                      variant="contained"
                      loading={state.loading}
                    >
                      <T>sign_up_submit_button</T>
                    </LoadingButton>
                  </Box>

                  {oAuthServices.length > 0 && (
                    <Box
                      height="1px"
                      bgcolor="lightgray"
                      marginY={4}
                      marginX={-1}
                    />
                  )}
                  {oAuthServices.map((provider, i) => (
                    <Button
                      key={i}
                      component="a"
                      href={provider.authenticationUrl}
                      size="medium"
                      endIcon={provider.buttonIcon}
                      variant="outlined"
                      style={{ marginBottom: '0.5rem' }}
                    >
                      <T>{provider.buttonLabelTranslationKey}</T>
                    </Button>
                  ))}
                </Box>
              }
              onSubmit={props.onSubmit}
            >
              <TextField
                name="name"
                label={<T>sign_up_form_full_name</T>}
                variant="standard"
              />
              <TextField
                name="email"
                label={<T>sign_up_form_email</T>}
                variant="standard"
              />
              {orgRequired && (
                <TextField
                  name="organizationName"
                  label={<T>sign_up_form_organization_name</T>}
                  variant="standard"
                />
              )}
              <SetPasswordFields />
              <Box mt={2} mb={3}>
                <Typography variant="body2">
                  <T
                    params={{
                      Link(content) {
                        return (
                          <Link href="https://tolgee.io/docs/terms_of_use">
                            {content}
                          </Link>
                        );
                      },
                    }}
                    keyName="sign-up-terms-and-conditions-message"
                  />
                </Typography>
              </Box>
            </StandardForm>
          )
        }
      />
    </DashboardPage>
  );

  return config.recaptchaSiteKey ? <WithRecaptcha /> : <WithoutRecaptcha />;
};

export default SignUpView;
