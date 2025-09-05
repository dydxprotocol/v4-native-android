import * as React from "react";
import { Input } from "../components/ui/input";
import { useThemedStyles } from '../turnkeyStyle';
import { Image, Modal, View, TouchableOpacity } from "react-native";
import { Text } from './ui/text';
import { useState } from 'react';
import { useAuthRelay } from "../hooks/useAuthRelay";
import { TurnkeyConfigs } from "../sharedConfigs";
import { EmbeddedKeyAndNonce } from "./useEmbeddedKeyAndNonce";
import { Button } from "./ui/button";
import { OtpType } from "../lib/types";
import { currentTheme } from "../../rn_style/themes/currentTheme";

interface EmailInputProps {
  embeddedKeyAndNonce: EmbeddedKeyAndNonce;
  configs: TurnkeyConfigs;
  focusChanged: (isFocused: boolean) => void;
}

export const EmailInput = ({
  embeddedKeyAndNonce,
  configs,
  focusChanged,
}: EmailInputProps) => {
  const { initOtpLogin, completeOtpAuth, state } = useAuthRelay();
  const [email, setEmail] = useState<string>('');
  const [isValidEmail, setIsValidEmail] = useState<boolean>(false);
  const styles = useThemedStyles(currentTheme);

  const [checkEmailModalVisible, setCheckEmailModalVisible] = useState(false);
  const [showResendButton, setShowResendButton] = useState(false);

  const handleEmailSubmit = () => {
    if (isValidEmail) {
      initOtpLogin({
        otpType: OtpType.Email,
        contact: email,
        embeddedKeyAndNonce: embeddedKeyAndNonce,
        configs: configs,
      });
      setCheckEmailModalVisible(true);
      setShowResendButton(false); // hide initially
      setTimeout(() => {
        setShowResendButton(true); // show after 10s
      }, 10000);
    }
  };

  return (
    <View style={{ flex: 1, flexDirection: "row", alignItems: "center", justifyContent: "center" }}>

      <CheckEmailModal
        visible={checkEmailModalVisible}
        onClose={() => setCheckEmailModalVisible(false)}
        onResend={handleEmailSubmit}
        showResendButton={showResendButton}
        configs={configs}
        currentTheme={currentTheme}
        styles={styles}
      />

      <Image
        source={require('../../rn_style/assets/icon_mail.png')}
        style={{ width: 24, height: 24, tintColor: currentTheme.colors.textTertiary, marginLeft: 8 }}
      />

      <Input
        style={styles.emailInput}
        autoCapitalize="none"
        autoComplete="email"
        autoCorrect={false}
        keyboardType="email-address"
        placeholderTextColor={currentTheme.colors.textTertiary}
        placeholder={configs.strings["APP.TURNKEY_ONBOARD.EMAIL_PLACEHOLDER"]}
        value={email}
        onChangeText={(text: string) => {
          setEmail(text);
          const isValid = validateEmail(text);
          setIsValidEmail(isValid);
        }}
        onFocus={() => focusChanged(true)} 
        onBlur={() => focusChanged(false)}
        aria-labelledby="emailLabel"
        aria-errormessage="emailError"
      />

      <Button
        disabled={!!state.loading || !isValidEmail}
        onPress={() => handleEmailSubmit()}
      >
        <TouchableOpacity style={[styles.sendButton, { backgroundColor: isValidEmail ? currentTheme.colors.purple : currentTheme.colors.textTertiary }]}>
          <Image
            source={require('../../rn_style/assets/icon_arrow.png')}
            style={{ width: 12, height: 12, tintColor: currentTheme.colors.white}}
          />
        </TouchableOpacity>

        {/* <Text style={{ color: isValidEmail ? currentTheme.colors.purple : currentTheme.colors.textTertiary }}>
          {configs.strings["APP.TURNKEY_ONBOARD.SUBMIT"]}
        </Text> */}
      </Button>
    </View>
  );
};

type CheckEmailModalProps = {
  visible: boolean;
  onClose: () => void;
  onResend: () => void;
  showResendButton: boolean;
  configs: any; // Replace with proper type
  currentTheme: any; // Replace with proper type
  styles: any;
};

const CheckEmailModal = ({
  visible,
  onClose,
  onResend,
  showResendButton,
  configs,
  currentTheme,
  styles,
}: CheckEmailModalProps) => {
  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <View style={styles.modalOverlay}>
        <View>
          <View style={styles.modalDialog}>
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
            <Image
              source={require('../../rn_style/assets/icon_mail2.png')}
              style={{
                width: 48,
                height: 48,
                marginEnd: 8,
                tintColor: currentTheme.colors.textPrimary,
                marginBottom: 12,
              }}
            />
            <Text
              style={{
                fontSize: currentTheme.fontSizes.medium,
                color: currentTheme.colors.textPrimary,
                marginBottom: 8,
              }}
            >
              {configs.strings['APP.TURNKEY_ONBOARD.CHECK_EMAIL_TITLE']}
            </Text>
            <Text
              style={{
                fontSize: currentTheme.fontSizes.small,
                color: currentTheme.colors.textTertiary,
                textAlign: 'center',
                marginBottom: 24,
              }}
            >
              {configs.strings['APP.TURNKEY_ONBOARD.CHECK_EMAIL_DESCRIPTION']}
            </Text>

            {showResendButton && (
              <Button onPress={onResend}>
                <View
                  style={{
                    flexDirection: 'row',
                    alignItems: 'center',
                    justifyContent: 'center',
                    backgroundColor: currentTheme.colors.layer5,
                    borderRadius: 999,
                    paddingHorizontal: 20,
                    paddingVertical: 10,
                  }}
                >
                  <Image
                    source={require('../../rn_style/assets/icon_refresh.png')}
                    style={{
                      width: 16,
                      height: 16,
                      tintColor: currentTheme.colors.purple,
                      marginRight: 6,
                    }}
                  />
                  <Text
                    style={{
                      color: currentTheme.colors.purple,
                      fontSize: currentTheme.fontSizes.small,
                    }}
                  >
                    {configs.strings['APP.TURNKEY_ONBOARD.RESEND']}
                  </Text>
                </View>
              </Button>
            )}
          </View>
        </View>
      </View>
    </Modal>
  );
};

const validateEmail = (email: string | undefined) => {
  if (!email) return false;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};
