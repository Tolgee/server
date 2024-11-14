import { useApiQuery, useApiMutation } from 'tg.service/http/useQueryApi';
import { useProject } from 'tg.hooks/useProject';
import { LanguageInfoModel, MtServiceInfo, OnMtChange } from './types';
import { useMemo } from 'react';
import { supportsFormality } from './supportsFormality';

export const useMachineTranslationSettings = () => {
  const project = useProject();

  const languages = useApiQuery({
    url: '/v2/projects/{projectId}/languages',
    method: 'get',
    path: { projectId: project.id },
    query: { size: 1000 },
  });

  const mtSettings = useApiQuery({
    url: '/v2/projects/{projectId}/machine-translation-service-settings',
    method: 'get',
    path: { projectId: project.id },
  });

  const autoTranslationSettings = useApiQuery({
    url: '/v2/projects/{projectId}/per-language-auto-translation-settings',
    method: 'get',
    path: { projectId: project.id },
  });

  const updateMtSettings = useApiMutation({
    url: '/v2/projects/{projectId}/machine-translation-service-settings',
    method: 'put',
    invalidatePrefix:
      '/v2/projects/{projectId}/machine-translation-service-settings',
  });

  const updateAutoTranslationSettings = useApiMutation({
    url: '/v2/projects/{projectId}/per-language-auto-translation-settings',
    method: 'put',
    invalidatePrefix:
      '/v2/projects/{projectId}/per-language-auto-translation-settings',
  });

  const languageInfos = useApiQuery({
    url: '/v2/projects/{projectId}/machine-translation-language-info',
    method: 'get',
    path: { projectId: project.id },
  });

  function isSupported(
    service: MtServiceInfo | undefined,
    languageId: number | null
  ) {
    const settings = languageInfos.data!._embedded?.languageInfos?.find(
      (l) => l.languageId === languageId
    );
    return (
      service &&
      (!settings ||
        Boolean(
          settings.supportedServices.find(
            (i) => i.serviceType === service.serviceType
          )
        ))
    );
  }

  function validateService(
    info: LanguageInfoModel[],
    service: MtServiceInfo | undefined,
    languageId: number | null
  ): MtServiceInfo | undefined {
    if (service === undefined || !isSupported(service, languageId)) {
      return undefined;
    }
    return {
      serviceType: service.serviceType,
      formality: supportsFormality(info, service.serviceType, languageId)
        ? service.formality
        : undefined,
    };
  }

  const applyUpdate: OnMtChange = async (info, languageId, data) => {
    const languageIdOrNull = languageId ?? null;
    const existingMtSettings =
      mtSettings.data?._embedded?.languageConfigs
        ?.filter((l) => l.targetLanguageId !== languageIdOrNull)
        .map(
          ({ targetLanguageId, primaryServiceInfo, enabledServicesInfo }) => {
            return {
              targetLanguageId,
              primaryServiceInfo,
              enabledServicesInfo,
            };
          }
        ) || [];

    const updatedMtSettings = data
      ? [...existingMtSettings, data.machineTranslation]
      : existingMtSettings;

    const mtSettingsValidated = updatedMtSettings.map(
      ({ targetLanguageId, primaryServiceInfo, enabledServicesInfo }) => {
        const targetLanguageIdOrNull = targetLanguageId ?? null;
        return {
          targetLanguageId,
          primaryServiceInfo: validateService(
            info,
            primaryServiceInfo,
            targetLanguageIdOrNull
          ),
          enabledServicesInfo: enabledServicesInfo
            ?.map((service) =>
              validateService(info, service, targetLanguageIdOrNull)
            )
            .filter((service) => Boolean(service)) as MtServiceInfo[],
        };
      }
    );

    const existingAutoSettings =
      autoTranslationSettings.data?._embedded?.configs
        ?.filter((l) => l.languageId !== languageIdOrNull)
        .map(
          ({
            languageId,
            enableForImport,
            usingMachineTranslation,
            usingTranslationMemory,
          }) => {
            return {
              languageId,
              enableForImport,
              usingMachineTranslation,
              usingTranslationMemory,
            };
          }
        ) || [];

    const newAutoSettings = data
      ? [...existingAutoSettings, data.autoTranslation]
      : existingAutoSettings;

    await updateMtSettings.mutateAsync({
      path: { projectId: project.id },
      content: {
        'application/json': { settings: mtSettingsValidated },
      },
    });

    await updateAutoTranslationSettings.mutateAsync({
      path: { projectId: project.id },
      content: {
        'application/json': newAutoSettings,
      },
    });
  };

  const languageIds = [
    null,
    ...(languages.data?._embedded?.languages
      ?.filter((l) => !l.base)
      .map((l) => l.id) || []),
  ];

  const isFetching =
    languages.isFetching ||
    mtSettings.isFetching ||
    autoTranslationSettings.isFetching ||
    languageInfos.isFetching ||
    updateMtSettings.isLoading ||
    updateAutoTranslationSettings.isLoading;

  const isLoading =
    languages.isLoading ||
    mtSettings.isLoading ||
    autoTranslationSettings.isLoading ||
    languageInfos.isLoading;

  const settings = useMemo(() => {
    if (isLoading) {
      return undefined;
    }
    return languageIds.map((id) => ({
      id,
      language: languages.data?._embedded?.languages?.find((l) => l.id === id),
      info: languageInfos.data?._embedded?.languageInfos?.find(
        (l) => l.languageId === id
      ),
      mtSettings: mtSettings.data?._embedded?.languageConfigs?.find(
        (l) => l.targetLanguageId === id
      ),
      autoSettings: autoTranslationSettings.data?._embedded?.configs?.find(
        (c) => c.languageId === id
      ),
    }));
  }, [
    isLoading,
    languages.data,
    languageInfos.data,
    mtSettings.data,
    autoTranslationSettings.data,
  ]);

  return {
    info: languageInfos.data?._embedded?.languageInfos ?? [],
    settings,
    applyUpdate,
    isFetching,
    isLoading,
  };
};
