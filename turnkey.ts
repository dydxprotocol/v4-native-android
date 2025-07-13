import { useTurnkey, TurnkeyClient, PasskeyStamper } from '@turnkey/sdk-react-native';
import { useState } from 'react';
import { DeviceEventEmitter, NativeModules, Text } from 'react-native';

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

// Define type for native module
interface TurnkeyNativeModuleType {
  onJsResponse: (callbackId: string, result: string) => void;
}

// Safely cast NativeModules
export const { TurnkeyNativeModule } = NativeModules as {
  TurnkeyNativeModule: TurnkeyNativeModuleType;
};


// Define type for event payload
interface NativeToJsRequestEvent {
  callbackId: string;
}

export function addListener() {

    const { user } = useTurnkey();

    DeviceEventEmitter.addListener(
        'NativeToJsRequest',
        async (event: NativeToJsRequestEvent) => {
            const callbackId = event.callbackId;
            const result = await myJsFunction(callbackId);

            if (user) {
                console.log('User is logged in:', user);
            } else {
                console.log('No user is logged in.');
            }
            TurnkeyNativeModule.onJsResponse(callbackId, result);
        }
    );
}

// Async function with typed param/return
async function myJsFunction(callbackId: string): Promise<string> {
  return (
    'Hello Hello from JS!, callbackId: ' +
    callbackId +
    '. User Logged in: ' +
    isUserLoggedIn()
  );
}
