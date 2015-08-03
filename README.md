# LAS-Sample-Auth-Android

## Overview

Auth is a sample of LAS SDK, and it relies on the basic module of LAS. This app shows the third-party login based on LAS SDK, how to save relative user info  and connect to third-party SDK with LAS SDK.  


## Effect

![capture](capture/auth01.png)

![capture](capture/auth02.png)

![capture](capture/auth03.png)

## Precondition

Login [Facebook Developer Console](https://developers.facebook.com) to create an app and get `FACEBOOK_APP_ID` and `FACEBOOK_SECRET_KEY`.

## How to Use

1. Open Android Studio or IDEA, click `File -> Open `, select and import `setting.gradle`.
2. Open `App.java` and replace the defined constants with your own `APP Id`, `API KEY`, `FACEBOOK_APP_ID` and `FACEBOOK_SECRET_KEY`.
3. Open `strings.xml` and replace `app_id` with `FACEBOOK_APP_ID`.
4. Log in LAS Console, select `Services` - `App Settings` - `User Authentication`, enable `Allow Facebook authentication` and fill your `FACEBOOK_APP_ID` in `Facebook Application` textfield.
5. Run the app and click the Log In button, then you can see the corresponding user info.