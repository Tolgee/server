import React, { useState } from 'react';
import { T, useTranslate } from '@tolgee/react';
import { PaginatedHateoasList } from 'tg.component/common/list/PaginatedHateoasList';
import { BaseView } from 'tg.component/layout/BaseView';
import { DashboardPage } from 'tg.component/layout/DashboardPage';
import { LINKS } from 'tg.constants/links';
import { useApiQuery } from 'tg.service/http/useQueryApi';
import DashboardProjectListItem from 'tg.views/projects/DashboardProjectListItem';

export const ProjectListView = () => {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');

  const listPermitted = useApiQuery({
    url: '/v2/projects/with-stats',
    method: 'get',
    options: {
      keepPreviousData: true,
    },
    query: {
      page,
      size: 20,
      search,
    },
  });

  const t = useTranslate();

  return (
    <DashboardPage>
      <BaseView
        title={<T>projects_title</T>}
        windowTitle={t('projects_title', undefined, true)}
        onSearch={setSearch}
        containerMaxWidth="lg"
        addLinkTo={LINKS.PROJECT_ADD.build()}
        hideChildrenOnLoading={false}
        loading={listPermitted.isFetching}
      >
        <PaginatedHateoasList
          onPageChange={setPage}
          loadable={listPermitted}
          renderItem={(r) => <DashboardProjectListItem key={r.id} {...r} />}
        />
      </BaseView>
    </DashboardPage>
  );
};
