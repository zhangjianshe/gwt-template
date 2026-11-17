"""Render launcher icons from the existing cangling-update vector (requires rsvg-convert)."""
import copy
import json
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BRANDING = ROOT / 'assets/branding'
NS = 'http://www.w3.org/2000/svg'
ET.register_namespace('', NS)
source = ET.parse(BRANDING / 'image-bot.source.svg').getroot()
symbol = source.find(f'{{{NS}}}g')


def svg(name, box, background=False):
    root = ET.Element(f'{{{NS}}}svg', {'viewBox': box})
    defs = ET.SubElement(root, f'{{{NS}}}defs')
    gradient = ET.SubElement(defs, f'{{{NS}}}linearGradient', {'id': 'sky', 'x2': '1', 'y2': '1'})
    ET.SubElement(gradient, f'{{{NS}}}stop', {'stop-color': '#F1F8FF'})
    ET.SubElement(gradient, f'{{{NS}}}stop', {'offset': '1', 'stop-color': '#C9DFF8'})
    if background:
        ET.SubElement(root, f'{{{NS}}}rect', {'x': '-12', 'y': '-12', 'width': '216', 'height': '216', 'fill': 'url(#sky)'})
    ET.SubElement(root, f'{{{NS}}}circle', {'cx': '96', 'cy': '96', 'r': '88', 'fill': '#FFFFFF', 'opacity': '.56'})
    ET.SubElement(root, f'{{{NS}}}ellipse', {'cx': '94', 'cy': '165', 'rx': '56', 'ry': '8', 'fill': '#24558C', 'opacity': '.12'})
    root.append(copy.deepcopy(symbol))
    path = BRANDING / name
    ET.ElementTree(root).write(path, encoding='unicode')
    return path


legacy = svg('app-icon.svg', '-12 -12 216 216', True)
foreground = svg('app-icon-foreground.svg', '-48 -48 288 288')


def render(source, destination, size):
    destination.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(['rsvg-convert', '-w', str(size), '-h', str(size), '-o', str(destination), str(source)], check=True)


render(legacy, BRANDING / 'app-icon.png', 1024)
res = ROOT / 'android/app/src/main/res'
for density, size, fg_size in [('mdpi', 48, 108), ('hdpi', 72, 162), ('xhdpi', 96, 216), ('xxhdpi', 144, 324), ('xxxhdpi', 192, 432)]:
    render(legacy, res / f'mipmap-{density}/ic_launcher.png', size)
    render(foreground, res / f'mipmap-{density}/ic_launcher_foreground.png', fg_size)

adaptive = res / 'mipmap-anydpi-v26'
adaptive.mkdir(parents=True, exist_ok=True)
(adaptive / 'ic_launcher.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@android:color/white"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
''')
ios = ROOT / 'ios/Runner/Assets.xcassets/AppIcon.appiconset'
for image in json.loads((ios / 'Contents.json').read_text())['images']:
    if 'filename' in image:
        size = round(float(image['size'].split('x')[0]) * float(image['scale'].rstrip('x')))
        render(legacy, ios / image['filename'], size)
