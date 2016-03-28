# Red Moon

Red Moon is a screen filter app for night time phone use. It helps you
sleep after using your phone, by providing a red and dimming filter
that is easy on the eyes. It has separate color temperature, intensity
and dim level settings. Furthermore it has standard profiles and user
customisable profiles.

[![F-Droid](https://f-droid.org/wiki/images/0/06/F-Droid-button_get-it-on.png)](https://f-droid.org/repository/browse/?fdid=com.jmstudios.redmoon)

## Features
* Free and open source (MIT License)
* Separate color temperature, intensity and dim level settings for
complete control
* Persistent notification with pause and stop action
* Automatic startup feature
* Standard profiles
* Custom profiles
* Automatic turn on and off times
* Real-time color, intensity and dim level indicators
* A widget to toggle the filter
* A shortcut to toggle the filter
* Automatically lower the screen brightness as long as the filter is running
* Material design
* Dark and light theme
* Smooth transitions when turning the filter on or off
* Ability to dim the navigation and the notification bar

## Planned feature
* Base turn on and off times on sunset and sunrise times at the user's location (issue #5)

## Screenshots
<img src="https://lut.im/vmFN3vnhn7/anZettuas7khW5l9.png" width="180" height="320" />
<img src="https://lut.im/oymEd1HoVK/YWEpkIPVNOzPfm0O.png" width="180" height="320" />
<img src="https://lut.im/XDgAt3mSOx/WZO1rmwVaM1gS4Qk.png" width="180" height="320" />
<img src="https://lut.im/mBVVEtCZj6/tUNoPKUPoXOc29es.png" width="180" height="320" />
<img src="https://lut.im/EmFrykMlFy/ZIdJvWfV9w7PuVb4.png" width="180" height="320" />
<img src="https://lut.im/WrQySHuyyB/Z6Hy5x22gw9XZNFn.png" width="180" height="320" />


## License
The source code of Red Moon is distributed under the MIT License (see the
LICENSE file in the root folder of the project).

All used artwork is released into the public domain. Some of the icons use
cliparts from [openclipart.org](https://openclipart.org/), which are all
released in the public domain, namely:
* https://openclipart.org/detail/121903/full-moon
* https://openclipart.org/detail/213998/nexus-5-flat
* https://openclipart.org/detail/219211/option-button-symbol-minimal-svg-markup
* https://openclipart.org/detail/20806/wolf-head-howl-1

Red Moon is based on [Shades](https://github.com/cngu/shades), by Chris Nguyen.

## Building
To build the app on GNU+Linux, clone the repository and run

```
./gradlew build
```

in the root directory.

Use

```
./gradlew installDebug
```

to install the app on a connected device.
