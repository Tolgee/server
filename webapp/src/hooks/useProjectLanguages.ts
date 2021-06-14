import { useSelector } from 'react-redux';
import { AppState } from '../store';
import { GlobalError } from '../error/GlobalError';
import { components } from '../service/apiSchema.generated';

export const useProjectLanguages =
  (): components['schemas']['LanguageModel'][] => {
    const languagesLoadable = useSelector(
      (state: AppState) => state.languages.loadables.globalList
    );

    if (!languagesLoadable.data?._embedded?.languages) {
      throw new GlobalError(
        'Unexpected error',
        'No data in loadable? Did you use provider before using hook?'
      );
    }

    return languagesLoadable.data._embedded.languages;
  };
