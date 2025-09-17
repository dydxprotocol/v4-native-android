export const NAV_THEME = {
  light: {
    background: "hsl(0 0% 100%)", // background
    border: "hsl(240 5.9% 90%)", // border
    card: "hsl(0 0% 100%)", // card
    notification: "hsl(0 84.2% 60.2%)", // destructive
    primary: "hsl(241.31deg 100% 64.12%)", // primary
    text: "hsl(240 10% 3.9%)", // foreground
  },
  dark: {
    background: "hsl(240 10% 3.9%)", // background
    border: "hsl(240 3.7% 15.9%)", // border
    card: "hsl(240 10% 3.9%)", // card
    notification: "hsl(0 72% 51%)", // destructive
    primary: "hsl(241.31deg 100% 64.12%)", // primary
    text: "hsl(0 0% 98%)", // foreground
  },
};

export const DEFAULT_ETHEREUM_ACCOUNTS = [
  {
    curve: "CURVE_SECP256K1" as const,  
    pathFormat: "PATH_FORMAT_BIP32" as const,  
    path: "m/44'/60'/0'/0/0",
    addressFormat: "ADDRESS_FORMAT_ETHEREUM" as const, 
  },
];

export const APP_SCHEME = process.env.EXPO_PUBLIC_APP_SCHEME ?? "";

export const TURNKEY_API_URL = process.env.EXPO_PUBLIC_TURNKEY_API_URL ?? "";
export const TURNKEY_PARENT_ORG_ID =
  process.env.EXPO_PUBLIC_TURNKEY_ORGANIZATION_ID ?? "";

export const RP_ID = process.env.EXPO_PUBLIC_RPID ?? "";
export const PASSKEY_APP_NAME = process.env.EXPO_PUBLIC_PASSKEY_APP_NAME ?? "";

export class STORAGE_KEY {
  static EMAIL_SALT = "salt";
  static ORGANIZATION_ID = "organizationId";
  static USER_ID = "userId";
  static EMAIL = "email";
  static PRIVATE_KEY = "privateKey";
  static DYDX_ADDRESS = "dydxAddress";
}