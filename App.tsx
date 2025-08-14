import { TurnkeyProvider } from '@turnkey/sdk-react-native';
import {
  SafeAreaView,
  useColorScheme,
  Text,
} from 'react-native';

import {
  Colors,
} from 'react-native/Libraries/NewAppScreen';

import { TurnkeyCallbackProvider } from './TurnkeyCallbackProvider';

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
      <TurnkeyCallbackProvider>
        {children}
      </TurnkeyCallbackProvider>
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
        <Text>
          Welcome to Turnkey React Native!
        </Text>
      </SafeAreaView>
    </TurnkeyProviderComponent>
  );
}

export default App;