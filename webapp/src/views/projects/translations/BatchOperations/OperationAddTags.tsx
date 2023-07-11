import { useState } from 'react';
import { ChevronRight } from '@mui/icons-material';
import { Box, styled } from '@mui/material';

import LoadingButton from 'tg.component/common/form/LoadingButton';

import { OperationProps } from './types';
import { Tag } from '../Tags/Tag';
import { TagInput } from '../Tags/TagInput';
import { useTranslate } from '@tolgee/react';

const StyledTags = styled('div')`
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  overflow: hidden;
  gap: 4px;
  margin: 6px 6px;
  position: relative;
  max-width: 450px;
`;

const StyledTag = styled(Tag)`
  border-color: ${({ theme }) => theme.palette.success.main};
`;

type Props = OperationProps;

export const OperationAddTags = ({ disabled, onStart }: Props) => {
  const { t } = useTranslate();
  // const project = useProject();

  // const selection = useTranslationsSelector((c) => c.selection);

  const [tags, setTags] = useState<string[]>([]);

  function handleAddTag(tag: string) {
    if (!tags.includes(tag)) {
      setTags([...tags, tag]);
    }
  }

  function handleDelete(tag: string) {
    setTags((tags) => tags.filter((t) => t !== tag));
  }

  // const batchLoadable = useApiMutation({
  //   url: '/v2/projects/{projectId}/start-batch-job/translate',
  //   method: 'post',
  // });

  // function handleSubmit() {
  //   batchLoadable.mutate(
  //     {
  //       path: { projectId: project.id },
  //       content: {
  //         'application/json': {
  //           keyIds: selection,
  //           targetLanguageIds: allLanguages
  //             ?.filter((l) => selectedLangs?.includes(l.tag))
  //             .map((l) => l.id),
  //           useMachineTranslation: true,
  //           useTranslationMemory: false,
  //           service: undefined,
  //         },
  //       },
  //     },
  //     {
  //       onSuccess(data) {
  //         onStart(data);
  //       },
  //     }
  //   );
  // }

  return (
    <Box display="flex" gap="10px">
      <StyledTags>
        {tags.map((tag) => (
          <StyledTag key={tag} name={tag} onDelete={() => handleDelete(tag)} />
        ))}
        <TagInput
          onAdd={handleAddTag}
          placeholder={t('batch_operation_tag_input_placeholder')}
          filtered={tags}
        />
      </StyledTags>
      <LoadingButton
        data-cy="batch-operations-submit-button"
        // loading={batchLoadable.isLoading}
        disabled={disabled || tags.length === 0}
        sx={{ minWidth: 0, minHeight: 0, width: 40, height: 40 }}
        // onClick={handleSubmit}
        variant="contained"
        color="primary"
      >
        <ChevronRight />
      </LoadingButton>
    </Box>
  );
};
