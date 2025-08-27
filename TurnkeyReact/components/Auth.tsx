import React, { useEffect, useState } from 'react';
import {
  View,
  TouchableOpacity,
  ScrollView,
  DeviceEventEmitter,
  Modal,
} from 'react-native';
import { Button } from "./ui/button";
import { Text } from './ui/text';
import { TurnkeyConfigs } from '../sharedConfigs';
import { useAuthRelay } from '../hooks/useAuthRelay';
import { OAuthInput } from './OAuthInput';
import { EmailInput } from './EmailInput';
import { useThemedStyles } from '../turnkeyStyle';
import { LoginMethod } from '../lib/types';
import { DydxAddressReceivedEvent, EmailTokenReceivedEvent, TurnkeyNativeModule } from '../../TurnkeyModule';
import { useEmbeddedKeyAndNonce } from './useEmbeddedKeyAndNonce';
import { Image } from 'react-native';
import { currentTheme } from '../../rn_style/themes/currentTheme';
import { DydxTurnkeySession } from '../providers/dydxTurnkeySession';

const renderError = () => {
  const {
    state,
  } = useAuthRelay();

  if (state.error && state.loading === null) {
    return (
      <Text style={{ color: currentTheme.colors.red, marginBottom: 20, textAlign: 'center' }}>
        {state.error}
      </Text>
    );
  }
  return null;
}

export const Auth = ({ configs }: { configs: TurnkeyConfigs }) => {
  const {
    loginWithOAuth,
    uploadDydxAddress,
    completeOtpAuth,
  } = useAuthRelay();

  const [session, setSession] = useState<DydxTurnkeySession>();
  const [continueModal, setContinueModal] = useState(false);
  const [continueModalProviderName, setContinueModalProviderName] = useState<string>();

  const oAuthEmbeddedKeyAndNonce = useEmbeddedKeyAndNonce(LoginMethod.OAuth);
  const emailEmbeddedKeyAndNonce = useEmbeddedKeyAndNonce(LoginMethod.Email);

  useEffect(() => {
    DeviceEventEmitter.removeAllListeners('DydxAddressReceived');
    DeviceEventEmitter.addListener(
      'DydxAddressReceived',
      async ({ callbackId, dydxAddress }: DydxAddressReceivedEvent) => {
        if (!session) {
          console.error("No DYDX session available");
          TurnkeyNativeModule.onJsResponse(callbackId, "failed: No DYDX session available");
          return;
        }

        try {
          await uploadDydxAddress({
            dydxSession: session,
            dydxAddress: dydxAddress,
            configs: configs,
          })
          TurnkeyNativeModule.onJsResponse(callbackId, "success");
        } catch (error) {
          console.error("Error uploading dydx address:", error);
          TurnkeyNativeModule.onJsResponse(callbackId, "failed: " + error);
        } finally {
          setContinueModal(false);
          setContinueModalProviderName(undefined);
        }
      }
    );
  });

  useEffect(() => {
    DeviceEventEmitter.removeAllListeners('EmailTokenReceived');
    DeviceEventEmitter.addListener(
      'EmailTokenReceived',
      async ({ token }: EmailTokenReceivedEvent) => {
        setContinueModalProviderName("Email");
        setContinueModal(true);
        const session = await completeOtpAuth({
          otpType: "email",
          token: token,
          configs: configs,
        });

        setSession(session);

        await emailEmbeddedKeyAndNonce.refreshNonce();
      }
    );
  }, [emailEmbeddedKeyAndNonce]);

  const styles = useThemedStyles(currentTheme);

  return (
    <ScrollView
      bounces={false} // iOS
      overScrollMode="never" // Android
      contentContainerStyle={styles.container}
    >
      <View style={styles.content}>
        <ContinueSignInModal
          visible={continueModal}
          onClose={() => setContinueModal(false)}
          configs={configs}
          currentTheme={currentTheme}
          styles={styles}
          providerName={continueModalProviderName}
        />

        <View>
          {/* Draggable indicator bar */}
          <View style={styles.dragHandle} />

          {/* Header */}
          <Text style={styles.title}>{configs.strings["APP.TURNKEY_ONBOARD.SIGN_IN_TITLE"]}</Text>
          <Text style={styles.subtitle}>
            {configs.strings["APP.TURNKEY_ONBOARD.SIGN_IN_DESCRIPTION"]}
          </Text>

          {/* Social icons row */}
          <View style={styles.socialRow}>
            <OAuthInput
              onSuccess={async (params) => {
                setContinueModalProviderName(params.providerName);
                setContinueModal(true);
                const session = await loginWithOAuth(params);
                setSession(session);
              }}
              configs={configs}
              embeddedKeyAndNonce={oAuthEmbeddedKeyAndNonce} />
          </View>

          {/* Email input row */}
          <View style={styles.emailRow}>
            <EmailInput
              embeddedKeyAndNonce={emailEmbeddedKeyAndNonce}
              configs={configs}
            />
          </View>

          {renderError()}

        </View>

        <View>

          {/* Divider */}
          <View style={styles.dividerContainer}>
            <View style={styles.divider} />
            <Text style={styles.dividerText}>{configs.strings["APP.GENERAL.OR"]}</Text>
            <View style={styles.divider} />
          </View>

          {/* Sign in with Passkey */}
          {/* <TouchableOpacity style={styles.actionButton}>
          <Ionicons
            name="person"
            size={18}
            color="#fff"
            style={{ marginRight: 8 }}
          />
          <Text style={styles.actionButtonText}>Sign in with Passkey</Text>
        </TouchableOpacity> */}

          {/* Sign in with Desktop */}
          <TouchableOpacity
            style={styles.actionButton}
            onPress={async () => {
              TurnkeyNativeModule.onAuthRouteToDesktopQR();
            }}>
            <Image
              source={require('../../rn_style/assets/icon_desktop.png')}
              style={{ width: 18, height: 18, marginEnd: 8, tintColor: currentTheme.colors.textSecondary }}
            />
            <Text style={styles.actionButtonText}>{configs.strings["APP.TURNKEY_ONBOARD.SIGN_IN_DESKTOP"]}</Text>
            <Image
              source={require('../../rn_style/assets/chevron_right.png')}
              style={{ height: 10, tintColor: currentTheme.colors.textTertiary }}
              resizeMode="contain"
            />
          </TouchableOpacity>

          {/* Sign in with Wallet */}
          <TouchableOpacity
            style={styles.actionButton}
            onPress={async () => {
              TurnkeyNativeModule.onAuthRouteToWallet();
            }}>
            <Image
              source={require('../../rn_style/assets/icon_wallet.png')}
              style={{ width: 16, height: 16, marginEnd: 8, tintColor: currentTheme.colors.textSecondary }}
            />
            <Text style={styles.actionButtonText}>{configs.strings["APP.TURNKEY_ONBOARD.SIGN_IN_WALLET"]}</Text>
            <Image
              source={require('../../rn_style/assets/chevron_right.png')}
              style={{ height: 10, tintColor: currentTheme.colors.textTertiary }}
              resizeMode="contain"
            />
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );
}

type ContinueSignInModalProps = {
  visible: boolean;
  onClose: () => void;
  configs: any; // Replace with proper type
  currentTheme: any; // Replace with proper type
  styles: any;
  providerName: any;
};

const iconMap: Record<string, any> = {
  email: require('../../rn_style/assets/icon_mail2.png'),
  apple: require('../../rn_style/assets/logo_apple.png'),
  google: require('../../rn_style/assets/logo_google.png'),
};

const ContinueSignInModal = ({
  visible,
  onClose,
  configs,
  currentTheme,
  styles,
  providerName,
}: ContinueSignInModalProps) => {
  const iconSource = providerName
    ? iconMap[providerName.toLowerCase()]
    : undefined;

  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <View
        style={[
          styles.modalOverlay,
          { flex: 1, justifyContent: 'center', alignItems: 'center' },
        ]}
      >
        <View style={[styles.modalDialog, { width: 300, alignItems: 'center' }]}>
          <View style={{ width: '100%', alignItems: 'flex-end' }}>
            <Button onPress={onClose}>
              <Image
                source={require('../../rn_style/assets/x-mark.png')}
                style={{
                  width: 16,
                  height: 16,
                  tintColor: currentTheme.colors.textPrimary,
                  marginBottom: 24,
                }}
              />
            </Button>
          </View>

          {iconSource && (
            <Image
              source={iconSource}
              style={{
                width: 24,
                height: 24,
                resizeMode: 'contain',
                tintColor: currentTheme.colors.textPrimary,
                marginBottom: 12,
              }}
            />
          )}

          <Text
            style={{
              fontSize: currentTheme.fontSizes.medium,
              color: currentTheme.colors.textPrimary,
              marginBottom: 8,
            }}
          >
            {configs.strings['APP.TURNKEY_ONBOARD.CONTINUE_SIGN_IN_TITLE']}
          </Text>
          <Text
            style={{
              fontSize: currentTheme.fontSizes.small,
              color: currentTheme.colors.textTertiary,
              textAlign: 'center',
              marginBottom: 24,
            }}
          >
            {configs.strings['APP.TURNKEY_ONBOARD.CONTINUE_SIGN_IN_DESCRIPTION']}
          </Text>
        </View>
      </View>
    </Modal>
  );
};
