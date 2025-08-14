import { PasskeyStamper, TurnkeyClient, useTurnkey } from '@turnkey/sdk-react-native';
import React, { createContext, useContext, useEffect, useState } from 'react';
import {
  DeviceEventEmitter,
} from 'react-native';

import { NativeModules } from 'react-native';
import { NativeToJsRequestEvent, TurnkeyNativeModule } from './TurnkeyModule';

const TurnkeyCallbackContext = createContext({ isReady: false });

export const TurnkeyCallbackProvider = ({ children }: { children: React.ReactNode }) => {
  const [isReady, setIsReady] = useState(false);
  const { user } = useTurnkey();

  useEffect(() => {
    DeviceEventEmitter.removeAllListeners('NativeToJsRequest');
    DeviceEventEmitter.addListener(
      'NativeToJsRequest',
      async (event: NativeToJsRequestEvent) => {
        const callbackId = event.callbackId;
        const result = await myJsFunction(callbackId);
        
        console.log('✅ NativeModules keys:', Object.keys(NativeModules));
        console.log('✅ NativeModules.TurnkeyNativeModule:', NativeModules.TurnkeyNativeModule);

        if (user) {
          console.log('User is logged in:', user);
        } else {
          console.log('No user is logged in.');
        }

        TurnkeyNativeModule.onJsResponse(callbackId, result);
      }
    );

    setIsReady(true);

    return () => {
      // Listener cleanup
      // ...removeListener
      setIsReady(false);
    }
  }, [user]); // this is the dep array. It only runs code if `user` obj changes

  return (
    <TurnkeyCallbackContext.Provider value={{ isReady }}>
      {children}
    </TurnkeyCallbackContext.Provider>
  );
}

// If you want to view isReady.
export function useTurnkeyCallbackStatus() {
  return useContext(TurnkeyCallbackContext).isReady;
};


// Async function with typed param/return
async function myJsFunction(callbackId: string): Promise<string> {
  return (
    'Hello Hello from JS!, callbackId: ' +
    callbackId +
    '. User Logged in: ' +
    isUserLoggedIn()
  );
}

function isUserLoggedIn(): boolean {
    try {
        const stamper = new PasskeyStamper({
            rpId: "RP_ID",
        });

        const httpClient = new TurnkeyClient(
            { baseUrl: "TURNKEY_API_URL" },
            stamper
        );

    } catch (error) {
        console.error('Error initializing TurnkeyClient:', error);
        return false;
    }
    return true;
}
