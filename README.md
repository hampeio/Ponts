# Skyline Span

A reskinned bridge-building game prototype based on the open source
[Ponts](https://github.com/Lysquid/Ponts) project.

The gameplay core is intentionally unchanged:

- physics: JBox2D bridge, car, material, collision, and budget logic
- levels: the original ten sample levels
- editor: the original level authoring flow

This fork only changes the presentation layer:

- window title and UI copy
- Swing/FlatLaf colors
- custom line icons for buttons
- a new original car bitmap
- Windows `compile.ps1` and `run.ps1` helpers

## License and attribution

Ponts is licensed under the MIT License. The original copyright and license are
kept in `LICENSE`. Keep that file when distributing this fork.

The original game credits JBox2D for physics and FlatLaf for the Swing theme.
This fork keeps those dependencies unchanged.

## Run on Windows

Requirement: Java 11 or newer.

```powershell
.\run.ps1
```

To compile without launching:

```powershell
.\compile.ps1
```

## Run on Linux/macOS

```bash
chmod +x compile.sh execute.sh
./compile.sh
./execute.sh
```

## Notes

This is a legal open-source reskin starter, not a copy of the commercial
Poly Bridge codebase. Replace the placeholder visual style with your own art,
icons, title, levels, and audio before publishing.
