import { Box, styled, Typography } from '@mui/material';
import { T } from '@tolgee/react';
import React from 'react';
import TolgeeLogo from 'tg.svgs/tolgeeLogo.svg?react';
import IcuLogo from 'tg.svgs/logos/icu.svg?react';
import PhoLogo from 'tg.svgs/logos/php.svg?react';
import CLogo from 'tg.svgs/logos/c.svg?react';
import PythonLogo from 'tg.svgs/logos/python.svg?react';
import AppleLogo from 'tg.svgs/logos/apple.svg?react';
import AndroidLogo from 'tg.svgs/logos/android.svg?react';
import FluttrerLogo from 'tg.svgs/logos/flutter.svg?react';

const TechLogo = ({ svg }: { svg: React.ReactNode }) => {
  return (
    <Box
      sx={(theme) => ({
        color: theme.palette.tokens.TEXT_SECONDARY,
        height: '20px',
      })}
    >
      {svg}
    </Box>
  );
};

const FORMATS = [
  {
    name: 'JSON',
    logo: <TolgeeLogo />,
  },
  {
    name: 'XLIFF',
    logo: <IcuLogo />,
  },
  { name: 'PO PHP', logo: <PhoLogo /> },
  { name: 'PO C/C++', logo: <CLogo /> },
  { name: 'PO Python', logo: <PythonLogo /> },
  { name: 'Apple Strings', logo: <AppleLogo /> },
  { name: 'Apple Stringsdict', logo: <AppleLogo /> },
  { name: 'Apple XLIFF', logo: <AppleLogo /> },
  { name: 'Android XML', logo: <AndroidLogo /> },
  { name: 'Flutter ARB', logo: <FluttrerLogo /> },
];

export const ImportSupportedFormats = () => {
  return (
    <>
      <Typography
        variant="body1"
        sx={(theme) => ({
          color: theme.palette.tokens.TEXT_SECONDARY,
          marginBottom: '16px',
          marginTop: '16px',
          textAlign: 'center',
        })}
      >
        <T keyName="import_file_supported_formats_title" />
      </Typography>
      <StyledContainer>
        {FORMATS.map((f) => (
          <Item key={f.name} name={f.name} logo={f.logo} />
        ))}
      </StyledContainer>
    </>
  );
};

const Item = ({ name, logo }: { name: string; logo?: React.ReactNode }) => {
  return (
    <StyledItem>
      <TechLogo svg={logo} />
      {name}
    </StyledItem>
  );
};

const StyledItem = styled('div')`
  display: inline-flex;
  padding: 8px 12px;
  justify-content: center;
  align-items: center;
  gap: 4px;
  border-radius: 12px;
  border: 1px solid
    ${({ theme }) => theme.palette.tokens.LINE_BORDER_LINE_PRIMARY};
  color: ${({ theme }) => theme.palette.tokens.TEXT_SECONDARY};
  font-size: 15px;
`;

const StyledContainer = styled('div')`
  display: flex;
  max-width: 795px;
  justify-content: center;
  align-items: center;
  align-content: center;
  gap: 8px;
  flex-shrink: 0;
  flex-wrap: wrap;
`;