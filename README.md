# LocatorJobService
geolocation service guide for android 6.0+, which is different due to new Background execution and location limits.

  Starting from Android 6.0 (API level 23), Google introduces [Doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby.html?hl=ru) - power-saving feature, which strongly affect on [Background services](https://developer.android.com/about/versions/oreo/background.html?hl=ru#services) works. Also, Google recommends you to use the JobScheduler to execute background tasks (particularly, because of restrictions applied to your apps while in Doze mode).
  
  This app shows example of using:
  1. Custom JobService and registation in JobScheduler;
  2. Getting geolocation with new FusedLocationProviderClient ( which is strongly recommended to use over [FusedLocationProviderApi](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi?hl=ru) and the Android framework location APIs [android.location](https://developer.android.com/reference/android/location/package-summary.html)  );
  3. Requesting Permissions at Run Time;
