import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import ReactList from 'react-list';
import { styled } from '@mui/material';
import { T } from '@tolgee/react';

import {
  useTranslationsSelector,
  useTranslationsDispatch,
} from '../context/TranslationsContext';
import { resizeColumn, useResize } from '../useResize';
import { ColumnResizer } from '../ColumnResizer';
import { CellLanguage } from './CellLanguage';
import { RowTable } from './RowTable';
import { TranslationsToolbar } from '../TranslationsToolbar';
import { NamespaceBanner } from '../Namespace/NamespaceBanner';
import { useNsBanners } from '../context/useNsBanners';

const StyledContainer = styled('div')`
  position: relative;
  margin: 10px 0px 100px 0px;
  border-left: 0px;
  border-right: 0px;
  background: ${({ theme }) => theme.palette.background.default};
  flex-grow: 1;
`;

const StyledHeaderRow = styled('div')`
  border: 1px solid ${({ theme }) => theme.palette.emphasis[200]};
  border-width: 1px 0px 1px 0px;
  position: sticky;
  background: ${({ theme }) => theme.palette.background.default};
  top: 0px;
  margin-bottom: -1px;
  display: flex;
`;

const StyledHeaderCell = styled('div')`
  box-sizing: border-box;
  display: flex;
  flex-grow: 0;
  align-items: center;
  overflow: hidden;

  &.keyCell {
    padding-left: 13px;
  }
`;

export const TranslationsTable = () => {
  const tableRef = useRef<HTMLDivElement>(null);
  const reactListRef = useRef<ReactList>(null);
  const resizersCallbacksRef = useRef<(() => void)[]>([]);

  const dispatch = useTranslationsDispatch();
  const translations = useTranslationsSelector((v) => v.translations);
  const translationsLanguages =
    useTranslationsSelector((v) => v.translationsLanguages) || [];

  const languages = useTranslationsSelector((v) => v.languages);
  const isFetchingMore = useTranslationsSelector((v) => v.isFetchingMore);
  const hasMoreToFetch = useTranslationsSelector((v) => v.hasMoreToFetch);
  const cursorKeyId = useTranslationsSelector((c) => c.cursor?.keyId);

  const languageCols = useMemo(() => {
    if (languages && translationsLanguages) {
      return (
        translationsLanguages?.map((lang) => {
          return languages.find((l) => l.tag === lang)!;
        }, [] as any[]) || []
      );
    } else {
      return [];
    }
  }, [translationsLanguages, languages]);

  const columns = useMemo(
    () => [null, ...translationsLanguages.map((tag) => tag)],
    [translationsLanguages]
  );

  const [columnSizes, setColumnSizes] = useState(columns.map(() => 1));

  const { width } = useResize(tableRef, translations);

  const handleColumnResize = (i: number) => (size: number) => {
    setColumnSizes(resizeColumn(columnSizes, i, size));
  };

  const columnSizesPercent = useMemo(() => {
    const columnsSum = columnSizes.reduce((a, b) => a + b, 0);
    return columnSizes.map((size) => (size / columnsSum) * 100 + '%');
  }, [columnSizes]);

  const handleResize = useCallback(
    (colIndex: number) => {
      resizersCallbacksRef.current[colIndex]?.();
    },
    [resizersCallbacksRef]
  );

  useEffect(() => {
    const prevSizes =
      columnSizes.length === columns.length
        ? columnSizes
        : columns.map(() => 1);
    const previousWidth = prevSizes.reduce((a, b) => a + b, 0) || 1;
    const newSizes = prevSizes.map((w) => (w / previousWidth) * (width || 1));
    setColumnSizes(newSizes);
  }, [width, columns]);

  const handleFetchMore = useCallback(() => {
    dispatch({
      type: 'FETCH_MORE',
    });
  }, [translations]);

  useEffect(() => {
    if (reactListRef.current) {
      dispatch({
        type: 'REGISTER_LIST',
        payload: reactListRef.current,
      });
      return () => {
        dispatch({
          type: 'UNREGISTER_LIST',
          payload: reactListRef.current!,
        });
      };
    }
  }, [reactListRef.current]);

  const nsBanners = useNsBanners();

  if (!translations) {
    return null;
  }

  return (
    <StyledContainer
      style={{ marginBottom: cursorKeyId ? 500 : undefined }}
      ref={tableRef}
      data-cy="translations-view-table"
    >
      <StyledHeaderRow>
        {columns.map((tag, i) => {
          const language = languages!.find((lang) => lang.tag === tag)!;
          return tag ? (
            <StyledHeaderCell key={i} style={{ width: columnSizesPercent[i] }}>
              <CellLanguage
                colIndex={i - 1}
                onResize={handleResize}
                language={language}
              />
            </StyledHeaderCell>
          ) : (
            <StyledHeaderCell
              key={i}
              style={{ width: columnSizesPercent[i] }}
              className="keyCell"
            >
              <T>translation_grid_key_text</T>
            </StyledHeaderCell>
          );
        })}
      </StyledHeaderRow>
      {columnSizes.slice(0, -1).map((w, i) => {
        const left = columnSizes.slice(0, i + 1).reduce((a, b) => a + b, 0);
        return (
          <ColumnResizer
            key={i}
            size={w}
            left={left}
            onResize={handleColumnResize(i)}
            passResizeCallback={(callback) =>
              (resizersCallbacksRef.current[i] = callback)
            }
          />
        );
      })}

      <ReactList
        ref={reactListRef}
        threshold={800}
        type="variable"
        itemSizeEstimator={(index, cache) => {
          return cache[index] || 84;
        }}
        // @ts-ignore
        scrollParentGetter={() => window}
        length={translations.length}
        useTranslate3d
        itemRenderer={(index) => {
          const row = translations[index];
          const isLast = index === translations.length - 1;
          if (isLast && !isFetchingMore && hasMoreToFetch) {
            handleFetchMore();
          }

          const nsBanner = nsBanners.find((b) => b.row === index);

          return (
            <div key={row.keyId}>
              {nsBanner && (
                <NamespaceBanner
                  namespace={nsBanner}
                  columnSizes={columnSizes}
                />
              )}
              <RowTable
                data={row}
                languages={languageCols}
                columnSizes={columnSizesPercent}
                onResize={handleResize}
              />
            </div>
          );
        }}
      />
      <TranslationsToolbar width={width} />
    </StyledContainer>
  );
};
