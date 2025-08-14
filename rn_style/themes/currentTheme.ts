import { darkTheme } from "./darkTheme";
import { lightTheme } from "./lightTheme";
import { classicDarkTheme } from "./classicDarkTheme";

export enum DydxTheme {
  Dark = "dark",
  Light = "light",
  ClassicDark = "classicDark",
}

export const setDydXTheme = (theme: string) => {
  switch (theme) {
    case DydxTheme.Dark:
      currentTheme = darkTheme;
      break;
    case DydxTheme.Light:
      currentTheme = lightTheme;
      break;
    case DydxTheme.ClassicDark:
      currentTheme = classicDarkTheme;
      break;
    default:
      throw new Error(`Unknown theme: ${theme}`);
  }
};

export var currentTheme = darkTheme;