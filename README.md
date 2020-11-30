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
Preview of work done till now. 
 </p>
<p align="center">
<img src="/Resources/App_Overview.gif" width="25%" height="25%" /> 
</p>
<p align="center">
Note: I am still working on this personal project. 
 </p>


### 🛠️ Tools Used:
 - ViewBinding, LiveData , ViewModels, FragmentStateAdapter , Foreground and Bound Service
 - Google Maps SDK
 - Views: Co-ordinator,Constraint,Linear and Frame Layouts ,Spinner, RecyclerView, SearchView, ~~TabLayout~~ BottomNavigationView,  ViewPager2, SwitchCompat
  - Kotlin

### ✍️ Reflection:
- Package organisation - went with Packaging via feature after having a skim through [this](https://proandroiddev.com/package-by-type-by-layer-by-feature-vs-package-by-layered-feature-e59921a4dffa).

- Learned a few things about styles and themes. I learned it for the SwitchCompat, and mad it such that it is red when unchecked and green when checked. 
- Tried to reduce redundant layout code for tabitem by using <include> however <include> only overwrite layout based attributes. Based on the [tabitem documentation.](https://stackoverflow.com/a/38035415/11200630), 3 attributes are provided for TabItem and they can be used alongside with the layout declaration.
- One of the SwitchCompat was white in color despite applying a themeoverlay. A [unresolved SO post](https://stackoverflow.com/questions/59086466/after-migration-to-androidx-switchcompat-is-white) states that the OP had a similar issue as well. I made the MainActivity extend AppCompactActivity instead to overcome this problem.
- Learned about constraint weight, which help me achieve layout suitable for different device size. I faced a challenge in which i needed to place an image with a width of 1/3rd the screen width. I wanted to do this declaritively instead of programmatically. I eventually managed to do this by having a linear layout within which i placed the image alongside with a void ( used view instead of space as space is deprecated) 
- In order to hide my Google Map SDK API key, I had to encapsulate key in a gradle property file and then acccess it via meta data created by the app level gradle build. By default, the repo will not contain the API key, thus affecting the installation APK. In order to overcome this, I un-ignored the apk file created in gitignore and have linked it in the Installation instruction.
- Got to know about the existence of bottomNavigationView, although I had already achieved a similar result using a tablayout.I changed it to a BottomNavigationView as it allow the [bar can disappear on scroll, based on HideBottomViewOnScrollBehavior, when it is placed within a CoordinatorLayout and one of the children within the CoordinatorLayout is scrolled](https://developer.android.com/reference/com/google/android/material/bottomnavigation/BottomNavigationView). 
- I had to make sure the application rings an alarm even when the user is not interacting with the application. This seemed like a long running task  which  1) constantly check the location 2) use that location to check whether it is going to cause an alarm. I went with [Services](https://developer.android.com/guide/components/services) in order to achieve this. I also needed to gain information about the location so that the application is informed as well and hence went with a bound service. By making it a bound service, the service will run as long 

### 🔖 References:
Learned a gradle way to hide API key from this [google codelab](https://codelabs.developers.google.com/codelabs/maps-platform-101-android#3)

### ⚙️ Installation:
Note: It will be easier to do the following via a phone. 
1) Install the [APK](Project/app/build/debug/apk-debug.apk).
2) Allow App Installations from Unknown Sources, if requested.


