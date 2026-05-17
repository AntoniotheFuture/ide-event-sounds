import { ConfigManager } from '../config/configManager';
import { SoundMapping } from '../config/types';
import { SoundPlayer } from '../soundPlayer/soundPlayer';

export class EventMatcher {
  private configManager: ConfigManager;
  private soundPlayer: SoundPlayer;
  private regexCache: Map<string, RegExp> = new Map();

  constructor() {
    this.configManager = ConfigManager.getInstance();
    this.soundPlayer = new SoundPlayer();
  }

  matchAndPlay(eventKey: string, message?: string): void {
    const config = this.configManager.loadConfig();
    if (!config.enable) {
      return;
    }

    if (eventKey === 'notification' && message) {
      this.matchAndPlayNotification(message);
      return;
    }

    const soundMapping = this.configManager.getSoundMapping(eventKey);
    if (!soundMapping) {
      return;
    }

    if (!soundMapping.isEnabled) {
      return;
    }

    if (soundMapping.regex && message) {
      if (!this.matchesRegex(soundMapping.regex, message)) {
        return;
      }
    }

    this.soundPlayer.playSound(eventKey);
  }

  private matchAndPlayNotification(message: string): void {
    const config = this.configManager.loadConfig();
    const notificationMappings = config.sounds.filter(
      s => s.eventKey === 'notification' && s.isEnabled
    );

    for (const soundMapping of notificationMappings) {
      if (soundMapping.regex && this.matchesRegex(soundMapping.regex, message)) {
        this.playSoundFromMapping(soundMapping);
        return;
      }
    }
  }

  private playSoundFromMapping(soundMapping: SoundMapping): void {
    try {
      if (soundMapping.soundPath.startsWith('preset/') || soundMapping.soundPath.startsWith('sounds/')) {
        this.soundPlayer.playSound(soundMapping.eventKey);
      } else {
        this.soundPlayer.playSound(soundMapping.eventKey);
      }
    } catch (e) {
      console.error(`[EventMatcher] Error playing sound: ${e}`);
    }
  }

  private matchesRegex(pattern: string, message: string): boolean {
    try {
      let regex = this.regexCache.get(pattern);
      if (!regex) {
        regex = new RegExp(pattern, 'i');
        this.regexCache.set(pattern, regex);
      }
      return regex.test(message);
    } catch (e) {
      console.error(`[EventMatcher] Invalid regex pattern: ${pattern}, error: ${e}`);
      return true;
    }
  }

  testRegex(pattern: string, testMessage: string): boolean {
    try {
      const regex = new RegExp(pattern, 'i');
      return regex.test(testMessage);
    } catch (e) {
      console.error(`[EventMatcher] Regex test failed: ${e}`);
      return false;
    }
  }
}
