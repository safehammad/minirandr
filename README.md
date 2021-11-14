# Minirandr

Minirandr is a very simple command line interface to `xrandr` for configuring screens when all you care about is which screens to connect, and how they're arranged. All screens will be connected at native resolution in standard orientation.

## Installation

This software relies on the [xrandr](https://www.x.org/releases/X11R7.5/doc/man/man1/xrandr.1.html) command to configure screens. The command is installed by default on most Linux distros, and can be installed on Mac (though I've not tested this). I'm not aware of a Windows port of `xrandr`.

### Option 1: Babashka Script

This application is written as a Clojure script which can be run from the shell using [Babashka](https://babashka.org/). Install instructions for Babashka can be found at https://github.com/babashka/babashka#installation.

Then simply run the provided `minirandr` shell script in the `bin` directory. For convenience, you can add the `bin` directory to your PATH.

### Option 2: Native Executable

A native executable for your platform can be built using [GraalVM](https://www.graalvm.org/) as follows:

1. Install Clojure -- the [Getting Started Guide](https://clojure.org/guides/getting_started) will get you up and running quickly.

2. Install GraalVM native image as follows:

a) Download GraalVM from https://github.com/graalvm/graalvm-ce-builds/releases

b) Unpack and add the `bin` directory to your PATH.

c) Run:

    $ gu install native-image

3. Finally, you can build the native `minirandr` executable using:

    $ clj -T:build native-image

This will create the standalone execuatable `minirandr` the `target` directory.

## Usage

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

You can quickly configure screen 0 as the single primary screen. This is especially useful when you know you only have a single screen, for example, when disconnecting your laptop from all external screens. Run:

    $ minirandr -s
    xrandr --output eDP1 --auto --primary ---output DP1 --off --output HDMI2 -off

Finally, to see the help, run:

    $ minirandr -h

That's it!
