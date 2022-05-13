import { T } from '@tolgee/react';
import { Box, styled } from '@mui/material';
import { Activity, Reference } from './types';
import { AnyReference } from './references/AnyReference';

const StyledContainer = styled('div')`
  display: flex;
  gap: 5px;
  overflow: hidden;
`;

const StyledText = styled(Box)`
  white-space: nowrap;
`;

type Props = {
  activity: Activity;
};

export const ActivityTitle: React.FC<Props> = ({ activity }) => {
  let references = 0;
  const filteredReferences: Reference[] = [];
  const titleReferences = activity.options?.titleReferences;

  activity.references.forEach((ref) => {
    references += 1;
    if (!titleReferences || titleReferences.includes(ref.type)) {
      filteredReferences.push(ref);
    }
  });

  const titleParameters = {
    references,
  };

  Object.entries(activity.counts || {}).forEach(([entity, value]) => {
    titleParameters[`${entity}Count`] = value;
  });

  return (
    <StyledContainer>
      <StyledText>
        {activity.translationKey ? (
          <T keyName={activity.translationKey} parameters={titleParameters}></T>
        ) : (
          activity.type
        )}
      </StyledText>
      <AnyReference data={filteredReferences} />
    </StyledContainer>
  );
};
