import { ReactNode, createContext, useReducer } from "react";
import { LoginMethod } from "../lib/types";
import {
  User,
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
  configs: TurnkeyConfigs;
};

export type UploadDydxAddressRequest = {
  dydxSession: DydxTurnkeySession;
  dydxAddress: string;
  configs: TurnkeyConfigs;
};

export interface AuthRelayProviderType {
  state: AuthState;
  initOtpLogin: (params: OtpAuthRequest) => Promise<void>;
  completeOtpAuth: (params: OtpAuthComplete) => Promise<DydxTurnkeySession | undefined>;
  signUpWithPasskey: () => Promise<void>;
  loginWithPasskey: () => Promise<void>;
  loginWithOAuth: (params: OAuthRequest) => Promise<DydxTurnkeySession | undefined>;
  clearError: () => void;
  uploadDydxAddress: (params: UploadDydxAddressRequest) => Promise<void>;
}

export const AuthRelayContext = createContext<AuthRelayProviderType>({
  state: initialState,
  initOtpLogin: async () => Promise.resolve(),
  completeOtpAuth: async () => Promise.resolve(undefined),
  signUpWithPasskey: async () => Promise.resolve(),
  loginWithPasskey: async () => Promise.resolve(),
  loginWithOAuth: async () => Promise.resolve(undefined),
  clearError: () => { },
  uploadDydxAddress: async () => Promise.resolve(),
});

type SendSignInRequestParams = {
  headers: HeadersInit;
  body: string;
  embeddedKeyAndNonce: EmbeddedKeyAndNonce;
  configs: TurnkeyConfigs;
  loginMethod: LoginMethod;
  providerName?: string;
  userEmail?: string;
};

type OnboardDydxParams = {
  dydxSession: DydxTurnkeySession;
  salt: string;
  loginMethod: string;
  userEmail?: string;
  dydxAddress?: string;
};

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
      "magicLink":  configs.deploymentUri + "onboard/turnkey?token",
    };
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    sendSignInRequest({
      headers: headers,
      body: JSON.stringify(inputBody),
      embeddedKeyAndNonce,
      configs,
      loginMethod: LoginMethod.Email,
      providerName: undefined,
      userEmail: contact
    });
  };

  const completeOtpAuth = async ({
    otpType,
    token,
    configs,
  }: OtpAuthComplete): Promise<DydxTurnkeySession | undefined>  => {
    dispatch({ type: "LOADING", payload: LoginMethod.Email });
    try {
      const deleteKey = true; // Set to true to delete the key after use
      const decryptKey = await getValueWithKey(deleteKey, STORAGE_KEY.PRIVATE_KEY);
      if (!decryptKey) {
        throw new Error("No private decrypt key found in storage");
      }

      const privateKey = decryptCredentialBundle(token, decryptKey);
      const publicKey = uint8ArrayToHexString(getPublicKey(privateKey));

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
      var dydxAddress = await getValueWithKey(deleteKey, STORAGE_KEY.DYDX_ADDRESS);

      const dydxSession = new DydxTurnkeySession(
        privateKey, publicKey, configs, organizationId, userId
      )

      await onboardDydx({
        dydxSession,
        salt,
        loginMethod: LoginMethod.Email,
        userEmail,
        dydxAddress: dydxAddress ?? undefined
      });

      return Promise.resolve(dydxSession);

    } catch (error: any) {
      TurnkeyNativeModule.onTrackingEvent("TurnkeyLoginError", { "signinMethod": "email", "error": error.message });
  
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
  }: OAuthRequest): Promise<DydxTurnkeySession | undefined> => {
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

    const result = await sendSignInRequest({
      headers: headers,
      body: JSON.stringify(inputBody),
      embeddedKeyAndNonce,
      configs,
      loginMethod: LoginMethod.OAuth,
      providerName,
      userEmail: decoded.email
    });
    return Promise.resolve(result);
  };

  const sendSignInRequest = async ({
    headers,
    body,
    embeddedKeyAndNonce,
    configs,
    loginMethod,
    providerName,
    userEmail,
  }: SendSignInRequestParams): Promise<DydxTurnkeySession | undefined>  => {
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

      if (loginMethod === LoginMethod.OAuth && providerName !== undefined) {
        const result = await handleOauthResponse(response, embeddedKeyAndNonce, configs, providerName, userEmail);
        return Promise.resolve(result);
      } else if (loginMethod === LoginMethod.Email && userEmail !== undefined) {
        await handleEmailResponse(response, embeddedKeyAndNonce, configs, "email", userEmail);
        return Promise.resolve(undefined);
      }
      return Promise.resolve(undefined);

    } catch (error: any) {
      var signInMethod = "unknown";
      if (providerName) {
        signInMethod = providerName;
      } else if (loginMethod === LoginMethod.Email) {
        signInMethod = "email";
      }
      TurnkeyNativeModule.onTrackingEvent("TurnkeyLoginError", { "signinMethod": signInMethod, "error": error.message });
      console.error("Error during sign-in: ", error, error.message);
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
  ): Promise<DydxTurnkeySession | undefined> => {
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

    const dydxAddress = response.dydxAddress;

    await onboardDydx({ dydxSession, salt, loginMethod, userEmail, dydxAddress});
    return Promise.resolve(dydxSession);
  }

  const onboardDydx = async ({
    dydxSession,
    salt,
    loginMethod,
    userEmail,
    dydxAddress,
  }: OnboardDydxParams) => {
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

    TurnkeyNativeModule.onTrackingEvent("TurnkeyLoginCompleted", { "signinMethod": loginMethod });

    TurnkeyNativeModule.onAuthCompleted(
      signed,
      ethAccount.address,
      solanaAccount.address,
      mnemonics,
      loginMethod,
      userEmail,
      dydxAddress
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
    const dydxAddress = response.dydxAddress;

    // save data needed after the user clicks the magic link to secure store
    // so that we retain the info if the app is closed
    setValueWithKey(STORAGE_KEY.EMAIL_SALT, salt);
    setValueWithKey(STORAGE_KEY.ORGANIZATION_ID, organizationId);
    setValueWithKey(STORAGE_KEY.USER_ID, userId);
    setValueWithKey(STORAGE_KEY.EMAIL, userEmail);
    if (embeddedKeyAndNonce.privateKey) {
      setValueWithKey(STORAGE_KEY.PRIVATE_KEY, embeddedKeyAndNonce.privateKey);
    }
    setValueWithKey(STORAGE_KEY.DYDX_ADDRESS, dydxAddress);
  }

  const clearError = () => {
    dispatch({ type: "CLEAR_ERROR" });
  };

  const uploadDydxAddress = async ({
    dydxSession,
    dydxAddress,
    configs,
  }: UploadDydxAddressRequest) => {
    const session = dydxSession;

    if (session == undefined) {
      throw new Error("No active session found");
    }
    const accounts = await session.loadWalletAccounts();

    // get the eth account
    const ethAccount = accounts.accounts.find((account) => account.addressFormat === "ADDRESS_FORMAT_ETHEREUM");
    if (!ethAccount) {
      throw new Error("No Ethereum account found in wallet accounts");
    }

    const signature = await session.signUploadAddressMessage(ethAccount.address, dydxAddress);

    const headers = {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    }
    const body = JSON.stringify({
      dydxAddress,
      signature
    });

    try {
      const response = await fetch(`${configs.backendApiUrl}/v4/turnkey/uploadAddress`, {
        method: "POST",
        headers: headers,
        body: body,
      }).then((res) => res.json());

      if (response.errors && Array.isArray(response.errors)) {
        // Handle API-reported errors
        const errorMsg = response.errors.map((e: { msg: any; }) => e.msg).join(", ");
        throw new Error(`Backend Error: ${errorMsg}`);
      }

      // TODO(turnkey): handle policy returned in response

    } catch (error: any) {
      TurnkeyNativeModule.onTrackingEvent("UploadAddressError", { dydxAddress, "error": error.message });
      console.error("Error during sign-in: ", error, error.message);
      dispatch({ type: "ERROR", payload: error.message });
      throw error;
    } finally {
      dispatch({ type: "LOADING", payload: null });
    }
  }

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
        uploadDydxAddress
      }}
    >
      {children}
    </AuthRelayContext.Provider>
  );
};
