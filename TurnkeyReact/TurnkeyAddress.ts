
import { DeviceEventEmitter, NativeModules } from 'react-native';
import { FetchDepositAddressesEvent, NativeToJsRequestEvent, TurnkeyNativeModule } from '../TurnkeyModule';

DeviceEventEmitter.addListener(
  'FetchDepositAddresses',
  async ({ callbackId, dydxAddress, indexerUrl }: FetchDepositAddressesEvent) => {
    const headers = {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    }
    const body = JSON.stringify({
      dydxAddress,
    });

    try {
      const rawResponse = await fetch(`${indexerUrl}/v4/bridging/getDepositAddress/${dydxAddress}`, {
        method: "GET",
        headers: headers,
      }).then((res) => res.text());

      const response = JSON.parse(rawResponse);
      if (response.errors && Array.isArray(response.errors)) {
        // Handle API-reported errors
        const errorMsg = response.errors.map((e: { msg: any; }) => e.msg).join(", ");
        throw new Error(`Backend Error: ${errorMsg}`);
      }

      // TODO(turnkey): handle policy returned in response

      TurnkeyNativeModule.onJsResponse(callbackId, rawResponse);

    } catch (error: any) {
      TurnkeyNativeModule.onTrackingEvent("TurnkeyFetchDepositAddressError", { "dydxAddress": dydxAddress, "error": error.message });
      console.error("Error during sign-in: ", error, error.message);
      TurnkeyNativeModule.onJsResponse(callbackId, error.message);
    }
  }
);
