

import { Auth } from './components/Auth';
import { Providers } from './providers/providers';
import "react-native-get-random-values";
import { TurnkeyConfigs } from './sharedConfigs';
import { setDydXTheme } from '../rn_style/themes/currentTheme';
import { useEffect } from 'react';

export const TurnkeyLogin = (configs: TurnkeyConfigs) => {
  useEffect(() => {
    if (configs.theme !== undefined) {
      setDydXTheme(configs.theme);
    }
  }, [configs.theme]);

    useEffect(() => {
    console.log("MOUNT");
  
    return () => {
      console.log("UNMOUNT");
    };
  }, []);
  
  return (
    <Providers configs={configs}>
      <Auth configs={configs} />
    </Providers>
  );
};

