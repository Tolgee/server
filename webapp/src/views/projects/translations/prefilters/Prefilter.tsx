import { PrefilterActivity } from './PrefilterActivity';
import { PrefilterFailedJob } from './PrefilterFailedJob';
import { PrefilterType } from './usePrefilter';
import { getEe } from 'plugin/getEe';

type Props = {
  prefilter?: PrefilterType;
};

export const Prefilter = ({ prefilter }: Props) => {
  const {
    tasks: { PrefilterTask: EePrefilterTask },
  } = getEe();

  if (prefilter?.activity) {
    return <PrefilterActivity revisionId={prefilter.activity} />;
  } else if (prefilter?.failedJob) {
    return <PrefilterFailedJob jobId={prefilter.failedJob} />;
  } else if (prefilter?.task) {
    return <EePrefilterTask taskNumber={prefilter.task} />;
  }
  return null;
};
