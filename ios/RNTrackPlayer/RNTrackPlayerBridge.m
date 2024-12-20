//
//  RNTrackPlayerBridge.m
//  RNTrackPlayerBridge
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import "RNTrackPlayerBridge.h"
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_REMAP_MODULE(TrackPlayerModule, RNTrackPlayer, NSObject)

RCT_EXTERN_METHOD(setupPlayer:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(isServiceRunning:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(updateOptions:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(add:(NSArray *)objects
                  before:(nonnull NSNumber *)trackIndex
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(move:(nonnull NSNumber *)fromIndex
                  toIndex:(nonnull NSNumber *)toIndex
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(load:(NSDictionary *)trackDict
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(remove:(NSArray *)objects
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(removeUpcomingTracks:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(skip:(nonnull NSNumber *)trackIndex
                  initialTime:(double)initialTime
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(skipToNext:(double)initialTime
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(skipToPrevious:(double)initialTime
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(reset:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(play:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(pause:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(stop:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(setPlayWhenReady:(BOOL)playWhenReady
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getPlayWhenReady:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(retry:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(seekTo:(double)time
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(seekBy:(double)offset
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(setRepeatMode:(nonnull NSNumber *)repeatMode
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getRepeatMode:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(setVolume:(float)volume
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getVolume:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(setRate:(float)rate
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getRate:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getTrack:(nonnull NSNumber *)trackIndex
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getQueue:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(setQueue:(NSArray *)objects
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getActiveTrack:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getActiveTrackIndex:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getDuration:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getBufferedPosition:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getPosition:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getProgress:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(getPlaybackState:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(updateMetadataForTrack:(nonnull NSNumber *)trackIndex
                  metadata:(NSDictionary *)metadata
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(clearNowPlayingMetadata:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(updateNowPlayingMetadata:(NSDictionary *)metadata
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

@end
