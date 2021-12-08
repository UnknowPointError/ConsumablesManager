package cn.example.consumablesManagement.util

import Decoder.BASE64Decoder
import Decoder.BASE64Encoder
import cn.example.consumablesManagement.util.AES.decrypt
import cn.example.consumablesManagement.util.AES.encrypt
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.Exception
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object AES {
    private val cipher = Cipher.getInstance("AES")

    //生成AES秘钥，然后Base64编码
    @Throws(Exception::class)
    fun genKeyAES(): String {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(128)
        val key = keyGen.generateKey()
        return byte2Base64(key.encoded)
    }

    //将Base64编码后的AES秘钥转换成SecretKey对象
    @Throws(Exception::class)
    fun loadKeyAES(base64Key: String?): SecretKey {
        val bytes = base642Byte(base64Key)
        return SecretKeySpec(bytes, "AES")
    }

    //字节数组转Base64编码
    private fun byte2Base64(bytes: ByteArray?): String {
        val encoder = BASE64Encoder()
        return encoder.encode(bytes)
    }

    //Base64编码转字节数组
    @Throws(IOException::class)
    private fun base642Byte(base64Key: String?): ByteArray {
        val decoder = BASE64Decoder()
        return decoder.decodeBuffer(base64Key)
    }

    //加密
    @Throws(Exception::class)
    private fun encryptAES(source: ByteArray?, key: SecretKey?): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(source)
    }

    //解密
    @Throws(Exception::class)
    private fun decryptAES(source: ByteArray?, key: SecretKey?): ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(source)
    }

    fun String.encrypt(key: SecretKey) = byte2Base64(encryptAES(this.toByteArray(), key))

    fun String.decrypt(key: SecretKey) = String(decryptAES(base642Byte(this), key))
}