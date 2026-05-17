import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { SoundConfig, SoundMapping } from './types';

const DEFAULT_CONFIG_DIR = path.join(os.homedir(), '.ide-event-sounds');
const DEFAULT_CONFIG_PATH = path.join(DEFAULT_CONFIG_DIR, 'config.json');
const DEFAULT_PRESET_DIR = path.join(DEFAULT_CONFIG_DIR, 'preset');

const DEFAULT_SOUND_MAPPINGS: SoundMapping[] = [
  { eventKey: 'build.success', soundPath: 'preset/build_success.wav', name: '构建成功', regex: '', isEnabled: true },
  { eventKey: 'build.failed', soundPath: 'preset/build_failed.wav', name: '构建失败', regex: '', isEnabled: true },
  { eventKey: 'run.start', soundPath: 'preset/run_start.wav', name: '运行启动', regex: '', isEnabled: true },
  { eventKey: 'run.stop', soundPath: 'preset/run_stop.wav', name: '运行终止', regex: '', isEnabled: true },
  { eventKey: 'compile.finished', soundPath: 'preset/compile_finished.wav', name: '编译完成', regex: '', isEnabled: true },
  { eventKey: 'test.passed', soundPath: 'preset/test_passed.wav', name: '测试通过', regex: '', isEnabled: true },
  { eventKey: 'test.failed', soundPath: 'preset/test_failed.wav', name: '测试失败', regex: '', isEnabled: true },
  { eventKey: 'project.opened', soundPath: 'preset/project_opened.wav', name: '项目打开', regex: '', isEnabled: true },
];

export class ConfigManager {
  private static instance: ConfigManager;
  private configPath: string;
  private cache: SoundConfig | null = null;
  private resourcePresetDir: string | null = null;

  private constructor() {
    this.configPath = DEFAULT_CONFIG_PATH;
  }

  static getInstance(): ConfigManager {
    if (!ConfigManager.instance) {
      ConfigManager.instance = new ConfigManager();
    }
    return ConfigManager.instance;
  }

  getConfigPath(): string {
    return this.configPath;
  }

  getPresetDir(): string {
    return DEFAULT_PRESET_DIR;
  }

  setResourcePresetDir(dir: string): void {
    this.resourcePresetDir = dir;
  }

  getResourcePresetDir(): string | null {
    return this.resourcePresetDir;
  }

  loadConfig(): SoundConfig {
    try {
      if (!fs.existsSync(this.configPath)) {
        return this.createDefaultConfig();
      }

      const content = fs.readFileSync(this.configPath, 'utf-8');
      const config: SoundConfig = JSON.parse(content);

      if (!config.sounds || config.sounds.length === 0) {
        config.sounds = [...DEFAULT_SOUND_MAPPINGS];
      }

      this.cache = config;
      return config;
    } catch (e) {
      console.error('[ConfigManager] Failed to load config, using defaults:', e);
      return this.createDefaultConfig();
    }
  }

  saveConfig(config: SoundConfig): void {
    try {
      if (!fs.existsSync(DEFAULT_CONFIG_DIR)) {
        fs.mkdirSync(DEFAULT_CONFIG_DIR, { recursive: true });
      }
      fs.writeFileSync(this.configPath, JSON.stringify(config, null, 2), 'utf-8');
      this.cache = config;
    } catch (e) {
      console.error('[ConfigManager] Failed to save config:', e);
    }
  }

  getSoundMapping(eventKey: string): SoundMapping | undefined {
    const config = this.loadConfig();
    return config.sounds.find(s => s.eventKey === eventKey);
  }

  private createDefaultConfig(): SoundConfig {
    const config: SoundConfig = {
      version: '0.0.5',
      enable: true,
      sounds: [...DEFAULT_SOUND_MAPPINGS],
    };

    try {
      if (!fs.existsSync(DEFAULT_CONFIG_DIR)) {
        fs.mkdirSync(DEFAULT_CONFIG_DIR, { recursive: true });
      }
      fs.writeFileSync(this.configPath, JSON.stringify(config, null, 2), 'utf-8');
    } catch (e) {
      console.error('[ConfigManager] Failed to create default config:', e);
    }

    this.cache = config;
    return config;
  }
}
