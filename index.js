import {AppRegistry, NativeModules, DeviceEventEmitter} from 'react-native';
import App from './App';

AppRegistry.registerComponent('HelloWorld', () => App);

const { TurnkeyNativeModule } = NativeModules;

DeviceEventEmitter.addListener("NativeToJsRequest", async (event) => {
  const callbackId = event.callbackId;
  const result = await myJsFunction(callbackId);

  TurnkeyNativeModule.onJsResponse(callbackId, result);
});

async function myJsFunction(callbackId) {
  return "Hello from JS!, callbackId: " + callbackId;
}