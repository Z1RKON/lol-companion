import React, { useEffect, useRef } from 'react';
import { Animated, Easing, Image, StyleSheet } from 'react-native';

const GAS_MASK_IMAGE = require('../../assets/gas-mask.png');

const AnimatedImage = Animated.createAnimatedComponent(Image);

type GasMaskOverlayProps = {
  size: number;
};

const DON_DURATION_MS = 1100;

/** PNG-противогаз без фона: надевается сверху, размер = аватар. */
export function GasMaskOverlay({ size }: GasMaskOverlayProps): React.JSX.Element {
  const donning = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    donning.setValue(0);
    const anim = Animated.timing(donning, {
      toValue: 1,
      duration: DON_DURATION_MS,
      easing: Easing.out(Easing.back(1.1)),
      useNativeDriver: true,
    });
    anim.start();
    return () => anim.stop();
  }, [donning, size]);

  const translateY = donning.interpolate({
    inputRange: [0, 1],
    outputRange: [-size * 1.2, 0],
  });
  const scale = donning.interpolate({
    inputRange: [0, 0.85, 1],
    outputRange: [0.55, 1.03, 1],
  });
  const opacity = donning.interpolate({
    inputRange: [0, 0.12, 1],
    outputRange: [0, 1, 1],
  });

  return (
    <Animated.View
      pointerEvents="none"
      style={[
        styles.overlay,
        {
          width: size,
          height: size,
          opacity,
          transform: [{ translateY }, { scale }],
        },
      ]}
    >
      <AnimatedImage
        source={GAS_MASK_IMAGE}
        resizeMode="contain"
        style={{ width: size, height: size }}
      />
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  overlay: {
    position: 'absolute',
    left: 0,
    top: 0,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
