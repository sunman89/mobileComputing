Wireless Mobile Computing Coursework

Username: sstoke04
My location issued was Floor 7, corridors between 723 and 736.
Points of interest are: Room 724, Stairs for that floor, and Room 736.

� Github URL = https://github.com/sunman89/mobileComputing

� Circle CI/Travis CI output = was success, all tests passed
https://circleci.com/gh/sunman89/mobileComputing/16 

The rest of your commands were successful:
    Starting the build
    Start container
    Enable SSH
    Restore source cache
    Checkout using deploy key: c7:fb:5b:8c:2a:4f:2b:f8:a6:b3:10:f8:92:69:24:b1
    Configure the build
    Restore cache
    Save cache
    emulator -avd circleci-android24 -no-window
    circle-android wait-for-boot
    echo y | android update sdk --no-ui --all --filter "com.google.gms:google-services:3.0.0"
    echo y | android update sdk --no-ui --all --filter "com.indooratlas.android:indooratlas-android-sdk:2.3.2@aar"
    mkdir -p $CIRCLE_TEST_REPORTS/junit/
    find . -type f -regex ".*/build/test-results/.*xml" -exec cp {} $CIRCLE_TEST_REPORTS/junit/ \;
    Collect test metadata
    Collect artifacts
    Disable SSH


� Any additional comments
Sometimes the app might freeze for a bit whilst getting the users location, that might just be my phone though.
Landscape mode does not seem to work properly, I think that is because of the bearing of the phone being different
to when I mapped the location using my phone. Was told to map the location by using the phone in portrait mode.

I have only done a few unit tests, because the code that I used from the Indoor Atlas website regarding using their sdk to get the location, the methods/variables were always private. And I don't know how to unit test private methods. Some tutorials or linked material to instrument testing would of helped.

I have never used CircleCI before so not sure if I used it properly.It says I have gotten success on it, but a tutorial on this would of been great.

I tried to implement the map tracking on my app. But everytime I was trying to create a map fragment my code kept getting errors. If I had more time to spend on it I would of tried implementing a map fragment that I would of then used a ground overlay, to put the floor image of the current floor the user is on onto the map. And a marker that always is displayed upon location changed and on the current location of the user.
