import * as Keychain from "react-native-keychain";

export const getValueWithKey = async (
  deleteKey = false,
  storageKey: string,
): Promise<string | null> => {
  try {
    const credentials = await Keychain.getGenericPassword({
      service: storageKey,
    });

    if (credentials) {
      if (deleteKey) {
        await Keychain.resetGenericPassword({
          service: storageKey,
        });
      }
      return credentials.password;
    }
    return null;
  } catch (error) {
    throw new Error(`Failed to get value: ${error}`);
  }
};

export const setValueWithKey = async (
  storageKey: string,
  value: string,
): Promise<void> => {
  try {
    await Keychain.setGenericPassword(storageKey, value, {
      accessible: Keychain.ACCESSIBLE.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
      service: storageKey,
    });
  } catch (error) {
    throw new Error(`Failed to set value: ${error}`);
  }
};