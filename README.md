# godot-kotlin-project-template

This is the directory structure and build scripts for the standard Godot/Kotlin project.

## How to use
The contents of this directory should be placed under your Godot project's root directory:
```
<Godot Project>/
- project.godot
- kotlin/ <- This is the directory that contains the contents of this repository
```

Alternatively you can use the `Godot/Kotlin Editor Plugin` which will automatically setup your projects for you by downloading this repository and doing various other tasks.


## End User notes:
The `Godot/Kotlin Editor Plugin` maintains a black list of files that are not important to end user projects. Examples include: `README.md` and `LICENSE`. These will be excluded automatically during setup by the plugin.

If using this repository directly to setup your project, you can safely exclude those files as well if you wish.

## Godot/Kotlin development notes
This repository should be tagged for release, as the `Godot/Kotlin Editor Plugin` will download the source bundles from here when setting up new projects.