# CueDes
![Android CI](https://github.com/Kalaiz/CueLoc/workflows/Android%20CI/badge.svg)
## Table of Content:
- [Description](#-description)
- [Tools Used](#%EF%B8%8F-tools-used)
- [Reflection](#%EF%B8%8F-reflection)
- [Installation](#%EF%B8%8F-installation)

### 📜 Description:


<p align="center">

</p>

### 🛠️ Tools Used:
 - ViewPager2,TabLayout,FragmentStateAdapter,Co-ordinator,Constraint,Linear Layout, Spinner,
 - Kotlin

### ✍️ Reflection:
Challenges - Package organisation - went with Packaging via feature after having a skim through [this](https://proandroiddev.com/package-by-type-by-layer-by-feature-vs-package-by-layered-feature-e59921a4dffa).

- Learned a few things about styles and themes Learned it for the SwitchCompat. 
- Tried to reduce redundant layout code for tabitem by using <include> however <include> only overwrite layout based attributes. Based on the [tabitem documentation.](https://stackoverflow.com/a/38035415/11200630), 3 attributes are provided for TabItem and they can be used alongside with the layout declaration.
- One of the SwitchCompat was white in color dewspite applying a theme. A [SO post](https://stackoverflow.com/questions/59086466/after-migration-to-androidx-switchcompat-is-white) states that the OP had a similar issue as well. I made the MainActivity extend AppCompactActivity instead to overcome this problem.
- Learned about constaint weight, which help me achieve layout suitable for different device size. I faced a challenge in which i needed to place an image with a width of 1/3rd the screen width. I wanted to do this declaritively instead of programmatically. I eventually managed to do this by having a linear layout within which i placed the image alongside with a void ( used view instead of space as space is deprecated) 


### ⚙️ Installation:

