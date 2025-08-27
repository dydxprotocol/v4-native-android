import {AppRegistry} from 'react-native';
import App from './App';
import 'react-native-get-random-values';
import {TurnkeyLogin} from './TurnkeyReact/TurnkeyLogin';
import 'react-native-url-polyfill/auto';

import { TextDecoder, TextEncoder } from 'text-encoding';
import { Buffer } from 'buffer';

if (typeof global.TextDecoder === 'undefined') {
  global.TextDecoder = TextDecoder;
}
if (typeof global.TextEncoder === 'undefined') {
  global.TextEncoder = TextEncoder;
}

global.Buffer = global.Buffer || Buffer;


import './TurnkeyReact/TurnkeyAddress';

AppRegistry.registerComponent('TurnkeyReact', () => App);
AppRegistry.registerComponent('TurnkeyLogin', () => TurnkeyLogin);
