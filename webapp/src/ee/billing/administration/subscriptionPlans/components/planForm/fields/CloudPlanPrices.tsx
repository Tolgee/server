import { FC, useEffect, useState } from 'react';
import { TextField } from 'tg.component/common/form/fields/TextField';
import { Box, Typography } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { CloudPlanFormData } from '../CloudPlanFormBase';
import { useFormikContext } from 'formik';

type CloudPlanPricesProps = {
  parentName: string | undefined;
};

export const CloudPlanPrices: FC<CloudPlanPricesProps> = ({ parentName }) => {
  const { t } = useTranslate();

  const { setFieldValue, values } = useFormikContext<CloudPlanFormData>();

  const [nonZeroPrices, setNonZeroPrices] =
    useState<CloudPlanFormData['prices']>();

  function setPriceValuesZero() {
    setNonZeroPrices(values.prices);
    const zeroPrices = Object.entries(values.prices).reduce(
      (acc, [key]) => ({ ...acc, [key]: 0 }),
      {}
    );

    setFieldValue(`${parentName}prices`, zeroPrices);
  }

  function setPriceValuesNonZero() {
    setFieldValue(`${parentName}prices`, nonZeroPrices);
  }

  useEffect(() => {
    if (values.free) {
      setPriceValuesZero();
      return;
    }
    setPriceValuesNonZero();
  }, [values.free]);

  return (
    <>
      <Typography sx={{ mt: 2 }}>
        {t('administration_cloud_plan_form_prices_title')}
      </Typography>
      <Box
        display="grid"
        gridTemplateColumns="repeat(4, 1fr)"
        gap={2}
        sx={{ mt: 1 }}
      >
        <TextField
          name={`${parentName}prices.subscriptionMonthly`}
          size="small"
          data-cy="administration-cloud-plan-field-price-monthly"
          label={t('administration_cloud_plan_field_price_monthly')}
          type="number"
          fullWidth
        />
        <TextField
          name={`${parentName}prices.subscriptionYearly`}
          size="small"
          data-cy="administration-cloud-plan-field-price-yearly"
          label={t('administration_cloud_plan_field_price_yearly')}
          type="number"
          fullWidth
        />
        <TextField
          name={`${parentName}prices.perThousandTranslations`}
          size="small"
          data-cy="administration-cloud-plan-field-price-per-thousand-translations"
          label={t(
            'administration_cloud_plan_field_price_per_thousand_translations'
          )}
          type="number"
          fullWidth
          disabled={values.type !== 'PAY_AS_YOU_GO'}
        />
        <TextField
          name={`${parentName}prices.perThousandMtCredits`}
          size="small"
          data-cy="administration-cloud-plan-field-price-per-thousand-mt-credits"
          label={t(
            'administration_cloud_plan_field_price_per_thousand_mt_credits'
          )}
          type="number"
          fullWidth
          disabled={values.type !== 'PAY_AS_YOU_GO'}
        />
      </Box>
    </>
  );
};