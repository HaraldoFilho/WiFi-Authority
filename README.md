# **WiFi Authority**

**_WiFi Authority_** is a Wi-Fi networks management application for Android™ on which you can:

- List all WiFi networks configured on the device with multiple sort options
- View all networks available at your current location with multiple filter options
- Add new networks to the device
- Delete networks from device
- Give a description for each configured network
- View detailed information, including the location, of a configured network
- View all configured networks on a map
- View the WiFi connection status
- Automatically reconfigure name changes on networks
- Automatically restore networks removed outside the application
- Store password inside application so it can be restored

### Project Info

Status: **in Production**
- Start Date: Nov 22, 2016
- Alpha release date: Dec 4, 2016
- Beta release date: Jan 2, 2017
- Put on hold: Apr 17, 2017
- Resumed: Jun 26, 2017
- Production release date: Sep 3, 2017

### API Key

This application uses a *Google Maps™* API Key, that for obvious reasons it was removed from this repository. An API Key can be obtained following the instructions here:
https://developers.google.com/maps/documentation/android/signup

After get the key, the file **google_maps_api.xml** must be created at **_./app/src/debug/res/values/_** with the following content:

```xml
<resources>

    <!--Google Maps API Debug Key-->
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
        ### API Key (it starts with "AIza") ###
    </string>

</resources>
```

####
