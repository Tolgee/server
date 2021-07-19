import { FunctionComponent } from 'react';
import {
  Box,
  Checkbox,
  ListItemText,
  MenuItem,
  Select,
  Typography,
} from '@material-ui/core';
import { T } from '@tolgee/react';
import { container } from 'tsyringe';
import FormControl from '@material-ui/core/FormControl';
import makeStyles from '@material-ui/core/styles/makeStyles';

import { MessageService } from 'tg.service/MessageService';

const useStyles = makeStyles({
  input: {
    minWidth: 200,
    height: 40,
  },
});

type Language = {
  value: string;
  label: string;
};
export interface LanguagesMenuProps {
  onChange: (value: string[]) => void;
  languages: Language[];
  value: string[];
  context: string;
}

const messaging = container.resolve(MessageService);

export const LanguagesMenu: FunctionComponent<LanguagesMenuProps> = (props) => {
  const classes = useStyles();

  const langsChange = (e) => {
    if (e.target.value < 1) {
      messaging.error(<T>set_at_least_one_language_error</T>);
      return;
    }
    props.onChange(e.target.value);
  };

  const menuProps = {
    variant: 'menu',
    PaperProps: {
      style: {
        width: 250,
      },
    },
    id: `language-select-${props.context}-menu`,
  } as const;

  return (
    <Box display="flex" alignItems="right">
      <FormControl
        data-cy="translations-language-select-form-control"
        variant="outlined"
        size="small"
      >
        <Select
          labelId={`languages-${props.context}`}
          id={`languages-select-${props.context}`}
          multiple
          value={props.value}
          onChange={(e) => langsChange(e)}
          renderValue={(selected) => (
            <Typography color="textPrimary" variant="body2">
              {(selected as string[]).join(', ')}
            </Typography>
          )}
          className={classes.input}
          MenuProps={menuProps}
          margin="dense"
        >
          {props.languages.map((lang) => (
            <MenuItem key={lang.value} value={lang.value}>
              <Checkbox
                checked={props.value.indexOf(lang.value) > -1}
                size="small"
              />
              <ListItemText primary={lang.label} />
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    </Box>
  );
};
