# react-native-ttd-gvr

## Getting started

`$ npm install react-native-ttd-gvr --save`

### Mostly automatic installation

`$ react-native link react-native-ttd-gvr`

### Manual installation

#### iOS

1.  In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2.  Go to `node_modules` ➜ `react-native-ttd-gvr` and add `RNGvr.xcodeproj`
3.  In XCode, in the project navigator, select your project. Add `libRNGvr.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4.  Run your project (`Cmd+R`)<

#### Android

1.  Open up `android/app/src/main/java/[...]/MainActivity.java`

- Add `import com.reactlibrary.RNGvrPackage;` to the imports at the top of the file
- Add `new RNGvrPackage()` to the list returned by the `getPackages()` method

2.  Append the following lines to `android/settings.gradle`:
    ```
    include ':react-native-ttd-gvr'
    project(':react-native-ttd-gvr').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-ttd-gvr/android')
    ```
3.  Insert the following lines inside the dependencies block in `android/app/build.gradle`:
    ```
      compile project(':react-native-ttd-gvr')
    ```

## Setup

#### iOS

- Copy `./node_modules/react-native-ttd-gvr/pod_post_install.sh` to ios folder

- Create a **Podfile** in ios folder

```shell
target 'myProject' do
  pod 'GVRSDK'
end
```

Still in ios folder install pods locally

```shell
pod install
pod update
```

- Open `myProject.xcworkspace` and under `myProject` > `Build Settings` under `Build Options` set **ENABLE BITCODE** to **NO**

### Android

- Open `./android/app/build.gradle` then set `minSdkVersion 19`

## Usage

```javascript
import { VideoView } from 'react-native-ttd-gvr'

<VideoView
  style={{ height: 300, width: 200 }}
  source={{
    uri: 'https://raw.githubusercontent.com/googlevr/gvr-ios-sdk/master/Samples/VideoWidgetDemo/resources/congo.mp4',
    type: 'mono'
  }}
  displayMode={'embedded'}
  volume={1}
  enableFullscreenButton
  enableCardboardButton
  enableTouchTracking
  hidesTransitionView
  enableInfoButton={false}
  onLoadSuccess={(e) => console.log()}
  onLoadError={(e) => console.log()}
  onProgress={(e) => console.log()}
/>
```
