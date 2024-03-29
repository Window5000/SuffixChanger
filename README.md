# SuffixChanger
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

Paper plugin to change LuckPerms suffixes via a GUI.

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

# Install

[Download the plugin](https://hangar.papermc.io/Window5/SuffixChanger) and add it to your ``plugins`` folder.

## Dependencies

This plugin obviously requires [LuckPerms](https://luckperms.net/download) to function.

# Usage

Create a group for each of your suffixes, then add them all to a track called ``suffixes``
Before players can change their suffix, you need to give them the permissions ``suffix.[group name of suffix]`` of every suffix they're allowed to use.

## Commands:
- ``/suffix`` Open the SuffixChanger GUI
- ``/addsuffix <group_name> <suffix...(use as many args here as you wish)>`` Add a group with the specified suffix to luckperms

## Permissions:
  - ``suffix.[group name of suffix]`` for every suffix the player is allowed to use
  - ``suffixchanger.suffix`` to open the GUI
  - ``suffixchanger.addsuffix`` to add suffixes via a command

# Configuration
  - ``watermark`` set to false to disable the watermark under the leftover buttons
  - ``obfuscate`` set to true to obfuscate suffixes that the players isn't allowed to use

# Contributing

PRs accepted.

Small note: If editing the Readme, please conform to the [standard-readme](https://github.com/RichardLitt/standard-readme) specification.

# License

[GNU GPL](LICENSE) © Window5
