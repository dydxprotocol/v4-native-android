
import {type TurnkeyApiTypes} from "@turnkey/sdk-react-native";

export enum LoginMethod {
  Passkey = "PASSKEY",
  Email = "EMAIL",
  Phone = "PHONE",
  OAuth = "OAUTH",
}

export enum OtpType {
  Email = "OTP_TYPE_EMAIL",
  Sms = "OTP_TYPE_SMS",
}

export enum HashFunction {
  NoOp = "HASH_FUNCTION_NO_OP",
  SHA256 = "HASH_FUNCTION_SHA256",
  KECCAK256 = "HASH_FUNCTION_KECCAK256",
  NotApplicable = "HASH_FUNCTION_NOT_APPLICABLE",
}

export enum PayloadEncoding {
  Hexadecimal = "PAYLOAD_ENCODING_HEXADECIMAL",
  TextUTF8 = "PAYLOAD_ENCODING_TEXT_UTF8",
}

export type SignRawPayloadResult = TurnkeyApiTypes["v1SignRawPayloadResult"];
