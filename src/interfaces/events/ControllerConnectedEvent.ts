export interface AndroidControllerConnectedEvent extends AndroidControllerDisconnectedEvent {
  isMediaNotificationController: boolean;
  isAutomotiveController: boolean;
  isAutoCompanionController: boolean;
}

export interface AndroidControllerDisconnectedEvent {
  package: string;
}
