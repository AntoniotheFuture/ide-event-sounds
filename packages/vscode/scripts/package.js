const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const rootDir = path.join(__dirname, '..');
const packageJson = JSON.parse(fs.readFileSync(path.join(rootDir, 'package.json'), 'utf-8'));
const distDir = path.join(rootDir, 'dist');
const outDir = path.join(rootDir, 'out');
const resourcesDir = path.join(rootDir, 'resources');

if (!fs.existsSync(distDir)) {
  fs.mkdirSync(distDir, { recursive: true });
}

const vsixName = `${packageJson.name}-${packageJson.version}.vsix`;
const vsixPath = path.join(distDir, vsixName);

const tmpDir = path.join(distDir, 'tmp');
if (fs.existsSync(tmpDir)) {
  fs.rmSync(tmpDir, { recursive: true, force: true });
}
fs.mkdirSync(tmpDir, { recursive: true });

const extensionDir = path.join(tmpDir, 'extension');
fs.mkdirSync(extensionDir, { recursive: true });

copyRecursive(outDir, path.join(extensionDir, 'out'));
copyRecursive(resourcesDir, path.join(extensionDir, 'resources'));
fs.copyFileSync(
  path.join(rootDir, 'package.json'),
  path.join(extensionDir, 'package.json')
);

const manifest = `<?xml version="1.0" encoding="utf-8"?>
<PackageManifest Version="2.0.0" xmlns="http://schemas.microsoft.com/developer/vsx-schema/2011" xmlns:d="http://schemas.microsoft.com/developer/vsx-schema-design/2011">
  <Metadata>
    <Identity Id="${packageJson.name}" Version="${packageJson.version}" Publisher="${packageJson.publisher}" TargetPlatform="universal" />
    <Properties>
      <Property Id="Microsoft.VisualStudio.Code.Engine" Value="${packageJson.engines.vscode}" />
      <Property Id="Microsoft.VisualStudio.Code.ExtensionDependencies" Value="" />
      <Property Id="Microsoft.VisualStudio.Code.ExtensionPack" Value="" />
      <Property Id="Microsoft.VisualStudio.Code.ExtensionKind" Value="workspace" />
      <Property Id="Microsoft.VisualStudio.Code.LocalizedLanguages" Value="" />
      <Property Id="Microsoft.VisualStudio.Services.Language" Value="zh-Hans" />
    </Properties>
  </Metadata>
  <Installation>
    <InstallationTarget Id="Microsoft.VisualStudio.Code"/>
  </Installation>
  <Dependencies/>
  <Assets>
    <Asset Type="Microsoft.VisualStudio.Code.Manifest" Path="extension/package.json" Addressable="true" />
    <Asset Type="Microsoft.VisualStudio.Services.Content.Details" Path="extension/package.json" Addressable="true" />
  </Assets>
</PackageManifest>`;

fs.writeFileSync(path.join(tmpDir, 'extension.vsixmanifest'), manifest);

const contentTypes = `<?xml version="1.0" encoding="utf-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="json" ContentType="application/json" />
  <Default Extension="js" ContentType="application/javascript" />
  <Default Extension="wav" ContentType="audio/wav" />
  <Default Extension="d.ts" ContentType="text/plain" />
  <Default Extension="map" ContentType="application/json" />
  <Default Extension="xml" ContentType="text/xml" />
  <Default Extension="vsixmanifest" ContentType="text/xml" />
</Types>`;

fs.writeFileSync(path.join(tmpDir, '[Content_Types].xml'), contentTypes);

try {
  execSync(`cd "${tmpDir}" && zip -r "${vsixPath}" .`, { stdio: 'inherit' });
  console.log(`[Package] Created: ${vsixPath}`);
} catch (e) {
  console.error(`[Package] Failed to create VSIX: ${e.message}`);
} finally {
  fs.rmSync(tmpDir, { recursive: true, force: true });
}

function copyRecursive(src, dest) {
  if (!fs.existsSync(src)) return;
  fs.mkdirSync(dest, { recursive: true });
  const entries = fs.readdirSync(src, { withFileTypes: true });
  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      copyRecursive(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

console.log(`[Package] Done! Output: ${vsixName}`);
