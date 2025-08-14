import { ReactNode, createContext, useReducer } from "react";
import { LoginMethod } from "../lib/types";
import {
  User,
  useTurnkey,
} from "@turnkey/sdk-react-native";
import { TurnkeyNativeModule } from "../../TurnkeyModule";
import { DydxTurnkeySession } from "./dydxTurnkeySession";
import { EmbeddedKeyAndNonce } from "../components/useEmbeddedKeyAndNonce";
import { TurnkeyConfigs } from "../sharedConfigs";
import { decryptCredentialBundle, getPublicKey } from "@turnkey/crypto";
import {
  uint8ArrayToHexString,
} from "@turnkey/encoding";
import { getValueWithKey, setValueWithKey } from "../lib/store";
import { STORAGE_KEY } from "../lib/constants";
import { jwtDecode } from 'jwt-decode';

type AuthActionType =
  | { type: "PASSKEY"; payload: User }
  | { type: "INIT_EMAIL_AUTH" }
  | { type: "COMPLETE_EMAIL_AUTH"; payload: User }
  | { type: "INIT_PHONE_AUTH" }
  | { type: "COMPLETE_PHONE_AUTH"; payload: User }
  | { type: "EMAIL_RECOVERY"; payload: User }
  | { type: "WALLET_AUTH"; payload: User }
  | { type: "OAUTH"; payload: User }
  | { type: "LOADING"; payload: LoginMethod | null }
  | { type: "ERROR"; payload: string }
  | { type: "CLEAR_ERROR" };
interface AuthState {
  loading: LoginMethod | null;
  error: string;
  user: User | null;
}

const initialState: AuthState = {
  loading: null,
  error: "",
  user: null,
};

function authReducer(state: AuthState, action: AuthActionType): AuthState {
  switch (action.type) {
    case "LOADING":
      return { ...state, loading: action.payload ? action.payload : null };
    case "ERROR":
      return { ...state, error: action.payload, loading: null };
    case "CLEAR_ERROR":
      return { ...state, error: "" };
    case "INIT_EMAIL_AUTH":
      return { ...state, loading: null, error: "" };
    case "COMPLETE_EMAIL_AUTH":
      return { ...state, user: action.payload, loading: null, error: "" };
    case "INIT_PHONE_AUTH":
      return { ...state, loading: null, error: "" };
    case "COMPLETE_PHONE_AUTH":
      return { ...state, user: action.payload, loading: null, error: "" };
    case "OAUTH":
    case "PASSKEY":
    case "EMAIL_RECOVERY":
    case "WALLET_AUTH":
    case "OAUTH":
      return { ...state, user: action.payload, loading: null, error: "" };
    default:
      return state;
  }
}

export type OAuthRequest = {
  oidcToken: string;
  providerName: string;
  embeddedKeyAndNonce: EmbeddedKeyAndNonce;
  configs: TurnkeyConfigs;
};

export type OtpAuthRequest = {
  otpType: string;
  contact: string;
  embeddedKeyAndNonce: EmbeddedKeyAndNonce;
  configs: TurnkeyConfigs;
};

export type OtpAuthComplete = {
  otpType: string;
  token: string;
  embeddedKeyAndNonce: EmbeddedKeyAndNonce;
  configs: TurnkeyConfigs;
};

export interface AuthRelayProviderType {
  state: AuthState;
  initOtpLogin: (params: OtpAuthRequest) => Promise<void>;
  completeOtpAuth: (params: OtpAuthComplete) => Promise<void>;
  signUpWithPasskey: () => Promise<void>;
  loginWithPasskey: () => Promise<void>;
  loginWithOAuth: (params: OAuthRequest) => Promise<void>;
  clearError: () => void;
}

export const AuthRelayContext = createContext<AuthRelayProviderType>({
  state: initialState,
  initOtpLogin: async () => Promise.resolve(),
  completeOtpAuth: async () => Promise.resolve(),
  signUpWithPasskey: async () => Promise.resolve(),
  loginWithPasskey: async () => Promise.resolve(),
  loginWithOAuth: async () => Promise.resolve(),
  clearError: () => { },
});

interface AuthRelayProviderProps {
  children: ReactNode;
}

export const AuthRelayProvider: React.FC<AuthRelayProviderProps> = ({
  children,
}) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  const initOtpLogin = async ({
    otpType,
    contact,
    embeddedKeyAndNonce,
    configs,
  }: OtpAuthRequest) => {

    const inputBody = {
      "signinMethod": "email",
      "userEmail": contact,
      "targetPublicKey": embeddedKeyAndNonce.targetPublicKey,
      "magicLink": "https://v4.testnet.dydx.exchange/onboard/turnkey?token",
    };
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    sendSignInRequest(headers, JSON.stringify(inputBody), embeddedKeyAndNonce, configs, LoginMethod.Email, contact);
  };

  const completeOtpAuth = async ({
    token,
    embeddedKeyAndNonce,
    configs,
  }: OtpAuthComplete) => {
    dispatch({ type: "LOADING", payload: LoginMethod.Email });
    try {
      const privateKey = decryptCredentialBundle(token, embeddedKeyAndNonce.privateKey!);
      const publicKey = uint8ArrayToHexString(getPublicKey(privateKey));

      console.log("Decrypted bundle private key:", privateKey);
      console.log("Decrypted bundle public key:", publicKey);

      const deleteKey = true; // Set to true to delete the key after use
      const salt = await getValueWithKey(deleteKey, STORAGE_KEY.EMAIL_SALT)
      if (!salt) {
        throw new Error("No salt found in storage");
      }
      const organizationId = await getValueWithKey(deleteKey, STORAGE_KEY.ORGANIZATION_ID);
      if (!organizationId) {
        throw new Error("No organizationId found in storage");
      }
      const userId = await getValueWithKey(deleteKey, STORAGE_KEY.USER_ID);
      if (!userId) {
        throw new Error("No userId found in storage");
      }
      const userEmail = await getValueWithKey(deleteKey, STORAGE_KEY.EMAIL);
      if (!userEmail) {
        throw new Error("No userEmail found in storage");
      }

      const dydxSession = new DydxTurnkeySession(
        privateKey, publicKey, configs, organizationId, userId
      )

      onboardDydx(dydxSession, salt, LoginMethod.Email, userEmail);

    } catch (error: any) {
      console.error("Error decrypting credential bundle:", error);
      dispatch({ type: "ERROR", payload: error.message });
    } finally {
      dispatch({ type: "LOADING", payload: null });
    }
  };

  // User will be prompted once for passkey creation then will leverage an api key session to have a smooth "one tap" login experience
  const signUpWithPasskey = async () => {
    console.debug("signUpWithPasskey called");
  };

  const loginWithPasskey = async () => {
    console.debug("loginWithPasskey called");
  };

  const loginWithOAuth = async ({
    oidcToken,
    providerName,
    embeddedKeyAndNonce,
    configs,
  }: OAuthRequest) => {
    type GoogleIdTokenPayload = {
      email?: string;
      email_verified?: boolean;
    };
    const decoded = jwtDecode<GoogleIdTokenPayload>(oidcToken);

    const inputBody = {
      "signinMethod": "social",
      "targetPublicKey": embeddedKeyAndNonce.targetPublicKey,
      "provider": providerName,
      "oidcToken": oidcToken,
      "userEmail": decoded.email,
    };
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    sendSignInRequest(headers, JSON.stringify(inputBody), embeddedKeyAndNonce, configs, LoginMethod.OAuth, providerName, decoded.email);
  };

  const sendSignInRequest = async (
    headers: HeadersInit,
    body: string,
    embeddedKeyAndNonce: EmbeddedKeyAndNonce,
    configs: TurnkeyConfigs,
    loginMethod: LoginMethod,
    providerName?: string,
    userEmail?: string,
  ) => {
    dispatch({ type: "LOADING", payload: loginMethod });
    try {
      const response = await fetch(`${configs.backendApiUrl}/v4/turnkey/signin`, {
        method: "POST",
        headers: headers,
        body: body,
      }).then((res) => res.json());

      if (response.errors && Array.isArray(response.errors)) {
        // Handle API-reported errors
        const errorMsg = response.errors.map((e: { msg: any; }) => e.msg).join(", ");
        throw new Error(`Backend Error: ${errorMsg}`);
      }

      if (loginMethod === LoginMethod.OAuth) {
        handleOauthResponse(response, embeddedKeyAndNonce, configs, providerName, userEmail);
      } else if (loginMethod === LoginMethod.Email && userEmail !== undefined) {
        handleEmailResponse(response, embeddedKeyAndNonce, configs, "email", userEmail);
      }

    } catch (error: any) {
      console.error("Error during sign-in:", error);
      dispatch({ type: "ERROR", payload: error.message });
    } finally {
      dispatch({ type: "LOADING", payload: null });
    }
  }

  const handleOauthResponse = async (
    response: any,
    embeddedKeyAndNonce: EmbeddedKeyAndNonce,
    configs: TurnkeyConfigs,
    loginMethod: string,
    userEmail?: string,
  ) => {
    const salt = response.salt;
    if (!salt) {
      throw new Error("No salt provided in response");
    }
    const session = response.session;
    if (!session) {
      throw new Error("No session provided in response");
    }

    const dydxSession = DydxTurnkeySession.createFromSession(
      embeddedKeyAndNonce.privateKey!,
      session,
      configs
    );

    onboardDydx(dydxSession, salt, loginMethod, userEmail);
  }

  const onboardDydx = async (
    dydxSession: DydxTurnkeySession,
    salt: string,
    loginMethod: string,
    userEmail?: string,
  ) => {
    const accounts = await dydxSession.loadWalletAccounts();

    // get the eth account
    const ethAccount = accounts.accounts.find((account) => account.addressFormat === "ADDRESS_FORMAT_ETHEREUM");
    if (!ethAccount) {
      throw new Error("No Ethereum account found in wallet accounts");
    }
    // get the solana account
    const solanaAccount = accounts.accounts.find((account) => account.addressFormat === "ADDRESS_FORMAT_SOLANA");
    if (!solanaAccount) {
      throw new Error("No Solana account found in wallet accounts");
    }

    const signed = await dydxSession.signOnboardingMessage(ethAccount.address, salt);

    // get the wallet mnemonics
    const walletId = ethAccount.walletId;
    const mnemonics = await dydxSession.exportWallet(walletId);
    if (!mnemonics) {
      throw new Error("Unable to export wallet mnemonics");
    }

    TurnkeyNativeModule.onAuthCompleted(
      signed,
      ethAccount.address,
      solanaAccount.address,
      mnemonics,
      loginMethod,
      userEmail
    );
  };

  const handleEmailResponse = async (
    response: any,
    embeddedKeyAndNonce: EmbeddedKeyAndNonce,
    configs: TurnkeyConfigs,
    loginMethod: string,
    userEmail: string,
  ) => {
    const salt = response.salt;
    if (!salt) {
      throw new Error("No salt provided in response");
    }
    const organizationId = response.organizationId;
    if (!organizationId) {
      throw new Error("No organizationId provided in response");
    }
    const userId = response.userId;
    if (!userId) {
      throw new Error("No userId provided in response");
    }

    // save data needed after the user clicks the magic link to secure store
    // so that we retain the info if the app is closed
    setValueWithKey(STORAGE_KEY.EMAIL_SALT, salt);
    setValueWithKey(STORAGE_KEY.ORGANIZATION_ID, organizationId);
    setValueWithKey(STORAGE_KEY.USER_ID, userId);
    setValueWithKey(STORAGE_KEY.EMAIL, userEmail);
  }

  const clearError = () => {
    dispatch({ type: "CLEAR_ERROR" });
  };

  return (
    <AuthRelayContext.Provider
      value={{
        state,
        initOtpLogin,
        completeOtpAuth,
        signUpWithPasskey,
        loginWithPasskey,
        loginWithOAuth,
        clearError,
      }}
    >
      {children}
    </AuthRelayContext.Provider>
  );
};
