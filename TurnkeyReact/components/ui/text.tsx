import * as Slot from "@rn-primitives/slot";
import type { SlottableTextProps, TextRef } from "@rn-primitives/types";
import * as React from "react";
import { Text as RNText } from "react-native";
import { currentTheme } from "../../../rn_style/themes/currentTheme";
import { useThemedStyles } from "../../turnkeyStyle";

const TextClassContext = React.createContext<string | undefined>(undefined);

const Text = React.forwardRef<TextRef, SlottableTextProps>(
  ({ asChild = false, style, ...props }, ref) => {
    const textClass = React.useContext(TextClassContext);
    const Component = asChild ? Slot.Text : RNText;

    const styles = useThemedStyles(currentTheme);
   
    return (
      <Component
        ref={ref}
        {...props}
        style={[
          { 
            fontFamily: currentTheme.fonts.base,
            fontSize: currentTheme.fontSizes.medium,
          }, 
          textClass && styles[textClass as keyof typeof styles],    // resolve text class (if using StyleSheet)
          style,                              // override last
        ]}
      />
    );
  },
);
Text.displayName = "Text";

export { Text, TextClassContext };
