
import { NativeModules } from 'react-native';

// Define type for native module
interface TurnkeyNativeModuleType {
  onJsResponse: (callbackId: string, result: string) => void;
}

// Safely cast NativeModules
export const { TurnkeyNativeModule } = NativeModules as {
  TurnkeyNativeModule: TurnkeyNativeModuleType;
};

// Define type for event payload
export interface NativeToJsRequestEvent {
  callbackId: string;
}
