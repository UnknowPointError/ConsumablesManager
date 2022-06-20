package cn.example.consumablesManagement.view.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.util.AppData
import com.zackratos.ultimatebarx.ultimatebarx.statusBar

class ImageViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        findViewById<ImageView>(R.id.pictureImageView).setImageBitmap(
            rotateIfRequired(
                BitmapFactory.decodeStream(
                    intent.getParcelableExtra<Uri>(AppData.IMAGE_URI)
                        ?.let { this.contentResolver?.openInputStream(it) })
            )
        )
        supportActionBar?.hide()
        statusBar {
            fitWindow = true
            drawableRes = R.color.homeBarColor
        }
    }

    // [2021/12/10 22:58] BitMap角度旋转判断及设置
    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = intent.getStringExtra(AppData.IMAGE_FILE_PATH)?.let { ExifInterface(it) }
        return when (exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    // [2021/12/10 22:59] 返回Bitmap旋转后的结果
    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        Matrix().apply {
            postRotate(degree.toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, this, true)
        }
    }
}