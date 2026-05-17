export interface SoundMapping {
  eventKey: string;
  soundPath: string;
  name: string;
  regex: string;
  isEnabled: boolean;
}

export interface SoundConfig {
  version: string;
  enable: boolean;
  sounds: SoundMapping[];
}
