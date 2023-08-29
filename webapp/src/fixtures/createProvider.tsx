import React, { useMemo, useRef } from 'react';
import { createContext, useContextSelector } from 'use-context-selector';

type SelectorType<StateType, ReturnType> = (state: StateType) => ReturnType;

export const createProvider = <StateType, Actions, ProviderProps>(
  controller: (
    props: ProviderProps
  ) => [state: StateType, actions: Actions] | undefined | null
) => {
  const StateContext = createContext<StateType>(null as any);
  const DispatchContext = React.createContext<Actions>(null as any);

  const Provider: React.FC<ProviderProps> = ({ children, ...props }) => {
    const [state, _actions] = controller(props as any) || [];
    const actionsRef = useRef(_actions);

    actionsRef.current = _actions;

    // stable actions
    const actions = useMemo(() => {
      const result = {};
      if (actionsRef.current) {
        Object.keys(actionsRef.current as any).map((key) => {
          result[key] = (...args) =>
            (actionsRef.current?.[key] as CallableFunction)?.(...args);
        });
      }
      return result as Actions;
    }, [actionsRef, Boolean(state)]);

    if (!state) {
      return null;
    }

    return (
      <StateContext.Provider value={state}>
        <DispatchContext.Provider value={actions}>
          {children}
        </DispatchContext.Provider>
      </StateContext.Provider>
    );
  };

  const useActions = () => React.useContext(DispatchContext);
  const useStateContext = function <SelectorReturn>(
    selector: SelectorType<StateType, SelectorReturn>
  ) {
    return useContextSelector(StateContext, selector);
  };

  return [Provider, useActions, useStateContext] as const;
};
