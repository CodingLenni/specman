"""
Regenerates the Inno Setup wizard images in this directory from the Specman logo
(src/main/resources/images/specman-300.png). Only needed when the logo changes; the generated
BMPs are committed so a normal installer build doesn't depend on Python.

Usage (needs Pillow): python src/main/inno/generate-wizard-images.py

Sizes follow Inno Setup's recommendations for 100%..250% display scaling, so the wizard stays
sharp on high-DPI screens (the .iss lists all of them in WizardImageFile/WizardSmallImageFile).
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).resolve().parent
LOGO = HERE.parent / "resources" / "images" / "specman-300.png"

# (width, height) per scaling step: 100%, 125%, 150%, 175%, 200%, 225%, 250%
LARGE_SIZES = [(164, 314), (192, 386), (246, 459), (273, 556), (328, 628), (355, 680), (410, 797)]
SMALL_SIZES = [(55, 55), (64, 68), (83, 80), (92, 97), (110, 106), (119, 123), (138, 140)]
SUFFIXES = ["100", "125", "150", "175", "200", "225", "250"]

WHITE = (255, 255, 255)
TEXT = (51, 51, 51)
FONT_CANDIDATES = ["C:/Windows/Fonts/segoeuib.ttf", "C:/Windows/Fonts/arialbd.ttf", "DejaVuSans-Bold.ttf"]


def load_font(size):
    for candidate in FONT_CANDIDATES:
        try:
            return ImageFont.truetype(candidate, size)
        except OSError:
            continue
    return ImageFont.load_default()


def logo_scaled(logo, edge):
    return logo.resize((edge, edge), Image.LANCZOS)


def paste_on_white(canvas, logo, x, y):
    # BMP has no alpha channel: composite the rounded logo onto the white background
    canvas.paste(logo, (x, y), logo)


def large_image(logo, size):
    w, h = size
    canvas = Image.new("RGB", size, WHITE)
    edge = int(w * 0.62)
    paste_on_white(canvas, logo_scaled(logo, edge), (w - edge) // 2, int(h * 0.12))
    draw = ImageDraw.Draw(canvas)
    font = load_font(int(w * 0.17))
    text = "Specman"
    box = draw.textbbox((0, 0), text, font=font)
    draw.text(((w - (box[2] - box[0])) // 2 - box[0], int(h * 0.12) + edge + int(h * 0.04)),
              text, font=font, fill=TEXT)
    return canvas


def small_image(logo, size):
    w, h = size
    canvas = Image.new("RGB", size, WHITE)
    edge = min(w, h)
    paste_on_white(canvas, logo_scaled(logo, edge), (w - edge) // 2, (h - edge) // 2)
    return canvas


def main():
    logo = Image.open(LOGO).convert("RGBA")
    for suffix, large, small in zip(SUFFIXES, LARGE_SIZES, SMALL_SIZES):
        large_image(logo, large).save(HERE / f"wizard-large-{suffix}.bmp")
        small_image(logo, small).save(HERE / f"wizard-small-{suffix}.bmp")
    print(f"Wrote {len(SUFFIXES) * 2} images to {HERE}")


if __name__ == "__main__":
    main()
