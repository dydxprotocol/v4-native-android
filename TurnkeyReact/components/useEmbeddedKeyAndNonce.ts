import { SHA256 } from "crypto-js";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  generateP256KeyPair,
} from "@turnkey/crypto";
import { LoginMethod } from "../lib/types";

/**
 * The nonce is a unique, cryptographically secure string used to ensure the authenticity and integrity
 * of each authentication request. In our implementation, we generate the nonce by hashing the embedded public key.
 *
 * Key purposes:
 * 1. Prevent Replay Attacks: By using a unique nonce per session, we help ensure that an intercepted token
 *    cannot be reused maliciously.
 * 2. Tie the Authentication Request to the Response: The nonce is included in the OAuth flow so that the identity token
 *    received from providers (Google or Apple) is bound to the specific authentication request.
 *
 * After a successful authentication, the nonce is refreshed to guarantee that every new authentication flow uses
 * a unique value.
 */

export type EmbeddedKeyAndNonce = {
  privateKey: string | null;
  targetPublicKey: string | null;
  nonce: string | null;
  refreshNonce: () => Promise<void>;
};

export const useEmbeddedKeyAndNonce = (loginMethod: LoginMethod): EmbeddedKeyAndNonce => {
  const [privateKey, setPrivateKey] = useState<string | null>(null);
  const [targetPublicKey, setTargetPublicKey] = useState<string | null>(null);
  const [nonce, setNonce] = useState<string | null>(null);

  const generateNonce = useCallback(async () => {
    try {
      const keypair = generateP256KeyPair();

      const privKey = keypair.privateKey;
      setPrivateKey(privKey);

      var pubKey: string;
      if (loginMethod === LoginMethod.OAuth) {
        pubKey = keypair.publicKey;
      } else {
        pubKey = keypair.publicKeyUncompressed; // 65-byte uncompressed public key
      }
      setTargetPublicKey(pubKey);

      const hashedNonce = SHA256(pubKey).toString();
      setNonce(hashedNonce);
    } catch (error) {
      console.error("Error generating nonce and public key:", error);
    }
  }, []);

  useEffect(() => {
    generateNonce();
  }, [generateNonce]);

  return useMemo(() => ({ privateKey, targetPublicKey, nonce, refreshNonce: generateNonce }), [privateKey, targetPublicKey, nonce]);
};
