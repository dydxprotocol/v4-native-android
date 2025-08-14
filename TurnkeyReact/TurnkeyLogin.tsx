

import { Auth } from './components/Auth';
import { Providers } from './providers/providers';
import "react-native-get-random-values";
import { TurnkeyConfigs } from './sharedConfigs';
import { setDydXTheme } from '../rn_style/themes/currentTheme';
import { useThemedStyles } from './turnkeyStyle';

export const TurnkeyLogin = (configs: TurnkeyConfigs) => {
  if (configs.theme !== undefined) {
    setDydXTheme(configs.theme);
  }

  return (
    <Providers configs={configs}>
      <Auth configs={configs} />
    </Providers>
  );
};

