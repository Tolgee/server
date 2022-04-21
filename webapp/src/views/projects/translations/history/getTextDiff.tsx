import { styled } from '@mui/material';
import clsx from 'clsx';
import { diffWordsWithSpace } from 'diff';
import { DiffInput } from './types';

const StyledDiff = styled('span')`
  & .removed {
    color: #7e0b00;
    text-decoration: line-through;
  }
  & .added {
    background: #00b80068;
  }
`;

export const getTextDiff = (input?: DiffInput) => {
  const oldInput = input?.old;
  const newInput = input?.new;
  if (oldInput && newInput) {
    const diffed = diffWordsWithSpace(oldInput, newInput);
    return (
      <StyledDiff>
        {diffed.map((part, i) => {
          return (
            <span
              key={i}
              className={clsx({ added: part.added, removed: part.removed })}
            >
              {part.value}
            </span>
          );
        })}
      </StyledDiff>
    );
  } else if (oldInput) {
    return (
      <StyledDiff>
        <span className="removed">{oldInput}</span>
      </StyledDiff>
    );
  } else if (newInput) {
    return (
      <StyledDiff>
        <span className="added">{newInput}</span>
      </StyledDiff>
    );
  }
};
