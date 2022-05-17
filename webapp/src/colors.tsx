import { grey } from '@mui/material/colors';

export type Emphasis = {
  50: string;
  100: string;
  200: string;
  300: string;
  400: string;
  500: string;
  600: string;
  700: string;
  800: string;
  900: string;
  A100: string;
  A200: string;
  A400: string;
  A700: string;
};

export type Editor = {
  function: string;
  other: string;
  main: string;
};

export type Activity = {
  removed: string;
  added: string;
  addedHighlight: string;
};

export const colors = {
  light: {
    white: '#fff',
    primary: '#822B55',
    secondary: '#2B5582',
    default: grey[700],
    navbarBackground: '#822B55',
    text: '#000000',
    textSecondary: '#808080',
    divider1: '#00000014',
    divider2: '#0000001e',
    background: '#ffffff',
    cellSelected1: '#f5f5f5',
    cellSelected2: '#ffffff',
    backgroundPaper: '#ffffff',
    emphasis: grey as Emphasis,
    activity: {
      removed: '#822B55',
      added: '#006900',
      addedHighlight: '#c4f0da',
    } as Activity,
    editor: {
      function: '#007300',
      other: '#002bff',
      main: '#000000',
    } as Editor,
  },
  dark: {
    white: '#dddddd',
    primary: '#ff6995',
    secondary: '#aed5ff',
    default: grey[400],
    navbarBackground: '#182230',
    text: '#dddddd',
    textSecondary: '#acacac',
    divider1: '#eeeeee14',
    divider2: '#ffffff1e',
    background: '#1f2d40',
    backgroundPaper: '#1e2b3e',
    cellSelected1: '#24344b',
    cellSelected2: '#283a53',
    emphasis: {
      50: '#233043',
      100: '#243245',
      200: '#2c3c52',
      300: '#374961',
      400: '#576d8a',
      500: '#8f9eb1',
      600: '#b4bbc3',
      700: '#eeeeee',
      800: '#f5f5f5',
      900: '#fafafa',
      A100: '#616161',
      A200: '#bdbdbd',
      A400: '#eeeeee',
      A700: '#f5f5f5',
    } as Emphasis,
    activity: {
      removed: '#ff7e7e',
      added: '#7fe07f',
      addedHighlight: '#006300',
    } as Activity,
    editor: {
      function: '#9ac99a',
      other: '#99aaff',
      main: '#eeeeee',
    } as Editor,
  },
} as const;
