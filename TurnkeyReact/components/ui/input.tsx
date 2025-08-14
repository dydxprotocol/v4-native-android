import * as React from "react";
import { TextInput, type TextInputProps } from "react-native";

const Input = React.forwardRef<
  React.ElementRef<typeof TextInput>,
  TextInputProps
>(({ ...props }, ref) => {
  return (
    <TextInput
      ref={ref}
      {...props}
    />
  );
});

Input.displayName = "Input";

export { Input };
