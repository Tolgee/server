import { components } from 'tg.service/apiSchema.generated';
import { DeletableKeyWithTranslationsModelType } from '../../context/types';
import { LanguageModel } from 'tg.component/PermissionsSettings/types';

export type ProjectModel = components['schemas']['ProjectModel'];
export type TranslationViewModel =
  components['schemas']['TranslationViewModel'];

export type PanelContentData = {
  project: ProjectModel;
  keyData: DeletableKeyWithTranslationsModelType;
  language: LanguageModel;
  baseLanguage: LanguageModel;
  activeVariant: string | undefined;
  editEnabled: boolean;
};

export type PanelContentProps = PanelContentData & {
  setItemsCount: (value: number | undefined) => void;
  setValue: (value: string) => void;
};

export type PanelConfig = {
  id: string;
  icon: React.ReactNode;
  name: React.ReactNode;
  component: React.FC<PanelContentProps>;
  itemsCountComponent?: React.FC<PanelContentData>;
  displayPanel?: (value: PanelContentData) => boolean;
};
