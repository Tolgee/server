// This file was exported from Figma design system, do not edit it manually

export const tolgeeColors = {
  pink: {
    '50': '#fdecf2',
    '100': '#f9c4d6',
    '200': '#f6a7c2',
    '300': '#f27fa6',
    '400': '#f06695',
    '500': '#ec407a',
    '600': '#d73a6f',
    '700': '#a82d57',
    '800': '#822343',
    '900': '#631b33',
  },
  grey: {
    '50': '#f0f2f4',
    '100': '#d1d6dc',
    '200': '#bbc2cb',
    '300': '#9da7b4',
    '400': '#8995a5',
    '500': '#6c7b8f',
    '600': '#627082',
    '700': '#4d5b6e',
    '750': '#36455b',
    '800': '#2c3c52',
    '825': '#27364b',
    '850': '#243245',
    '900': '#1f2d40',
    '950': '#182230',
  },
  lightGrey: {
    '50': '#fdfdff',
    '100': '#fbfbfd',
    '200': '#f9f9fb',
    '300': '#f6f6f8',
    '400': '#f4f4f6',
    '500': '#f1f1f4',
    '600': '#dbdbde',
    '700': '#ababae',
    '800': '#858587',
    '900': '#656567',
  },
  green: {
    '50': '#e6faf0',
    '100': '#b0efd1',
    '200': '#8ae7bb',
    '300': '#54dc9d',
    '400': '#33d589',
    '500': '#00cb6c',
    '600': '#00b962',
    '700': '#00904d',
    '800': '#00703b',
    '900': '#00552d',
  },
  teal: {
    '50': '#e8fcf8',
    '100': '#bef3e9',
    '200': '#99e5d6',
    '300': '#7ad3c1',
    '400': '#35c4b0',
    '500': '#00af9a',
    '600': '#009b85',
    '700': '#008371',
    '800': '#006b5b',
    '900': '#004437',
  },
  yellow: {
    '500': '#ffce00',
    '800': '#cca500',
  },
  red: {
    '50': '#ffe5ea',
    '100': '#ffb7c4',
    '200': '#ff8a9f',
    '300': '#ff5c79',
    '400': '#ff2e53',
    '500': '#ff002e',
    '600': '#d60027',
    '700': '#ad001f',
    '800': '#850018',
    '900': '#5c0011',
  },
  lightBlue: {
    '50': '#e1f5fe',
    '100': '#b3e5fc',
    '200': '#81d4fa',
    '300': '#4fc3f7',
    '400': '#29b6f6',
    '500': '#03a9f4',
    '600': '#039be5',
    '700': '#0288d1',
    '800': '#0277bd',
    '900': '#01579b',
  },
  orange: {
    '50': '#fff3e0',
    '100': '#ffe0b2',
    '200': '#ffcc80',
    '300': '#ffb74d',
    '400': '#ffa726',
    '500': '#ff9800',
    '600': '#fb8c00',
    '700': '#f57c00',
    '800': '#ef6c00',
    '900': '#e65100',
  },
  neutral: {
    white: '#ffffff',
    black: '#000000',
  },
} as const;

export const tolgeePalette = {
  Light: {
    logo: {
      symbol: tolgeeColors['pink']['500'],
      text: tolgeeColors['grey']['800'],
    },
    icon: {
      primary: '#1f2d40de',
      secondary: '#1f2d4099',
      tertiary: '#1f2d4066',
      menuDefault: tolgeeColors['grey']['600'],
      menuHover: tolgeeColors['grey']['800'],
      menuSelected: tolgeeColors['pink']['600'],
      menuSelectedBg: '#1f2d400f',
    },
    state: {
      untranslated: tolgeeColors['grey']['200'],
      translated: tolgeeColors['yellow']['500'],
      reviewed: tolgeeColors['green']['600'],
    },
    translation: {
      highlighted: tolgeeColors['green']['100'],
    },
    border: {
      primary: '#1f2d403b',
      secondary: '#1f2d401f',
      soft: tolgeeColors['grey']['50'],
    },
    text: {
      primary: '#1f2d40de',
      secondary: '#1f2d4099',
      tertiary: '#1f2d4066',
      disabled: '#1f2d4061',
      _states: {
        hover: '#1f2d4008',
        selected: '#1f2d400f',
        focus: '#1f2d4014',
        focusVisible: '#1f2d404d',
      },
    },
    primary: {
      main: tolgeeColors['pink']['500'],
      dark: '#a52c55',
      light: '#ef6694',
      contrast: '#ffffff',
      _states: {
        hover: '#ec407a0a',
        selected: '#ec407a14',
        focus: '#ec407a1f',
        focusVisible: '#ec407a4d',
        outlinedBorder: '#ec407a80',
      },
    },
    secondary: {
      main: tolgeeColors['teal']['500'],
      dark: '#007a6b',
      light: '#33bfae',
      contrast: '#ffffff',
      _states: {
        hover: '#00af9a0a',
        selected: '#00af9a14',
        focus: '#00af9a1f',
        focusVisible: '#00af9a4d',
        outlinedBorder: '#00af9a80',
      },
    },
    action: {
      active: '#1f2d408f',
      hover: '#1f2d4008',
      selected: '#1f2d400f',
      focus: '#1f2d4014',
      disabled: '#1f2d4061',
      disabledBackground: '#1f2d401f',
    },
    error: {
      main: tolgeeColors['red']['600'],
      dark: tolgeeColors['red']['800'],
      light: tolgeeColors['red']['400'],
      contrast: '#ffffff',
      _states: {
        hover: '#d600270a',
        selected: '#d6002714',
        focusVisible: '#d600274d',
        outlinedBorder: '#d6002780',
      },
    },
    warning: {
      main: tolgeeColors['orange']['800'],
      dark: tolgeeColors['orange']['900'],
      light: tolgeeColors['orange']['900'],
      contrast: '#ffffff',
      _states: {
        hover: '#ef6c000a',
        selected: '#ef6c0014',
        focusVisible: '#ef6c004d',
        outlinedBorder: '#ef6c0080',
      },
    },
    info: {
      main: tolgeeColors['lightBlue']['700'],
      dark: tolgeeColors['lightBlue']['900'],
      light: tolgeeColors['lightBlue']['500'],
      contrast: '#ffffff',
      _states: {
        hover: '#0288d10a',
        selected: '#0288d114',
        focusVisible: '#0288d14d',
        outlinedBorder: '#0288d180',
      },
    },
    success: {
      main: tolgeeColors['green']['600'],
      dark: '#008144',
      light: '#33c781',
      contrast: '#ffffff',
      _states: {
        hover: '#00b9620a',
        selected: '#00b96214',
        focusVisible: '#00b9624d',
        outlinedBorder: '#00b96280',
      },
    },
    common: {
      white_states: {
        main: '#ffffff',
        hover: '#ffffff0a',
        selected: '#ffffff14',
        focus: '#ffffff1f',
        focusVisible: '#ffffff4d',
        outlinedBorder: '#ffffff80',
      },
      black_states: {
        main: '#1f2d40',
        hover: '#1f2d400a',
        selected: '#1f2d4014',
        focus: '#1f2d401f',
        focusVisible: '#1f2d404d',
        outlinedBorder: '#1f2d4080',
      },
    },
    background: {
      default: tolgeeColors['lightGrey']['50'],
      hover: '#1f2d4005',
      selected: '#1f2d400a',
      onDefault: '#ffffff',
      onDefaultGrey: '#1f2d400a',
      'paper-1': '#ffffff',
      'paper-2': '#ffffff',
      'paper-3': '#f9f9fb',
      floating: tolgeeColors['grey']['50'],
      'paper-elevation-1': '#ffffff',
      'paper-elevation-2': '#ffffff',
      'paper-elevation-3': '#ffffff',
      'paper-elevation-4': '#ffffff',
      'paper-elevation-5': '#ffffff',
      'paper-elevation-6': '#ffffff',
      'paper-elevation-7': '#ffffff',
      'paper-elevation-8': '#ffffff',
      'paper-elevation-9': '#ffffff',
      'paper-elevation-10': '#ffffff',
      'paper-elevation-11': '#ffffff',
      'paper-elevation-12': '#ffffff',
      'paper-elevation-13': '#ffffff',
      'paper-elevation-14': '#ffffff',
      'paper-elevation-15': '#ffffff',
      'paper-elevation-16': '#ffffff',
      'paper-elevation-17': '#ffffff',
      'paper-elevation-18': '#ffffff',
      'paper-elevation-19': '#ffffff',
      'paper-elevation-20': '#ffffff',
      'paper-elevation-21': '#ffffff',
      'paper-elevation-22': '#ffffff',
      'paper-elevation-23': '#ffffff',
      'paper-elevation-24': '#ffffff',
    },
    elevation: {
      outlined: '#e0e0e0',
      pricingActive: '#00af9a26',
      pricing: '#1f2d400d',
    },
    _components: {
      task: {
        state: {
          review: '#00b9624d',
          translate: '#1f2d4014',
        },
      },
      alert: {
        success: {
          background: tolgeeColors['green']['50'],
          color: tolgeeColors['green']['900'],
        },
        info: {
          background: '#e5f6fd',
          color: '#014361',
        },
        warning: {
          background: '#fff4e5',
          color: '#663c00',
        },
        error: {
          background: tolgeeColors['red']['50'],
          color: tolgeeColors['red']['900'],
        },
      },
      avatar: {
        fill: tolgeeColors['grey']['200'],
      },
      appBar: {
        defaultFill: tolgeeColors['neutral']['white'],
      },
      backdrop: {
        fill: '#1f2d4080',
      },
      dropzone: {
        active: '#e8fcf8f0',
      },
      breadcrumbs: {
        collapseFill: tolgeeColors['grey']['50'],
        link: tolgeeColors['pink']['500'],
        actual: '#000000',
      },
      editor: {
        fill: tolgeeColors['neutral']['white'],
      },
      chip: {
        defaultFill: '#1f2d400f',
        defaultFocusFill: '#1f2d4033',
        defaultHoverFill: '#1f2d401f',
        defaultCloseFill: '#1f2d40',
        defaultEnabledBorder: tolgeeColors['grey']['200'],
        placeHolderPluralFill: tolgeeColors['teal']['100'],
        placeHolderPluralBorder: tolgeeColors['teal']['300'],
        placeHolderPluralText: tolgeeColors['teal']['800'],
        placeHolder: tolgeeColors['pink']['100'],
        placeHolderBorder: tolgeeColors['pink']['300'],
        placeHolderText: tolgeeColors['pink']['800'],
      },
      input: {
        filled: {
          hoverFill: '#1f2d4017',
          enabledFill: '#1f2d400f',
        },
        outlined: {
          hoverBorder: '#1f2d40',
          enabledBorder: '#1f2d403b',
          fill: '#ffffffe5',
          focusedFill: '#ffffff',
        },
        standard: {
          hoverBorder: '#1f2d40',
          enabledBorder: '#1f2d406b',
        },
      },
      noticeBar: {
        defaultFill: tolgeeColors['teal']['100'],
        defaultColor: tolgeeColors['teal']['900'],
        link: tolgeeColors['teal']['600'],
        linkHover: tolgeeColors['teal']['700'],
        importantFill: tolgeeColors['pink']['50'],
        importantColor: tolgeeColors['pink']['700'],
        importantLink: tolgeeColors['pink']['500'],
        importantLinkHover: tolgeeColors['pink']['600'],
      },
      progressbar: {
        background: tolgeeColors['grey']['50'],
        pricing: {
          low: tolgeeColors['red']['400'],
          over: tolgeeColors['yellow']['500'],
          overForbidden: tolgeeColors['red']['800'],
          sufficient: tolgeeColors['teal']['500'],
        },
        task: {
          inProgress: tolgeeColors['lightBlue']['500'],
          done: tolgeeColors['green']['600'],
        },
      },
      rating: {
        activeFill: '#ffb400',
        enabledBorder: '#1f2d403b',
      },
      snackbar: {
        fill: tolgeeColors['grey']['800'],
      },
      stepper: {
        connector: tolgeeColors['grey']['200'],
      },
      switch: {
        slideFill: tolgeeColors['grey']['900'],
        knowFillDisabled: tolgeeColors['grey']['100'],
        knobFillEnabled: tolgeeColors['grey']['50'],
      },
      tooltip: {
        fill: '#627082e5',
      },
    },
    divider: '#1f2d401f',
    _native: {
      'scrollbar-bg': tolgeeColors['grey']['200'],
    },
  },
  Dark: {
    logo: {
      symbol: tolgeeColors['grey']['50'],
      text: tolgeeColors['grey']['50'],
    },
    icon: {
      primary: '#ffffffde',
      secondary: '#ffffffb2',
      tertiary: '#ffffff66',
      menuDefault: tolgeeColors['grey']['200'],
      menuHover: tolgeeColors['grey']['50'],
      menuSelected: tolgeeColors['pink']['400'],
      menuSelectedBg: '#ffffff1f',
    },
    state: {
      untranslated: tolgeeColors['grey']['400'],
      translated: tolgeeColors['yellow']['800'],
      reviewed: tolgeeColors['green']['700'],
    },
    translation: {
      highlighted: tolgeeColors['green']['700'],
    },
    border: {
      primary: '#ffffff3b',
      secondary: '#ffffff29',
      soft: tolgeeColors['grey']['825'],
    },
    text: {
      primary: '#ffffffde',
      secondary: '#ffffffb2',
      tertiary: '#ffffff66',
      disabled: '#ffffff61',
      _states: {
        hover: '#ffffff14',
        selected: '#ffffff29',
        focus: '#ffffff1f',
        focusVisible: '#ffffff4d',
      },
    },
    primary: {
      main: tolgeeColors['pink']['400'],
      dark: '#a84768',
      light: '#f384aa',
      contrast: '#000000de',
      _states: {
        hover: '#f0669514',
        selected: '#f0669529',
        focus: '#f0669533',
        focusVisible: '#f066954d',
        outlinedBorder: '#f0669580',
      },
    },
    secondary: {
      main: tolgeeColors['teal']['200'],
      dark: '#6ba095',
      light: '#adeade',
      contrast: '#000000de',
      _states: {
        hover: '#99e5d614',
        selected: '#99e5d629',
        focus: '#99e5d61f',
        focusVisible: '#99e5d64d',
        outlinedBorder: '#99e5d680',
      },
    },
    action: {
      active: '#ffffff8f',
      hover: '#ffffff0f',
      selected: '#ffffff29',
      focus: '#ffffff1f',
      disabled: '#ffffff61',
      disabledBackground: '#ffffff1f',
    },
    error: {
      main: tolgeeColors['red']['300'],
      dark: tolgeeColors['red']['700'],
      light: tolgeeColors['red']['300'],
      contrast: '#ffffff',
      _states: {
        hover: '#f9c4d614',
        selected: '#f9c4d629',
        focusVisible: '#f9c4d64d',
        outlinedBorder: '#f9c4d680',
      },
    },
    warning: {
      main: tolgeeColors['orange']['400'],
      dark: tolgeeColors['orange']['700'],
      light: tolgeeColors['orange']['300'],
      contrast: '#000000de',
      _states: {
        hover: '#ffa72614',
        selected: '#ffa72629',
        focusVisible: '#ffa7264d',
        outlinedBorder: '#ffa72680',
      },
    },
    info: {
      main: tolgeeColors['lightBlue']['400'],
      dark: tolgeeColors['lightBlue']['700'],
      light: tolgeeColors['lightBlue']['300'],
      contrast: '#000000de',
      _states: {
        hover: '#29b6f614',
        selected: '#29b6f629',
        focusVisible: '#29b6f64d',
        outlinedBorder: '#29b6f680',
      },
    },
    success: {
      main: tolgeeColors['green']['400'],
      dark: '#23955f',
      light: '#5bdda0',
      contrast: '#000000de',
      _states: {
        hover: '#33d58914',
        selected: '#33d58929',
        focusVisible: '#33d5894d',
        outlinedBorder: '#33d58980',
      },
    },
    common: {
      white_states: {
        main: '#ffffff',
        hover: '#ffffff14',
        selected: '#ffffff29',
        focus: '#ffffff1f',
        focusVisible: '#ffffff4d',
        outlinedBorder: '#ffffff80',
      },
      black_states: {
        main: '#1f2d40',
        hover: '#1f2d4014',
        selected: '#1f2d4029',
        focus: '#1f2d401f',
        focusVisible: '#1f2d404d',
        outlinedBorder: '#1f2d4080',
      },
    },
    background: {
      default: tolgeeColors['grey']['900'],
      hover: '#ffffff05',
      selected: '#ffffff0a',
      onDefault: '#182230b2',
      onDefaultGrey: '#ffffff0a',
      'paper-1': tolgeeColors['grey']['800'],
      'paper-2': tolgeeColors['grey']['825'],
      'paper-3': '#243245',
      floating: tolgeeColors['grey']['800'],
      'paper-elevation-1': tolgeeColors['grey']['750'],
      'paper-elevation-2': '#232323',
      'paper-elevation-3': '#252525',
      'paper-elevation-4': '#272727',
      'paper-elevation-5': '#2a2a2a',
      'paper-elevation-6': '#2c2c2c',
      'paper-elevation-7': '#2c2c2c',
      'paper-elevation-8': '#2e2e2e',
      'paper-elevation-9': '#2e2e2e',
      'paper-elevation-10': '#313131',
      'paper-elevation-11': '#313131',
      'paper-elevation-12': '#333333',
      'paper-elevation-13': '#333333',
      'paper-elevation-14': '#333333',
      'paper-elevation-15': '#333333',
      'paper-elevation-16': '#363636',
      'paper-elevation-17': '#363636',
      'paper-elevation-18': '#363636',
      'paper-elevation-19': '#363636',
      'paper-elevation-20': '#383838',
      'paper-elevation-21': '#383838',
      'paper-elevation-22': '#383838',
      'paper-elevation-23': '#383838',
      'paper-elevation-24': '#383838',
    },
    elevation: {
      outlined: '#000000',
      pricingActive: '#00af9a33',
      pricing: '#1f2d400d',
    },
    _components: {
      task: {
        state: {
          review: '#33d5894d',
          translate: '#ffffff1f',
        },
      },
      alert: {
        success: {
          background: tolgeeColors['green']['900'],
          color: tolgeeColors['green']['100'],
        },
        info: {
          background: '#071318',
          color: '#b8e7fb',
        },
        warning: {
          background: '#191207',
          color: '#ffe2b7',
        },
        error: {
          background: tolgeeColors['grey']['950'],
          color: tolgeeColors['red']['50'],
        },
      },
      avatar: {
        fill: tolgeeColors['grey']['600'],
      },
      appBar: {
        defaultFill: tolgeeColors['grey']['950'],
      },
      backdrop: {
        fill: '#101926b2',
      },
      dropzone: {
        active: '#234b56f0',
      },
      breadcrumbs: {
        collapseFill: tolgeeColors['grey']['600'],
        link: tolgeeColors['pink']['400'],
        actual: '#000000',
      },
      editor: {
        fill: '#ffffff0a',
      },
      chip: {
        defaultFill: '#ffffff1f',
        defaultFocusFill: '#ffffff33',
        defaultHoverFill: '#ffffff29',
        defaultCloseFill: '#ffffff',
        defaultEnabledBorder: '#2c3c5200',
        placeHolderPluralFill: tolgeeColors['teal']['700'],
        placeHolderPluralBorder: '#00837100',
        placeHolderPluralText: tolgeeColors['grey']['50'],
        placeHolder: '#d73a6f80',
        placeHolderBorder: '#d73a6f00',
        placeHolderText: tolgeeColors['grey']['50'],
      },
      input: {
        filled: {
          hoverFill: '#ffffff1f',
          enabledFill: '#ffffff17',
        },
        outlined: {
          hoverBorder: '#ffffff',
          enabledBorder: '#ffffff3b',
          fill: '#ffffff08',
          focusedFill: '#ffffff0f',
        },
        standard: {
          hoverBorder: '#ffffff',
          enabledBorder: '#ffffff6b',
        },
      },
      noticeBar: {
        defaultFill: tolgeeColors['teal']['700'],
        defaultColor: tolgeeColors['teal']['100'],
        link: tolgeeColors['grey']['100'],
        linkHover: tolgeeColors['grey']['50'],
        importantFill: tolgeeColors['pink']['800'],
        importantColor: tolgeeColors['pink']['200'],
        importantLink: tolgeeColors['pink']['50'],
        importantLinkHover: tolgeeColors['grey']['50'],
      },
      progressbar: {
        background: tolgeeColors['grey']['700'],
        pricing: {
          low: tolgeeColors['red']['400'],
          over: tolgeeColors['yellow']['500'],
          overForbidden: tolgeeColors['red']['600'],
          sufficient: tolgeeColors['teal']['400'],
        },
        task: {
          inProgress: tolgeeColors['lightBlue']['700'],
          done: tolgeeColors['green']['600'],
        },
      },
      rating: {
        activeFill: '#ffb400',
        enabledBorder: '#1f2d403b',
      },
      snackbar: {
        fill: '#000000',
      },
      stepper: {
        connector: tolgeeColors['grey']['500'],
      },
      switch: {
        slideFill: '#ffffff61',
        knowFillDisabled: tolgeeColors['grey']['600'],
        knobFillEnabled: tolgeeColors['grey']['300'],
      },
      tooltip: {
        fill: '#627082e5',
      },
    },
    divider: '#ffffff1f',
    _native: {
      'scrollbar-bg': tolgeeColors['grey']['700'],
    },
  },
} as const;
