
import { NativeModules } from 'react-native';

// Define type for native module
interface TurnkeyNativeModuleType {
  onJsResponse: (callbackId: string, result: string) => void;

  onAuthRouteToWallet: () => void;
  onAuthRouteToDesktopQR: () => void;
  onAuthCompleted: (onboardingSignature: string, evmAddress: string, svmAddress: string,
    mnemonics: string, loginMethod: string, userEmail: string | undefined, dydxAddress: string | undefined) => void;

  onAppleAuthRequest: (nonce: string) => void;

  onUploadDydxAddressUploadResponse: (dydxAddress: string, result: string) => void;

  onTrackingEvent: (eventName: string, eventParams: Record<string, string>) => void;
}

// Safely cast NativeModules
export const { TurnkeyNativeModule } = NativeModules as {
  TurnkeyNativeModule: TurnkeyNativeModuleType;
};

// Define type for event payload
export interface NativeToJsRequestEvent {
  callbackId: string;
}

export interface AppleSignInCompletedEvent {
  identityToken: string | null;
  error: string | null;
}

export interface EmailTokenReceivedEvent {
  token: string;
}

export interface DydxAddressReceivedEvent {
  callbackId: string;
  dydxAddress: string;
}

export interface FetchDepositAddressesEvent {
  callbackId: string;
  dydxAddress: string;
  indexerUrl: string;
}