
package cn.example.consumablesManagement.util

import android.graphics.Bitmap
import android.graphics.Typeface
import cn.example.consumablesManagement.App

object AppData {
    var userName: String = ""
    val typeFonts = Typeface.createFromAsset(App.context.assets, "fonts/HarmonyOS-Font.ttf")!!
    const val INPUTTEXT_PASSWORD = 0x00000081
    const val INPUTTEXT_NORMAL = 0x00000001
    const val AES_KEY = "PASSWORDAAAAAAAAAAAAAA=="
    const val EXIT_BY_USER = "ExitByUser"
    const val IMAGE_URI = "ImageUri"
    const val IMAGE_FILE_PATH = "ImageFile"
    const val RV_APPLICATION = 2
    const val RV_REMOVE = 3
    const val RV_ACCEPT = 4
    const val RV_REFUSE = 5
    const val ERROR_CODE = 404
    const val NETWORKTAG_REMOVE = 1000
//    const val SIGN_IN = "http://120.79.132.118:8080/JAVA_WEB/sign/in"
//    const val SIGN_UP = "http://120.79.132.118:8080/JAVA_WEB/sign/up"
//    const val HOME_ADDCSA = "http://120.79.132.118:8080/JAVA_WEB/home/addcsa"
//    const val HOME_GETCSA = "http://120.79.132.118:8080/JAVA_WEB/home/getcsa"
//    const val HOME_APPLYCSA = "http://120.79.132.118:8080/JAVA_WEB/home/applycsa"

    /*const val SIGN_IN = "http://120.79.132.118:8080/JAVA_WEB/sign/in"
    const val SIGN_UP = "http://120.79.132.118:8080/JAVA_WEB/sign/up"
    const val HOME_ADDCSA = "http://120.79.132.118:8080/JAVA_WEB/home/addcsa"
    const val HOME_GETCSA = "http://120.79.132.118:8080/JAVA_WEB/home/getcsa"
    const val HOME_GETCSA_USERDATA = "http://120.79.132.118:8080/JAVA_WEB/home/getcsa/userdata"
    const val HOME_GETCSA_APPLY_INFO = "http://120.79.132.118:8080/JAVA_WEB/home/getcsa/applyinfo"
    const val HOME_APPLYCSA = "http://120.79.132.118:8080/JAVA_WEB/home/applycsa"
    const val HOME_REMOVECSA = "http://120.79.132.118:8080/JAVA_WEB/home/removecsa"
    const val HOME_ACCEPT_APPLY = "http://120.79.132.118:8080/JAVA_WEB/home/acceptcsa"
    const val HOME_REMOVE_APPLY = "http://120.79.132.118:8080/JAVA_WEB/home/removecsa/applyinfo"
    const val HOME_REFUSE_APPLY = "http://120.79.132.118:8080/JAVA_WEB/home/refusecsa/applyinfo"
    const val HOME_GETCSA_IMAGE = "http://120.79.132.118:8080/JAVA_WEB"*/
    const val SIGN_IN = "http://192.168.1.145:8080/sign/in"
    const val SIGN_UP = "http://192.168.1.145:8080/sign/up"
    const val HOME_ADDCSA = "http://192.168.1.145:8080/home/addcsa"
    const val HOME_GETCSA = "http://192.168.1.145:8080/home/getcsa"
    const val HOME_GETCSA_USERDATA = "http://192.168.1.145:8080/home/getcsa/userdata"
    const val HOME_GETCSA_APPLY_INFO = "http://192.168.1.145:8080/home/getcsa/applyinfo"
    const val HOME_APPLYCSA = "http://192.168.1.145:8080/home/applycsa"
    const val HOME_REMOVECSA = "http://192.168.1.145:8080/home/removecsa"
    const val HOME_ACCEPT_APPLY = "http://192.168.1.145:8080/home/acceptcsa"
    const val HOME_REMOVE_APPLY = "http://192.168.1.145:8080/home/removecsa/applyinfo"
    const val HOME_REFUSE_APPLY = "http://192.168.1.145:8080/home/refusecsa/applyinfo"
    const val HOME_GETCSA_IMAGE = "http://192.168.1.145:8080"

}