Math-UnLock (Eclipse project)
===========

multiple choice android unlock app

Setup

  Libraries
  
    android-support-v7-appcompat
    
    Facebook-SDK
    
    google play services lib
    
    Playhaven Android SDK http://help.analytics.upsight.com/api-sdk-reference/downloads/
    
      Need to include google play services lib to this project
      
      Need to change a few lines of code if you want to compile in JRE 1.6 instead of 1.7
      
  Publishing
  
    1. make sure to un-check "Android Dependencies" from all extrernal libraries added to the project. RC->Build Path->Configure Build Path->un-chekc "Android Dependencies"
    
    2. always upload a beta version and test that opening and running the app from the play store works. There is sometimes a change when Google upacks the apk and re-packs it so that it force closes on open. If this happens go back to step 1. of Publishing and try again.
