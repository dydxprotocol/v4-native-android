import { TurnkeyProvider } from '@turnkey/sdk-react-native';
import React, { useEffect } from 'react';
import {
  SafeAreaView,
  Text,
  useColorScheme,
} from 'react-native';

import {
  Colors,
} from 'react-native/Libraries/NewAppScreen';


import { useTurnkeyListener } from './turnkey'


export const AddListenerComponent = () => {
  useTurnkeyListener();
  return <Text>{'Waiting'}</Text>;
}

export const TurnkeyProviderComponent = ({ children }: { children: React.ReactNode }) => {
  const sessionConfig = {
    apiBaseUrl: 'TURNKEY_API_URL',
    organizationId: 'TURNKEY_PARENT_ORG_ID',
    onSessionSelected: () => {
      console.log("onSessionSelected");
    },
    onSessionCleared: () => {
      console.log("onSessionCleared");
    },
  };

  return (
    <TurnkeyProvider config={sessionConfig}>
      <AddListenerComponent />
      <Text>Turnkey Provider Initialized</Text>
      {children}
    </TurnkeyProvider>
  );
};

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  return (
    <TurnkeyProviderComponent>
      <SafeAreaView style={backgroundStyle}>
      </SafeAreaView>
    </TurnkeyProviderComponent>
  );
}

export default App;