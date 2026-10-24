"""Builds the app font files for Baloo 2 and Nunito from the Google Fonts variable fonts.

    pip install fonttools
    python3 design/fonts/build-fonts.py

Downloads the variable fonts from github.com/google/fonts, cuts one static file per weight
listed under "font" in design/tokens.json, keeps only Latin characters (French and English), and writes them to
the Android res/font folder and the iOS Fonts folder. Both fonts are under the SIL Open Font
License, copied next to this script.
"""
import io
import json
import urllib.request
from pathlib import Path

from fontTools import subset
from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

ROOT = Path(__file__).resolve().parents[2]
SOURCE = "https://raw.githubusercontent.com/google/fonts/main/ofl/{folder}/{file}"
# Same naming as design/build-tokens.mjs: res/font/nunito_extrabold.ttf, PostScript name Nunito-ExtraBold.
STYLES = {400: "Regular", 500: "Medium", 600: "SemiBold", 700: "Bold", 800: "ExtraBold"}
# Google Fonts' latin and latin-ext ranges, enough for French.
LATIN = "U+0000-024F,U+0259,U+02BB-02BC,U+02C6,U+02DA,U+02DC,U+1E00-1EFF,U+2000-206F,U+2074,U+20AC,U+2122,U+2191,U+2193,U+2212,U+2215,U+FEFF,U+FFFD"
OUTPUTS = [ROOT / "app-android/src/main/res/font", ROOT / "app-ios/CoachPoulpy/Fonts"]


def download(folder: str, file: str) -> bytes:
    url = SOURCE.format(folder=folder, file=file.replace("[", "%5B").replace("]", "%5D"))
    with urllib.request.urlopen(url) as response:
        return response.read()


def static_instance(variable: bytes, weight: int) -> TTFont:
    font = instancer.instantiateVariableFont(TTFont(io.BytesIO(variable)), {"wght": weight}, updateFontNames=True)
    options = subset.Options()
    options.layout_features = ["*"]
    options.name_IDs = ["*"]
    options.notdef_outline = True
    subsetter = subset.Subsetter(options)
    subsetter.populate(unicodes=subset.parse_unicodes(LATIN))
    subsetter.subset(font)
    return font


tokens = json.loads((ROOT / "design/tokens.json").read_text())
for face in tokens["font"].values():
    compact = face["family"].replace(" ", "")
    variable = download(compact.lower(), f"{compact}[wght].ttf")
    for weight in face["weights"]:
        font = static_instance(variable, weight)
        name = f"{compact.lower()}_{STYLES[weight].lower()}.ttf"
        for out in OUTPUTS:
            out.mkdir(parents=True, exist_ok=True)
            font.save(out / name)
        print(f"{name}  {font['name'].getDebugName(6)}")
