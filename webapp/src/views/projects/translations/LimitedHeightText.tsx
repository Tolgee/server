import { useState, useRef, useEffect, RefObject } from 'react';
import { makeStyles } from '@material-ui/core';

type Props = {
  maxLines?: number | undefined;
};

const useStyles = makeStyles({
  '@keyframes fadeIn': {
    from: { opacity: 0 },
    to: { opacity: 1 },
  },
  container: {
    position: 'relative',
    display: 'flex',
    overflow: 'hidden',
    lineHeight: '1.2rem',

    // clever word breaking
    overflowWrap: 'break-word',
    wordWrap: 'break-word',
    'word-break': 'break-all',
    wordBreak: 'break-word',

    // Adds a hyphen where the word breaks
    '-ms-hyphens': 'auto',
    '-moz-hyphens': 'auto',
    '-webkit-hyphens': 'auto',
    hyphens: 'auto',
    animation: 'fadeIn .2s ease-in-out',
  },
});

export const LimitedHeightText: React.FC<Props> = ({ maxLines, children }) => {
  const classes = useStyles();
  const textRef = useRef<HTMLDivElement>();
  const [expandable, setExpandable] = useState<boolean>();

  const detectExpandability = () => {
    const textElement = textRef.current;
    if (textElement != null) {
      const clone = textRef.current?.cloneNode(true) as HTMLDivElement;
      clone.style.position = 'absolute';
      clone.style.visibility = 'hidden';
      textElement.parentElement?.append(clone);
      setExpandable(
        textElement.clientWidth < clone.clientWidth ||
          textElement.clientHeight < clone.scrollHeight
      );
      textElement.parentElement?.removeChild(clone);
    }
  };

  useEffect(() => {
    detectExpandability();
  });

  const gradient = expandable
    ? `linear-gradient(to top, rgba(0,0,0,0) 0%, rgba(0,0,0,0.87) 1.2rem, rgba(0,0,0,0.87) ${
        100 / (maxLines || 100)
      }%, black 100%)`
    : undefined;

  return (
    <div
      className={classes.container}
      ref={textRef as RefObject<HTMLDivElement>}
      style={{
        maxHeight: maxLines ? `calc(1.2rem * ${maxLines})` : undefined,
        WebkitMaskImage: gradient,
        maskImage: gradient,
      }}
    >
      {children}
    </div>
  );
};
