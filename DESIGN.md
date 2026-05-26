# Design

## Theme

AnkiNBT documentation is read by players and maintainers while switching between a launcher, a browser and Minecraft. The site uses a dark technical shell by default to match that context, with a lighter mode still supported by Material for MkDocs.

## Color

Use OKLCH tokens in custom CSS:

- Identity cyan: `oklch(0.72 0.16 225)`
- Deep cyan: `oklch(0.45 0.14 238)`
- Release pink: `oklch(0.67 0.22 358)`
- Ink: `oklch(0.92 0.015 245)`
- Muted text: `oklch(0.74 0.04 245)`
- Surface: dark blue-tinted neutrals, never pure black

Color strategy: restrained with one visible cyan accent. Pink appears for version-fix moments only.

## Typography

Use the Material default stack. Make hierarchy through size and weight, not decorative font changes. Keep display headings compact enough for documentation pages and avoid huge marketing typography.

## Layout

The home page should work like a documentation command center:

- first fold: concise identity, primary actions, version rail
- second fold: three clear paths
- third fold: feature and release map

Use full-width bands and restrained bordered panels. Avoid nested cards and repeated icon-card grids.

## Components

- Hero: asymmetric text plus compact editor preview, not a giant centered card
- Route strip: version and Java summary
- Path panels: one panel for install, one for editing, one for release/build
- Language switch: handled by mkdocs-static-i18n with separate language folders

## Motion

Use short opacity and transform transitions only. Respect `prefers-reduced-motion`.
