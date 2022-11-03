import { components } from 'tg.service/apiSchema.generated';
import { useGlobalContext, useGlobalDispatch } from './GlobalContext';

type OrganizationModel = components['schemas']['OrganizationModel'];
type UsageModel = components['schemas']['UsageModel'];

export const useConfig = () => useGlobalContext((v) => v.serverConfiguration);

export const useUser = () => useGlobalContext((v) => v.userInfo);

export const useIsAdmin = () =>
  useGlobalContext((v) => v.userInfo?.globalServerRole === 'ADMIN');

export const usePreferredOrganization = () => {
  const globalDispatch = useGlobalDispatch();
  const { preferredOrganization, isFetching } = useGlobalContext((v) => ({
    preferredOrganization: v.preferredOrganization!,
    isFetching: v.isFetching,
  }));
  const updatePreferredOrganization = (org: number | OrganizationModel) =>
    globalDispatch({ type: 'UPDATE_ORGANIZATION', payload: org });
  return { preferredOrganization, updatePreferredOrganization, isFetching };
};

export const useOrganizationUsage = () => {
  return useGlobalContext((v) => v.organizationUsage!);
};

export const useOrganizationUsageMethods = () => {
  const globalDispatch = useGlobalDispatch();
  const updateUsage = (data: Partial<UsageModel>) =>
    globalDispatch({ type: 'UPDATE_USAGE', payload: data });
  const refetchUsage = () => globalDispatch({ type: 'REFETCH_USAGE' });
  return { updateUsage, refetchUsage };
};
