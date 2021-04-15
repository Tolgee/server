import {default as React, FunctionComponent, ReactNode, useEffect, useState} from "react";
import {container} from "tsyringe";
import {MessageService} from "../../service/MessageService";
import {TranslationActions} from "../../store/repository/TranslationActions";
import {useRepository} from "../../hooks/useRepository";
import {Loadable} from "../../store/AbstractLoadableActions";
import {RepositoryPermissionType, TranslationsDataResponse} from "../../service/response.types";
import {T, useTranslate} from "@tolgee/react";
import {FullPageLoading} from "../common/FullPageLoading";
import {useRepositoryLanguages} from "../../hooks/useRepositoryLanguages";
import {useLeaveEditConfirmationOtherEdit} from "./useLeaveEditConfirmation";
import {parseErrorResponse} from "../../fixtures/errorFIxtures";

//@ts-ignore
export const TranslationListContext = React.createContext<TranslationListContextType>(null);

const actions = container.resolve(TranslationActions);

export type TranslationListContextType = {
    listLanguages: string[],
    resetEdit: () => void,
    cellWidths: number[],
    headerCells: ReactNode[]
    refreshList: () => void
    loadData: (search?: string, limit?: number, offset?: number) => void,
    listLoadable: Loadable<TranslationsDataResponse>
    perPage: number,
    checkAllToggle: () => void,
    isKeyChecked: (id: number) => boolean,
    toggleKeyChecked: (id: number) => void,
    isAllChecked: () => boolean,
    isSomeChecked: () => boolean
    checkedKeys: Set<number>
    showKeys: boolean,
    setShowKeys: (showKeys: boolean) => void
    offset: number;
}

const messaging = container.resolve(MessageService);

export const TranslationGridContextProvider: FunctionComponent = (props) => {

    let repositoryDTO = useRepository();

    let listLoadable = actions.useSelector(s => s.loadables.translations);
    let selectedLanguages = actions.useSelector(s => s.selectedLanguages);
    let translationSaveLoadable = actions.useSelector(s => s.loadables.setTranslations);
    let keySaveLoadable = actions.useSelector(s => s.loadables.editKey);
    let deleteLoadable = actions.useSelector(s => s.loadables.delete);


    const repositoryLanguages = useRepositoryLanguages().reduce((acc, curr) => ({...acc, [curr.abbreviation]: curr.name}), {});

    const t = useTranslate();
    const [perPage, setPerPage] = useState(20);
    const [offset, setOffset] = useState(0);
    const [showKeys, setShowKeys] = useState(true);
    const [checkedKeys, setCheckedKeys] = useState(new Set<number>());
    const [_resetEdit, setResetEdit] = useState(() => () => {
    });

    const loadData = (search?: string, limit?: number, offset?: number) => {
        setOffset(0);
        setPerPage(limit || perPage);
        const lastLoadOffset = listLoadable?.dispatchParams?.[4];
        offset = offset !== undefined ? offset : lastLoadOffset
        actions.loadableActions.translations.dispatch(
            repositoryDTO.id, selectedLanguages.length ? selectedLanguages : null, search, limit || perPage, offset
        );
    };

    useEffect(() => {
        return () => {
            actions.loadableReset.translations.dispatch();
        }
    }, []);

    useEffect(() => {
        if (!listLoadable.data || (selectedLanguages !== listLoadable.data.params.languages && selectedLanguages.length)) {
            loadData();
        }
    }, [selectedLanguages]);


    useEffect(() => {
        if (listLoadable.loaded && !listLoadable.loading) {
            //reset edit just when its loaded and its not reloading after edit
            _resetEdit();
        }
    }, [listLoadable.loading]);

    useEffect(() => {
        if (translationSaveLoadable.error) {
            actions.loadableReset.setTranslations.dispatch();
            for (const error of parseErrorResponse(translationSaveLoadable.error)) {
                messaging.error(<T>{error}</T>);
            }
        }

        if (translationSaveLoadable.loaded) {
            messaging.success(<T>Translation grid - translation saved</T>);
            contextValue.refreshList();
            actions.loadableReset.setTranslations.dispatch();
        }

        if (keySaveLoadable.error) {
            for (const error of parseErrorResponse(keySaveLoadable.error)) {
                messaging.error(<T>{error}</T>);
            }
            actions.loadableReset.editKey.dispatch();
        }

        if (keySaveLoadable.loaded) {
            actions.loadableReset.editKey.dispatch();
            messaging.success(<T>Translation grid - Successfully edited!</T>);
            contextValue.refreshList();
        }

        if (deleteLoadable.error) {
            actions.loadableReset.delete.dispatch();
            for (const error of parseErrorResponse(deleteLoadable.error)) {
                messaging.error(<T>{error}</T>);
            }
        }

        if (deleteLoadable.loaded) {
            actions.loadableReset.delete.dispatch();
            messaging.success(<T>Translation grid - Successfully deleted!</T>);
            contextValue.refreshList();
        }

    }, [translationSaveLoadable, keySaveLoadable, deleteLoadable]);

    const editLeaveConfirmation = useLeaveEditConfirmationOtherEdit()

    useEffect(() => {
        editLeaveConfirmation(() => {
            actions.otherEditionConfirm.dispatch()
        }, () => {
            actions.otherEditionCancel.dispatch()
        })
    });

    if (!listLoadable.touched || (listLoadable.loading && !listLoadable.data)) {
        return <FullPageLoading/>
    }

    const isKeyChecked = (name) => checkedKeys.has(name);

    const isAllChecked = () => {
        return listLoadable.data.data.filter(i => !isKeyChecked(i.id)).length === 0;
    };

    const isSomeChecked = () => {
        return listLoadable.data.data.filter(i => isKeyChecked(i.id)).length > 0;
    };

    const headerCells = showKeys ? [<b>{t("translation_grid_key_text")}</b>] : [];
    headerCells.push(...listLoadable.data.params.languages.map((abbr, index) => <b key={index}>{repositoryLanguages[abbr]}</b>));

    const contextValue: TranslationListContextType = {
        checkAllToggle: () => {
            isAllChecked() ? setCheckedKeys(new Set()) : setCheckedKeys(new Set<number>(listLoadable.data.data.map(d => d.id)));
        },
        listLanguages: listLoadable.data.params.languages,
        headerCells,
        cellWidths: headerCells.map(_ => 100 / headerCells.length),
        set resetEdit(resetEdit: () => void) {
            setResetEdit(() => resetEdit);
        },
        //set state accepts also a function, thats why the funcin returns function - to handle the react call
        get resetEdit() {
            return _resetEdit;
        },
        refreshList: () => actions.loadableActions.translations.dispatch(...listLoadable.dispatchParams),
        loadData,
        listLoadable,
        perPage: perPage,
        offset: 0,
        isKeyChecked: isKeyChecked,
        toggleKeyChecked: (id) => {
            let copy = new Set<number>(checkedKeys);
            if (isKeyChecked(id)) {
                copy.delete(id);
            } else {
                copy.add(id);
            }
            setCheckedKeys(copy);
        },
        isAllChecked,
        isSomeChecked,
        checkedKeys: checkedKeys,
        showKeys,
        setShowKeys,
    };


    return (
        <TranslationListContext.Provider value={contextValue}>
            {props.children}
        </TranslationListContext.Provider>
    );
};
