import { exec, ChildProcess } from 'child_process';
import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { ConfigManager } from '../config/configManager';

export class SoundPlayer {
  private configManager: ConfigManager;
  private currentProcess: ChildProcess | null = null;

  constructor() {
    this.configManager = ConfigManager.getInstance();
    console.log('[SoundPlayer] Initialized');
  }

  playSound(eventKey: string): void {
    console.log(`[SoundPlayer] playSound called for eventKey: ${eventKey}`);

    const config = this.configManager.loadConfig();
    console.log(`[SoundPlayer] Config loaded, enable=${config.enable}`);

    if (!config.enable) {
      console.log('[SoundPlayer] Sound is disabled, skipping');
      return;
    }

    const soundMapping = this.configManager.getSoundMapping(eventKey);
    console.log(`[SoundPlayer] Sound mapping for ${eventKey}: ${JSON.stringify(soundMapping)}`);

    if (!soundMapping) {
      console.log(`[SoundPlayer] No sound mapping found for ${eventKey}`);
      return;
    }

    try {
      const soundPath = soundMapping.soundPath;
      console.log(`[SoundPlayer] Sound path: ${soundPath}`);

      this.stopCurrentSound();

      if (soundPath.startsWith('preset/')) {
        this.playPresetSound(soundPath);
      } else if (soundPath.startsWith('sounds/')) {
        const fileName = soundPath.substring('sounds/'.length);
        this.playPresetSound(`preset/${fileName}`);
      } else if (fs.existsSync(soundPath)) {
        this.playFile(soundPath);
      } else {
        this.playPresetSound(soundPath);
      }
    } catch (e) {
      console.error(`[SoundPlayer] Error playing sound: ${e}`);
    }
  }

  private playPresetSound(soundPath: string): void {
    const resourcePresetDir = this.configManager.getResourcePresetDir();
    const fileName = path.basename(soundPath);

    if (resourcePresetDir) {
      const resourcePath = path.join(resourcePresetDir, fileName);
      if (fs.existsSync(resourcePath)) {
        console.log(`[SoundPlayer] Playing from extension resources: ${resourcePath}`);
        this.playFile(resourcePath);
        return;
      }
    }

    const presetDir = this.configManager.getPresetDir();
    const fullPath = path.join(presetDir, fileName);

    console.log(`[SoundPlayer] Trying user preset dir: ${fullPath}`);

    if (fs.existsSync(fullPath)) {
      this.playFile(fullPath);
    } else {
      console.log(`[SoundPlayer] Preset sound not found: ${fullPath}`);
    }
  }

  private playFile(filePath: string): void {
    const platform = os.platform();
    let command: string;

    if (platform === 'darwin') {
      command = `afplay "${filePath}"`;
    } else if (platform === 'win32') {
      command = `powershell -c (New-Object Media.SoundPlayer '${filePath}').PlaySync()`;
    } else {
      command = `aplay "${filePath}" 2>/dev/null || paplay "${filePath}" 2>/dev/null || true`;
    }

    console.log(`[SoundPlayer] Playing: ${command}`);

    this.currentProcess = exec(command, (error) => {
      if (error) {
        console.error(`[SoundPlayer] Play error: ${error.message}`);
      }
      this.currentProcess = null;
    });
  }

  private stopCurrentSound(): void {
    if (this.currentProcess) {
      try {
        this.currentProcess.kill();
      } catch (e) {
        // ignore
      }
      this.currentProcess = null;
    }
  }
}
