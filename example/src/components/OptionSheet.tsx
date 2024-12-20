import SegmentedControl from '@react-native-segmented-control/segmented-control';
import React, { useState } from 'react';
import { Platform, ScrollView, StyleSheet, Text, View } from 'react-native';
import TrackPlayer, {
  AppKilledPlaybackBehavior,
  Capability,
  RepeatMode,
} from 'react-native-track-player';
import { DefaultAudioServiceBehaviour, DefaultRepeatMode } from '../services';
import { Spacer } from './Spacer';

export const OptionStack: React.FC<{
  children: React.ReactNode;
  vertical?: boolean;
}> = ({ children, vertical }) => {
  const childrenArray = React.Children.toArray(children);

  return (
    <View style={vertical ? styles.optionColumn : styles.optionRow}>
      {childrenArray.map((child, index) => (
        <View key={index}>{child}</View>
      ))}
    </View>
  );
};

export const OptionSheet: React.FC = () => {
  const [selectedRepeatMode, setSelectedRepeatMode] = useState(
    repeatModeToIndex(DefaultRepeatMode)
  );

  const [selectedAudioServiceBehaviour, setSelectedAudioServiceBehaviour] =
    useState(audioServiceBehaviourToIndex(DefaultAudioServiceBehaviour));

  return (
    <ScrollView>
      <OptionStack vertical={true}>
        <Text style={styles.optionRowLabel}>Repeat Mode</Text>
        <Spacer />
        <SegmentedControl
          appearance={'dark'}
          values={['Off', 'Track', 'Queue']}
          selectedIndex={selectedRepeatMode}
          onChange={async (event) => {
            setSelectedRepeatMode(event.nativeEvent.selectedSegmentIndex);
            const repeatMode = repeatModeFromIndex(
              event.nativeEvent.selectedSegmentIndex
            );
            await TrackPlayer.setRepeatMode(repeatMode);
          }}
        />
      </OptionStack>
      <Spacer />
      {Platform.OS === 'android' && (
        <OptionStack vertical={true}>
          <Text style={styles.optionRowLabel}>Audio Service on App Kill</Text>
          <Spacer />
          <SegmentedControl
            appearance={'dark'}
            values={['Continue', 'Pause', 'Stop & Remove']}
            selectedIndex={selectedAudioServiceBehaviour}
            onChange={async (event) => {
              setSelectedAudioServiceBehaviour(
                event.nativeEvent.selectedSegmentIndex
              );
              const appKilledPlaybackBehavior = audioServiceBehaviourFromIndex(
                event.nativeEvent.selectedSegmentIndex
              );

              // TODO: Copied from example/src/services/SetupService.tsx until updateOptions
              // allows for partial updates (i.e. only android.appKilledPlaybackBehavior).
              await TrackPlayer.updateOptions({
                android: {
                  appKilledPlaybackBehavior,
                },
                capabilities: [
                  Capability.Play,
                  Capability.Pause,
                  Capability.SkipToNext,
                  Capability.SkipToPrevious,
                  Capability.SeekTo,
                ],
                progressUpdateEventInterval: 2,
              });
            }}
          />
        </OptionStack>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  contentContainer: {
    flex: 1,
    marginTop: '4%',
    marginHorizontal: 16,
  },
  optionRow: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  optionColumn: {
    width: '100%',
    flexDirection: 'column',
  },
  optionRowLabel: {
    color: 'white',
    fontSize: 20,
    fontWeight: '600',
  },
});

const repeatModeFromIndex = (index: number): RepeatMode => {
  switch (index) {
    case 0:
      return RepeatMode.Off;
    case 1:
      return RepeatMode.Track;
    case 2:
      return RepeatMode.Queue;
    default:
      return RepeatMode.Off;
  }
};

const repeatModeToIndex = (repeatMode: RepeatMode): number => {
  switch (repeatMode) {
    case RepeatMode.Off:
      return 0;
    case RepeatMode.Track:
      return 1;
    case RepeatMode.Queue:
      return 2;
    default:
      return 0;
  }
};

const audioServiceBehaviourFromIndex = (
  index: number
): AppKilledPlaybackBehavior => {
  switch (index) {
    case 0:
      return AppKilledPlaybackBehavior.ContinuePlayback;
    case 1:
      return AppKilledPlaybackBehavior.PausePlayback;
    case 2:
      return AppKilledPlaybackBehavior.StopPlaybackAndRemoveNotification;
    default:
      return AppKilledPlaybackBehavior.ContinuePlayback;
  }
};

const audioServiceBehaviourToIndex = (
  audioServiceBehaviour: AppKilledPlaybackBehavior
): number => {
  switch (audioServiceBehaviour) {
    case AppKilledPlaybackBehavior.ContinuePlayback:
      return 0;
    case AppKilledPlaybackBehavior.PausePlayback:
      return 1;
    case AppKilledPlaybackBehavior.StopPlaybackAndRemoveNotification:
      return 2;
    default:
      return 0;
  }
};
