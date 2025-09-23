import { useTurnkey } from "@turnkey/sdk-react-native";
import { TurnkeyConfigs } from "../SharedConfigs";
import { Button } from "./ui/button";
import { View, DeviceEventEmitter, Image } from "react-native";
import { OAuthRequest } from "../providers/authRelayProvider";
import { EmbeddedKeyAndNonce } from "./useEmbeddedKeyAndNonce";
import { AppleSignInCompletedEvent, TurnkeyNativeModule } from "../../TurnkeyModule";
import { useEffect } from "react";
import { currentTheme } from "../../rn_style/themes/currentTheme";
import { useThemedStyles } from "../turnkeyStyle";
import { Platform } from 'react-native';

type OAuthProps = {
  onSuccess: (params: OAuthRequest) => Promise<void>;
  configs: TurnkeyConfigs;
  embeddedKeyAndNonce: EmbeddedKeyAndNonce;
}

export const GoogleAuthButton: React.FC<OAuthProps> = ({
  onSuccess,
  configs,
  embeddedKeyAndNonce
}: OAuthProps) => {
  const { handleGoogleOAuth } = useTurnkey();

  const styles = useThemedStyles(currentTheme);

  const handlePress = async () => {
    try {
      TurnkeyNativeModule.onTrackingEvent("TurnkeyLoginInitiated", { "signinMethod": "google" });
      await handleGoogleOAuth({
        clientId: configs.googleClientId,
        nonce: embeddedKeyAndNonce.nonce!,
        scheme: configs.appScheme,
        onSuccess: async (idToken: string) => {
          await onSuccess({
            oidcToken: idToken,
            providerName: "google",
            embeddedKeyAndNonce: embeddedKeyAndNonce,
            configs: configs,
          });

          // we refresh the nonce before authentication to ensure a new one is used
          // if the user logs out and logs in with oAuth again
          await embeddedKeyAndNonce.refreshNonce();
        },
      });
    } catch (error) {
      console.error("Error in Google Auth:", error);
    }
  };

  return (
    <Button
      onPress={handlePress}
      style={styles.socialButton}
      disabled={embeddedKeyAndNonce.nonce == null || !embeddedKeyAndNonce.targetPublicKey}
    >
      <View style={{ flex: 1, flexDirection: "row", alignItems: "center", justifyContent: "center" }}>
        <Image
          source={require('../../rn_style/assets/logo_google.png')}
          style={{ width: 24, height: 24 }}
        />
      </View>
    </Button>
  );
};

export const AppleAuthButton: React.FC<OAuthProps> = ({
  onSuccess,
  configs,
  embeddedKeyAndNonce
}: OAuthProps) => {
  useEffect(() => {
    DeviceEventEmitter.removeAllListeners('AppleSignInCompleted');
    DeviceEventEmitter.addListener(
      'AppleSignInCompleted',
      async ({ identityToken, error }: AppleSignInCompletedEvent) => {
        if (identityToken !== null && embeddedKeyAndNonce.targetPublicKey) {
          await onSuccess({
            oidcToken: identityToken,
            providerName: "apple",
            embeddedKeyAndNonce: embeddedKeyAndNonce,
            configs: configs,
          });

          // we refresh the nonce before authentication to ensure a new one is used
          // if the user logs out and logs in with oAuth again
          await embeddedKeyAndNonce.refreshNonce();
        }
      }
    );
  })

  const handleAppleAuth = async () => {
    TurnkeyNativeModule.onTrackingEvent("TurnkeyLoginInitiated", { "signinMethod": "apple" });
    if (!embeddedKeyAndNonce.nonce) {
      console.error("Nonce is not ready");
      return;
    }

    TurnkeyNativeModule.onAppleAuthRequest(embeddedKeyAndNonce.nonce);
  };

  const styles = useThemedStyles(currentTheme);

  return (
    <Button
      onPress={handleAppleAuth}
      style={styles.socialButton}
      disabled={embeddedKeyAndNonce.nonce == null || !embeddedKeyAndNonce.targetPublicKey}
    >
      <View style={{ flex: 1, flexDirection: "row", alignItems: "center", justifyContent: "center" }}>
        <Image
          source={require('../../rn_style/assets/logo_apple.png')}
          style={{ height: 24 }}
          resizeMode="contain"
          tintColor={currentTheme.colors.textPrimary}
        />
      </View>
    </Button>
  );
};

export const OAuthInput: React.FC<OAuthProps> = (props) => {
  const { onSuccess, configs, embeddedKeyAndNonce } = props;

  return (
    <View style={{ flexDirection: 'row', justifyContent: "space-evenly", gap: 16, width: '100%' }}>
      {Platform.OS === 'ios' && configs.enableAppleLoginIn && (
        <AppleAuthButton
          onSuccess={onSuccess}
          configs={configs}
          embeddedKeyAndNonce={embeddedKeyAndNonce}
        />
      )}
      <GoogleAuthButton
        onSuccess={onSuccess}
        configs={configs}
        embeddedKeyAndNonce={embeddedKeyAndNonce}
      />
    </View>
  );
};
