import { StyleSheet } from 'react-native';
import { currentTheme } from '../rn_style/themes/currentTheme'; // Adjust the import path as necessary

import { useMemo } from 'react';

export const useThemedStyles = (currentTheme: any) => {
  return useMemo(() => StyleSheet.create({
    container: {
      flexGrow: 1,
      backgroundColor: currentTheme.colors.layer2,
      padding: 20,
      paddingTop: 12,
      justifyContent: 'flex-start',
    },
    content: {
      flex: 1,
      justifyContent: 'space-between',
    },
    dragHandle: {
      alignSelf: 'center',
      width: 36,
      height: 4,
      borderRadius: 2,
      backgroundColor: currentTheme.colors.layer7,
      marginBottom: 12,
    },
    title: {
      fontSize: currentTheme.fontSizes.larger,
      color: currentTheme.colors.textPrimary,
      fontFamily: currentTheme.fonts.plus,
      marginBottom: 8,
    },
    subtitle: {
      fontSize: currentTheme.fontSizes.small,
      color: currentTheme.colors.textTertiary,
      marginBottom: 24,
    },
    socialRow: {
      flexDirection: 'row',
      justifyContent: 'space-around',
      marginBottom: 20,
      height: 48,
    },
    socialButton: {
      width: '48%',
      backgroundColor: currentTheme.colors.layer3,
      borderRadius: 16,
      borderColor: currentTheme.colors.layer4,
      borderWidth: 1.6,
      flex: 1,
    },
    emailRow: {
      flexDirection: 'row',
      alignItems: 'center',
      borderRadius: 16,
      paddingHorizontal: 10,
      borderColor: currentTheme.colors.layer4,
      borderWidth: 1,
      overflow: 'hidden',
      marginBottom: 24,
      height: 52,
    },
    emailInput: {
      flex: 1,
      paddingHorizontal: 12,
      color: currentTheme.colors.textPrimary,
    },
    submitButton: {
      justifyContent: 'center',
      paddingHorizontal: 12,
    },
    sendButton: {
      backgroundColor: "#6c63ff",
      padding: 10,
      borderRadius: 12,
    },
    dividerContainer: {
      flexDirection: 'row',
      alignItems: 'center',
      marginBottom: 24,
    },
    divider: {
      flex: 1,
      height: 1,
      backgroundColor: currentTheme.colors.layer3,
    },
    dividerText: {
      color: currentTheme.colors.textTertiary,
      marginHorizontal: 8,
    },
    actionButton: {
      flexDirection: 'row',
      alignItems: 'center',
      backgroundColor: currentTheme.colors.layer3,
      borderRadius: 16,
      paddingHorizontal: 14,
      marginBottom: 12,
      height: 48,
    },
    actionButtonText: {
      color: currentTheme.colors.textSecondary,
      fontSize: currentTheme.fontSizes.medium,
      flex: 1,
    },
    modalOverlay: {
      flex: 1,
      backgroundColor: 'rgba(0, 0, 0, 0.5)', 
      justifyContent: 'center',
      alignItems: 'center',
    },
    modalDialog: {
      backgroundColor: currentTheme.colors.layer3,
      padding: 20,
      marginHorizontal: 16,
      borderRadius: 16,
      minWidth: 280,
      alignItems: 'center',
    },
  }), [currentTheme]);
};
