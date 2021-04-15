import * as React from 'react';
import {FunctionComponent, useContext} from 'react';
import {Box, Checkbox} from "@material-ui/core";
import {TableCell} from "./TableCell";
import {RowContext} from "./TranslationsRow";
import {TranslationListContext} from "./TtranslationsGridContextProvider";
import {useRepositoryPermissions} from "../../hooks/useRepositoryPermissions";
import {RepositoryPermissionType} from "../../service/response.types";

export const Header: FunctionComponent = () => {

    const listContext = useContext(TranslationListContext);
    const permissions = useRepositoryPermissions()

    return (
        <Box display="flex" height={40}>
            {/*
            @ts-ignore*/}
            <RowContext.Provider value={{data: null, lastRendered: 0}}>
                {permissions.satisfiesPermission(RepositoryPermissionType.EDIT) &&
                <Box width={40} display="flex" alignItems="center">
                    <Checkbox checked={listContext.isAllChecked()}
                              indeterminate={!listContext.isAllChecked() && listContext.isSomeChecked()}
                              onChange={() => listContext.checkAllToggle()} style={{padding: 0}} size="small"/>
                </Box>}
                <Box display="flex" flexGrow={1}>
                    {listContext.headerCells.map((inner, key) =>
                        <TableCell key={key}>
                            {inner}
                        </TableCell>
                    )}
                </Box>
                <Box width={"24px"}/>
                {/*The size of advanced view icon in rows*/}
                {/*<Box width={"24px"}>*/}
                {/*</Box>*/}
            </RowContext.Provider>
        </Box>
    )
};
