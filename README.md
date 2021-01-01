# CueDes
![Android CI](https://github.com/Kalaiz/CueLoc/workflows/Android%20CI/badge.svg)
![Status](https://img.shields.io/badge/status-work--in--progress-red)

## Table of Content:
- [Description](#-description)
- [Tools Used](#%EF%B8%8F-tools-used)
- [Reflection](#%EF%B8%8F-reflection)
- [Installation](#%EF%B8%8F-installation)

### 📜 Description:
A simple Android App which rings an alarm when one is about to reach a destination.

<p align="center">
A slight peek at the progress so far.
 </p>
<p align="center">
<img src="resources/app_overview.gif" width="25%" height="25%" /> 
</p>
<p align="center">
Note: I am still working on this personal project. 
 </p>


### 🛠️ Tools Used:
 - ViewBinding, LiveData, DataStore, Room, ViewModel, FragmentStateAdapter, Foreground and Bound Service, Property Animation Framework
 - Views: Motion, Constraint, Linear and Frame Layouts, ~~Spinner~~, RecyclerView, SearchView, ~~TabLayout~~ BottomNavigationView,  ViewPager2, SwitchCompat, OnBoardSupportFragment
  - Google Maps SDK
  - Kotlin: Scope functions, Flow, Co-routines


### ✍️ [Reflection](/resources/reflection.md)



### 🔖 References:
- Learned a gradle way to hide API key from this [google codelab](https://codelabs.developers.google.com/codelabs/maps-platform-101-android#3).
- [Google Map ApiDemos repository](https://developers.google.com/maps/documentation/android-sdk/lite) by Google; Although some parts were obsolete, it gave me gotchas on the usage of the API alongside with `ViewGroup`s such as `RecyclerView`.
- ~~XML style for making `BottomSheetDialog` have rounded corners is adapted from this SO [answer](https://stackoverflow.com/a/50619479/11200630).~~
- ~~Prevented the dismissal of `BottomSheetDialogFragment` upon touching outside/pulling it down via this [method](https://stackoverflow.com/a/50734566/11200630).~~
- Used a Custom ViewPager2 Transition  via this [answer](https://stackoverflow.com/a/59235979/11200630). 
- ~~Expanding `BottomSheetDialogFragment` to a custom height was done with the help of this [answer](https://stackoverflow.com/a/58067230/11200630)~~

### ⚙️ Installation:
Note: It will be easier to do the following via a phone. 
1) Install the [APK](project/app/build/outputs/apk/debug/app-debug.apk).
2) Allow App Installations from Unknown Sources, if requested.


