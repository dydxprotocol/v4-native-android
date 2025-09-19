

export type TurnkeyConfigs = {
  googleClientId: string,
  appScheme: string,
  turnkeyUrl: string,
  turnkeyOrgId: string,
  backendApiUrl: string,
  deploymentUri: string,
  theme: "light" | "dark" | "classicDark" | undefined,
  enableAppleLoginIn: boolean,
  strings: Record<string, string>,
};