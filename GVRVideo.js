/*
 * @Author: tiero
 * @Date: 2017-01-05 17:39:15
 * @Last Modified by: tiero
 * @Last Modified time: 2017-01-05 17:40:04
 */
import React, { Component } from "react";
import PropTypes from "prop-types";
import { requireNativeComponent, ViewPropTypes } from "react-native";
import resolveAssetSource from "react-native/Libraries/Image/resolveAssetSource";

const RNVrVideo = requireNativeComponent("VrVideo", VideoView, {
  nativeOnly: { onChange: true }
});

class VideoView extends React.PureComponent {
  constructor(props) {
    super(props);

    this.counter = 0;
    this.countTimer;
  }

  onLoadSuccess = event => {
    const { duration } = event.nativeEvent;
    this.props.onLoadVideoSuccess({ duration });
    this.countTimer = setInterval(() => {
      if (!this.props.paused) {
        this.counter += 250;
      }
      if (this.props.onProgress) {
        this.props.onProgress({ currentTime: this.counter });
      }
    }, 250);
  }

  componentWillUnmount() {
    //android view will not automatically remove view, so we set suspended to true to garbage collect the view
    clearTimeout(this.countTimer);
  }

  setNativeProps(nativeProps) {
    this._root.setNativeProps(nativeProps);
  }

  seek(time) {
    this.counter = time;
    this.setNativeProps({ seekTo: time });
  }

  render() {
    const source = resolveAssetSource(this.props.source) || {};
    let uri = source.uri || "";
    if (uri && uri.match(/^\//)) {
      uri = `file://${uri}`;
    }

    const isNetwork = !!(uri && uri.match(/^https?:/));
    const isAsset = !!(
      uri && uri.match(/^(assets-library|content|ms-appx|ms-appdata):/)
    );
    return (
      <RNVrVideo
        ref={ref => this._root = ref}
        onLoadSuccess={this.onLoadSuccess}
        {...this.props}
        src={{
          uri,
          isNetwork,
          isAsset,
          type: source.type || ""
        }}
      />
    );
  }
}

VideoView.propTypes = {
  ...ViewPropTypes,
  src: PropTypes.object,
  source: PropTypes.oneOfType([
    PropTypes.shape({
      uri: PropTypes.string,
      type: PropTypes.string
    }),
    PropTypes.number
  ]),
  videoType: PropTypes.string,
  volume: PropTypes.number,
  seekTo: PropTypes.number,
  paused: PropTypes.bool,
  displayMode: PropTypes.string,
  enableFullscreenButton: PropTypes.bool,
  enableCardboardButton: PropTypes.bool,
  enableInfoButton: PropTypes.bool,
  enableTouchTracking: PropTypes.bool,
  hidesTransitionView: PropTypes.bool,
  onLoadSuccess: PropTypes.func,
  onLoadError: PropTypes.func,
  onVideoChangeDisplay:PropTypes.func,
};

export default VideoView;
