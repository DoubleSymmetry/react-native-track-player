import {
  AndroidAutoBrowseTree,
  AndroidAutoContentStyle,
} from 'react-native-track-player';

const DemoAndroidAutoHierarchy: AndroidAutoBrowseTree = {
  '/': [
    {
      mediaId: 'tab1',
      title: 'tab1',
      subtitle: 'tab subtitle',
      playable: '1',
      childrenBrowsableContentStyle: String(AndroidAutoContentStyle.Grid),
    },
    {
      mediaId: 'tab2',
      title: 'tab2',
      subtitle: 'tab subtitle',
      playable: '1',
    },
    {
      mediaId: 'tab3',
      title: 'tab3',
      subtitle: 'tab subtitle',
      playable: '1',
    },
  ],
  tab1: [
    {
      mediaId: '1',
      title: 'Soul Searching (Demo)',
      subtitle: 'David Chavez',
      playable: '0',
      iconUri: 'https://rntp.dev/example/Soul%20Searching.jpeg',
      mediaUri: 'https://rntp.dev/example/Soul%20Searching.mp3',
      groupTitle: 'RNTP Demo Group',
      playbackProgress: '0',
    },
    {
      mediaId: '2',
      title: 'Lullaby (Demo)',
      subtitle: 'David Chavez',
      playable: '0',
      iconUri: 'https://rntp.dev/example/Lullaby%20(Demo).jpeg',
      mediaUri: 'https://rntp.dev/example/Lullaby%20(Demo).mp3',
      groupTitle: 'RNTP Demo Group',
      playbackProgress: '0',
    },
  ],
};

export default DemoAndroidAutoHierarchy;
