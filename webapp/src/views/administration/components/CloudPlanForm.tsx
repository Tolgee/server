import {
  Box,
  FormControl,
  InputLabel,
  MenuItem,
  Typography,
  Checkbox,
  FormControlLabel,
  Switch,
} from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { Field, FieldProps, Form, Formik } from 'formik';

import { TextField } from 'tg.component/common/form/fields/TextField';
import { SearchSelect } from 'tg.component/searchSelect/SearchSelect';
import { Select } from 'tg.component/common/form/fields/Select';
import { components } from 'tg.service/billingApiSchema.generated';
import { useBillingApiQuery } from 'tg.service/http/useQueryApi';
import { Validation } from 'tg.constants/GlobalValidationSchema';
import LoadingButton from 'tg.component/common/form/LoadingButton';
import { ForOrganizationsList } from './ForOrganizationsList';

type CloudPlanModel = components['schemas']['CloudPlanModel'];
type EnabledFeature =
  components['schemas']['CloudPlanModel']['enabledFeatures'][number];

type FormData = {
  type: CloudPlanModel['type'];
  name: string;
  prices: CloudPlanModel['prices'];
  includedUsage: CloudPlanModel['includedUsage'];
  stripeProductId: string | undefined;
  enabledFeatures: EnabledFeature[];
  forOrganizationIds: number[];
  public: boolean;
};

type Props = {
  initialData: FormData;
  onSubmit: (value: FormData) => void;
  loading: boolean | undefined;
};

export function CloudPlanForm({ initialData, onSubmit, loading }: Props) {
  const { t } = useTranslate();

  const productsLoadable = useBillingApiQuery({
    url: '/v2/administration/billing/stripe-products',
    method: 'get',
  });

  const featuresLoadable = useBillingApiQuery({
    url: '/v2/administration/billing/features',
    method: 'get',
  });

  const products = productsLoadable.data?._embedded?.stripeProductModels;

  const typeOptions = [
    { value: 'PAY_AS_YOU_GO', label: 'Pay as you go' },
    { value: 'FIXED', label: 'Fixed' },
    { value: 'SLOTS_FIXED', label: 'Slots fixed' },
  ];

  return (
    <Formik
      initialValues={{
        type: initialData.type,
        name: initialData.name,
        prices: initialData.prices,
        includedUsage: {
          ...initialData.includedUsage,
          translations:
            initialData.type === 'SLOTS_FIXED'
              ? initialData.includedUsage.translationSlots
              : initialData.includedUsage.translations,
        },
        stripeProductId: initialData.stripeProductId,
        enabledFeatures: initialData.enabledFeatures,
        public: initialData.public,
        forOrganizationIds: initialData.forOrganizationIds,
      }}
      enableReinitialize
      onSubmit={(values) => {
        let prices = values.prices;
        if (values.type !== 'PAY_AS_YOU_GO') {
          prices = {
            perSeat: values.prices.perSeat,
            subscriptionMonthly: values.prices.subscriptionMonthly,
            subscriptionYearly: values.prices.subscriptionYearly,
          };
        }
        onSubmit({ ...values, prices });
      }}
      validationSchema={Validation.CLOUD_PLAN_FORM}
    >
      {({ values, errors, setFieldValue }) => (
        <Form>
          <Box mb={3}>
            <TextField
              sx={{ mt: 2 }}
              name="name"
              size="small"
              label={t('administration_cloud_plan_field_name')}
              fullWidth
            />
            <Box display="flex" gap={2} sx={{ mt: 2 }}>
              <FormControl sx={{ flexBasis: '50%' }} variant="standard">
                <InputLabel shrink>
                  {t('administration_cloud_plan_field_type')}
                </InputLabel>
                <Select
                  name="type"
                  size="small"
                  fullWidth
                  sx={{ flexBasis: '50%' }}
                  renderValue={(val) =>
                    typeOptions.find((o) => o.value === val)?.label
                  }
                >
                  {typeOptions.map(({ value, label }) => (
                    <MenuItem key={value} value={value}>
                      {label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <Field name="stripeProductId">
                {({ meta, field, form }: FieldProps) => (
                  <FormControl
                    sx={{
                      flexBasis: '50%',
                      alignItems: 'stretch',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'space-around',
                    }}
                    variant="standard"
                    error={!!meta.error && meta.touched}
                  >
                    <InputLabel shrink>
                      {t('administration_cloud_plan_field_stripe_product')}
                    </InputLabel>
                    <SearchSelect
                      compareFunction={(prompt, label) =>
                        label.toLowerCase().includes(prompt.toLowerCase())
                      }
                      SelectProps={{
                        size: 'small',
                        fullWidth: true,
                        variant: 'outlined',
                      }}
                      value={field.value}
                      onChange={(val) => form.setFieldValue(field.name, val)}
                      items={
                        products?.map(({ id, name }) => ({
                          value: id,
                          name: `${id} ${name}`,
                        })) || []
                      }
                    />
                  </FormControl>
                )}
              </Field>
            </Box>
            <Typography sx={{ mt: 2 }}>
              {t('administration_cloud_plan_form_prices_title')}
            </Typography>
            <Box display="flex" gap={2} sx={{ mt: 1 }}>
              <TextField
                name="prices.subscriptionMonthly"
                size="small"
                label={t('administration_cloud_plan_field_price_monthly')}
                type="number"
                fullWidth
              />
              <TextField
                name="prices.subscriptionYearly"
                size="small"
                label={t('administration_cloud_plan_field_price_yearly')}
                type="number"
                fullWidth
              />
              <TextField
                name="prices.perThousandMtCredits"
                size="small"
                label={t(
                  'administration_cloud_plan_field_price_per_thousand_mt_credits'
                )}
                type="number"
                fullWidth
                disabled={values.type !== 'PAY_AS_YOU_GO'}
              />
              <TextField
                name="prices.perThousandTranslations"
                size="small"
                label={t(
                  'administration_cloud_plan_field_price_per_thousand_translations'
                )}
                type="number"
                fullWidth
                disabled={values.type !== 'PAY_AS_YOU_GO'}
              />
            </Box>

            <Typography sx={{ mt: 2 }}>
              {t('administration_cloud_plan_form_limits_title')}
            </Typography>
            <Box display="flex" gap={2} sx={{ mt: 1 }}>
              <TextField
                name="includedUsage.mtCredits"
                size="small"
                type="number"
                fullWidth
                label={t('administration_cloud_plan_field_included_mt_credits')}
              />
              <TextField
                name="includedUsage.translations"
                size="small"
                type="number"
                fullWidth
                label={
                  values.type === 'SLOTS_FIXED'
                    ? t(
                        'administration_cloud_plan_field_included_translation_slots'
                      )
                    : t('administration_cloud_plan_field_included_translations')
                }
              />
            </Box>
            <Box>
              <Typography sx={{ mt: 2 }}>
                {t('administration_cloud_plan_form_features_title')}
              </Typography>
              <Field name="enabledFeatures">
                {(props: FieldProps<string[]>) =>
                  featuresLoadable.data?.map((feature) => {
                    const values = props.field.value;

                    const toggleField = () => {
                      let newValues = values;
                      if (values.includes(feature)) {
                        newValues = values.filter((val) => val !== feature);
                      } else {
                        newValues = [...values, feature];
                      }
                      props.form.setFieldValue(props.field.name, newValues);
                    };

                    return (
                      <FormControlLabel
                        key={feature}
                        control={
                          <Checkbox
                            value={feature}
                            checked={props.field.value.includes(feature)}
                            onChange={toggleField}
                          />
                        }
                        label={feature}
                      />
                    );
                  }) || []
                }
              </Field>
            </Box>

            <FormControlLabel
              control={
                <Switch
                  checked={values.public}
                  onChange={() => setFieldValue('public', !values.public)}
                />
              }
              label={t('administration_cloud_plan_field_public')}
            />

            {!values.public && (
              <Box>
                <Typography sx={{ mt: 2 }}>
                  {t('administration_cloud_plan_form_organizations_title')}
                </Typography>
                <ForOrganizationsList
                  ids={values.forOrganizationIds}
                  setIds={(ids) => setFieldValue('forOrganizationIds', ids)}
                />
                {errors.forOrganizationIds && (
                  <Typography color="error">
                    {errors.forOrganizationIds}
                  </Typography>
                )}
              </Box>
            )}

            <Box display="flex" justifyContent="end" mt={4}>
              <LoadingButton
                loading={loading}
                variant="contained"
                color="primary"
                type="submit"
              >
                {t('global_form_save')}
              </LoadingButton>
            </Box>
          </Box>
        </Form>
      )}
    </Formik>
  );
}
