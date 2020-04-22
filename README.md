
# Geofence Android Demo
The application is basically based on a geofence detection.

## Description:
In this Application you can add a location based task so whenever you add a task based on location you will be notify when you entered into your desired location with the message **"Task need to be completed"** and  the list of tasks will be shown which you were added, also when you exit from that location you will be notify too that your task has been completed. Location detect with the Fused API after every 15sec new location will be feteched and Geofence class will check whether the current location is the desired location or not.

## Features:

 - Geofence location based tasks.
 - Support of wearable.
 - Google autocomplete places.
 - Location is showing on google maps.
 - Tasks handled locally via Realm Database.

## Wearable Feature:
The Wearable application also notify when the watch is paired with the device. **"watch module"** added in the project.

**Important**
>To create Wearable app wearable module needs to have min SDK [21](https://www.android.com/versions/lollipop-5-0/)

### Wearable Configuration:

**Step 1:** Installed Android Wear application from Google play store on both devices phone & wear emulator. Below is the Link https://play.google.com/store/apps/details?id=com.google.android.wearable.app&hl=en

**Step 2:** Go to wear emulator Settings -> System -> About -> Tab the build number 7 times to enable the developer option.

**Step 3:** Then go to wear emulator Developer Option and checked “ADB Debugging” and “Debug over Bluetooth”.

**Step 4:** Then go to android SDK location “platform-tools” and open command window and run the below command.

```
 - adb devices
 - adb.exe -d forward tcp:5601 tcp:5601 
```
**Step 5:** On the phone, in the Android Wear app, begin the standard pairing process. For example, on the Welcome screen, tap the Set it up button. Alternatively, if an existing watch already is paired, in the upper-left drop-down, tap Add a New Watch.

**Step 6:** On the phone, in the Android Wear app, tap the Overflow button, and then tap Pair with Emulator.

**Step 7:** Tap the Settings icon.

**Step 8:** Under Device Settings, tap Emulator.

**Step 9:** Tap Accounts and select a Google Account, and follow the steps in the wizard to sync the account with the emulator. If necessary, type the screen-lock device password, and Google Account password, to start the account sync.

**Step 10:** That’s it now you paired your device with android wear emulator.
