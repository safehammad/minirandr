# Minirandr

Minirandr is a very simple command line interface to `xrandr` for configuring screens when all you care about is which screens to connect, and how they're arranged. All screens will be connected at native resolution in standard orientation.

## Quick start

1. Grab a binary from [Github Releases](https://github.com/safehammad/minirandr/releases) and place it anywhere on your `PATH`.

2. Run `minirandr` to list connected screens. For example:
```
    $ minirandr
    0: eDP1 1920x1080
    1: DP1 1920x1200
    2: HDMI2 2560x1080
```

3. Configure your screens left to right. For example, to configure screen 1 on the left, screen 0 to the right of that as primary screen, and screen 2 to the right of that, run:

    `$ minirandr 1 0p 2`

## Installation

This software relies on the [xrandr](https://www.x.org/releases/X11R7.5/doc/man/man1/xrandr.1.html) command to configure screens. The command is installed by default on most Linux distros, and can be installed on Mac (though I've not tested this). I'm not aware of a Windows port of `xrandr`.

https://github.com/babashka/babashka/releases

### Option 1: Download binary

Grab a binary from [Github Releases](https://github.com/safehammad/minirandr/releases) and place it anywhere on your `PATH`.

### Option 2: Run as Babashka script

This application is written as a Clojure script which can be run from the shell using [Babashka](https://babashka.org/). Install instructions for Babashka can be found at https://github.com/babashka/babashka#installation.

Then simply run the provided shell script `./bin/minirandr`. For convenience, you can add the `bin` directory to your `PATH`.

### Option 3: Build native Executable

You can build a native executable for your own platform using [GraalVM](https://www.graalvm.org/) as follows:

1. Install Clojure -- the [Getting Started Guide](https://clojure.org/guides/getting_started) will get you up and running quickly.

2. Install GraalVM native image as follows:

a) Download GraalVM from https://github.com/graalvm/graalvm-ce-builds/releases

b) Unpack and add the `bin` directory to your PATH.

c) Run:

    $ gu install native-image

3. Finally, you can build the native `minirandr` executable using:

    $ clj -T:build native-image

This will create the standalone executable `minirandr` the `target` directory.

## Usage

### Basic usage

First, call `minirandr -l` to list connected screens. Calling `minirandr` without arguments does the same thing. Each screen is listed alongside a numeric index (0, 1, 2 etc...) and its native resolution. For example:

    $ minirandr
    0: eDP1 1920x1080
    1: DP1 1920x1200
    2: HDMI2 2560x1080

Then decide which screens you want to activate by supplying their screen indexes to `minirandr`. The screens will be arranged *left to right* in the order that the indexes are supplied. For example, to place screen 1 at the far left, then 2 in the middle, and 0 at the right, run the following:

    $ minirandr 1 2 0
    xrandr --output DP1 --auto --output HDMI2 --auto --right-of DP1 --output eDP1 --auto --right-of HDMI2

As you can see, the underlying `xrandr` command being run is conveniently shown.

The `xrandr` command also has the concept of a "primary" screen which commonly determines where your window manager panel appears or where new windows appear by default. The primary screen can be specified by adding the letter "p" to the screen index. In our example setup, to set screen 2 as primary, run the following:

    $ minirandr 1 2p 0
    xrandr --output DP1 --auto --output HDMI2 --auto --primary --right-of DP1 --output eDP1 --auto --right-of HDMI2

Any screen indexes missing from the command will switch that screen off. In our example setup, to switch off screen 0 and leave only screens 1 and 2 active, run the following:

    $ minirandr 1 2p
    xrandr --output DP1 --auto --output HDMI2 --auto --primary --right-of DP1 --output eDP1 --off

### Single screen

To configure a screen as the single screen. Run:

    $ minirandr -s 1p
    xrandr --output DP1 --auto --primary ---output eDP1 --off --output HDMI2 -off

You can also quickly configure screen 0 as the single primary screen. This is especially useful when you know you only have a single screen, for example, after disconnecting your laptop from all external screens, run:

    $ minirandr -s
    xrandr --output eDP1 --auto --primary ---output DP1 --off --output HDMI2 -off

### Mirror screens

To mirror screens, for example screens 0 and 1 with 1 as primary while maintaining their native resolution and setting, run:

    $ minirandr -m 0 2p
    xrandr --output eDP1 --auto --output HDMI2 --auto --primary --same-as eDP1 --output DP1 --off

And as a quick shortcut, to mirror all screens while setting screen 0 as primary, run:

    $ minirandr -m
    xrandr --output eDP1 --auto --primary --output DP1 --auto --same-as eDP1 --output HDMI2 --auto --same-as DP1

### Help

Finally, to see the help, run:

    $ minirandr -h

That's it!
