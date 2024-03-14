import React, { Suspense } from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import { StyledEngineProvider } from '@mui/material/styles';
import {
  DevTools,
  LanguageDetector,
  Tolgee,
  TolgeeProvider,
} from '@tolgee/react';
import { FormatIcu } from '@tolgee/format-icu';
import ReactDOM from 'react-dom';
import { QueryClientProvider } from 'react-query';
import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import { SnackbarProvider } from 'notistack';
import 'reflect-metadata';
import 'regenerator-runtime/runtime';

import { GlobalLoading, LoadingProvider } from 'tg.component/GlobalLoading';
import { GlobalErrorModal } from 'tg.component/GlobalErrorModal';
import { BottomPanelProvider } from 'tg.component/bottomPanel/BottomPanelContext';
import { GlobalProvider } from 'tg.globalContext/GlobalContext';
import { App } from './component/App';
import ErrorBoundary from './component/ErrorBoundary';
import { FullPageLoading } from './component/common/FullPageLoading';
import { ThemeProvider } from './ThemeProvider';

import reportWebVitals from './reportWebVitals';
import { dispatchService } from './service/DispatchService';
import configureStore from './store';
import { MuiLocalizationProvider } from 'tg.component/MuiLocalizationProvider';
import { languageStorage, queryClient } from './initialSetup';

const store = configureStore();

dispatchService.store = store;

const tolgee = Tolgee()
  .use(DevTools())
  .use(FormatIcu())
  .use(LanguageDetector())
  .use(languageStorage)
  .init({
    defaultLanguage: 'en',
    fallbackLanguage: 'en',
    apiUrl: import.meta.env.VITE_APP_TOLGEE_API_URL,
    apiKey: import.meta.env.VITE_APP_TOLGEE_API_KEY,
    staticData: {
      en: () => import('./i18n/en.json').then((m) => m.default),
      es: () => import('./i18n/es.json').then((m) => m.default),
      cs: () => import('./i18n/cs.json').then((m) => m.default),
      fr: () => import('./i18n/fr.json').then((m) => m.default),
      de: () => import('./i18n/de.json').then((m) => m.default),
      pt: () => import('./i18n/pt.json').then((m) => m.default),
      da: () => import('./i18n/da.json').then((m) => m.default),
    },
  });

const MainWrapper = () => {
  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider>
        <CssBaseline />
        <LoadingProvider>
          <GlobalLoading />
          <Suspense fallback={<FullPageLoading />}>
            <TolgeeProvider
              tolgee={tolgee}
              fallback={<FullPageLoading />}
              options={{ useSuspense: false }}
            >
              <BrowserRouter>
                <Provider store={store}>
                  <QueryClientProvider client={queryClient}>
                    {/* @ts-ignore */}
                    <ErrorBoundary>
                      <SnackbarProvider data-cy="global-snackbars">
                        <GlobalProvider>
                          <BottomPanelProvider>
                            <MuiLocalizationProvider>
                              <App />
                            </MuiLocalizationProvider>
                            <GlobalErrorModal />
                          </BottomPanelProvider>
                        </GlobalProvider>
                      </SnackbarProvider>
                    </ErrorBoundary>
                  </QueryClientProvider>
                </Provider>
              </BrowserRouter>
            </TolgeeProvider>
          </Suspense>
        </LoadingProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  );
};

ReactDOM.render(<MainWrapper />, document.getElementById('root'));

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
