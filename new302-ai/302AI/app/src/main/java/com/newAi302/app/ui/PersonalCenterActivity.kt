package com.newAi302.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.databinding.ActivityPersonalCenterBinding
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.dialog.DeleteUserDialog
import com.newAi302.app.dialog.RenameDialog
import com.newAi302.app.http.ApiService
import com.newAi302.app.http.NetworkFactory
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.ui.login.LoginOneActivity
import com.newAi302.app.utils.DeviceDetector
import com.newAi302.app.utils.SystemUtils
import com.newAi302.app.utils.ViewAnimationUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.viewModel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PersonalCenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalCenterBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PHOTO_REQUEST = 2
    private val BASE_URL = "https://api.302.ai/"
    private var CUSTOMIZE_URL_TWO = "https://api.siliconflow.cn/"
    private var apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
    private var imageUrlServiceResult = ""
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var currentPhotoPath: String
    private var userName = ""
    private var newUserName = ""
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_personal_center)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        binding = ActivityPersonalCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backImage.setOnClickListener {
            finish()
        }

        chatViewModel.deleteUserResult.observeForever { result ->
            result?.let {
                Log.e("ceshi", "机器人有回复删除：$it,,")
                if (it == "success"){
                    val intent = Intent(this, LoginOneActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this, getString(R.string.delete_user_failed_message), Toast.LENGTH_SHORT).show()
                }

            }

        }

        chatViewModel.changeUserNameResult.observeForever { result ->
            result?.let {
                Log.e("ceshi", "机器人有回复修改名字：$it,,")
                if (it == "success"){
                    userName = newUserName
                    Toast.makeText(this, getString(R.string.change_name_sucess_message), Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch((Dispatchers.IO)) {
                        dataStoreManager.saveUserName(userName)
                    }
                }else{
                    Toast.makeText(this, getString(R.string.change_name_failed_message), Toast.LENGTH_SHORT).show()
                }
                binding.userNameTV.text = userName

            }

        }

        chatViewModel.imageUrlServiceResult.observe(this){
            Log.e("ceshi","返回的图片地址回复：$it")
            it?.let {
                imageUrlServiceResult = it

                lifecycleScope.launch(Dispatchers.IO) {
                    Log.e("ceshi","输入：${imageUrlServiceResult}")
                    dataStoreManager.saveImageUrl(imageUrlServiceResult)
                }
                // 方法1：使用内置的CircleCrop变换
                Glide.with(this)
                    .load(imageUrlServiceResult)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.personalImage)


            }
        }

        binding.userImageFrame.setOnClickListener {
            // 调用相册选择器
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            showBottomSheetMoreDialog()
        }

        binding.personalChangeNameCons.setOnClickListener {
            showRenameDialog(userName)
        }

        binding.personalChangePasswordCons.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        binding.deleteUserConst.setOnClickListener {
            showDeleteUserDialog(userName)
        }



    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch((Dispatchers.IO)) {
            val readUserNameData = dataStoreManager.readUserNameData.first()
            readUserNameData?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e("setting","readUserNameData：$it")
                    userName = it
                    binding.userNameTV.text = userName
                }
            }


            val data = dataStoreManager.readImageUrl.first()
            data?.let {
                Log.e("ceshi", "imageurl是个多少：$it")
                lifecycleScope.launch(Dispatchers.Main) {
                    // 方法1：使用内置的CircleCrop变换
                    Glide.with(this@PersonalCenterActivity)
                        .load(it)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(binding.personalImage)
                }

            }


        }



    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!

            Toast.makeText(this, getString(R.string.personal_upload_image_toast_message), Toast.LENGTH_SHORT).show()
            Log.e("ceshi","设置界面返回图片${selectedImageUri}")

            //上传图片到服务器
            lifecycleScope.launch(Dispatchers.IO) {

                dataStoreManager.saveImageUrl(selectedImageUri.toString())
                lifecycleScope.launch(Dispatchers.Main) {
                    // 方法1：使用内置的CircleCrop变换
                    Glide.with(this@PersonalCenterActivity)
                        .load(selectedImageUri.toString())
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(binding.personalImage)
                }


                chatViewModel.upLoadImage(
                    this@PersonalCenterActivity,
                    SystemUtils.uriToTempFile(this@PersonalCenterActivity, selectedImageUri), "imgs", false,apiService
                )

            }


        }else if(requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK){

            currentPhotoPath?.let { path ->
                val imageFile = File(path)
                //Log.e("ceshi","3图片地址$imageFile")
                if (imageFile.exists()) {
                    val contentUri = Uri.fromFile(imageFile)
                    Toast.makeText(this, getString(R.string.personal_upload_image_toast_message), Toast.LENGTH_SHORT).show()
                    //上传图片到服务器
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveImageUrl(contentUri.toString())
                        lifecycleScope.launch(Dispatchers.Main) {
                            // 方法1：使用内置的CircleCrop变换
                            Glide.with(this@PersonalCenterActivity)
                                .load(contentUri.toString())
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.stat_notify_error)
                                .into(binding.personalImage)
                        }


                        chatViewModel.upLoadImage(this@PersonalCenterActivity,
                            SystemUtils.uriToTempFile(this@PersonalCenterActivity, contentUri),"imgs",false,apiService)
                    }

                    galleryAddPic()
                    //Log.e("ceshi","2图片地址$imageUrlLocal")

                } else {
                    Log.e("Camera", "图片文件不存在: $path")
                }




            } ?: run {
                Log.e("Camera", "未找到保存的图片路径")
            }

        }


    }

    @SuppressLint("MissingInflatedId")
    private fun showBottomSheetMoreDialog() {
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(this@PersonalCenterActivity)

        // 为 BottomSheetDialog 设置布局
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_personal_image_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 获取布局中的 Button，并设置点击事件
        val btnClose = view.findViewById<ImageView>(R.id.btnMoreClose)
        btnClose.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            // 关闭 BottomSheetDialog
            bottomSheetDialog.dismiss()

        }

        view.findViewById<ConstraintLayout>(R.id.cons3).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            bottomSheetDialog.dismiss()

        }

        if (DeviceDetector.isHuaweiDevice()){
            view.findViewById<ConstraintLayout>(R.id.cons2).visibility = View.GONE
        }else{
            view.findViewById<ConstraintLayout>(R.id.cons2).visibility = View.VISIBLE
        }

        view.findViewById<ConstraintLayout>(R.id.cons2).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            dispatchTakePictureIntent()
            bottomSheetDialog.dismiss()
        }

//        view.findViewById<ConstraintLayout>(R.id.cons4).setOnClickListener {
//            // 点击时执行动画效果,上传文件
//            ViewAnimationUtils.performClickEffect(it)
//            openFilePicker()
//            bottomSheetDialog.dismiss()
//
//        }


        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }


    // 调用相机
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // 确保有相机应用可以处理该Intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // 创建临时文件保存照片
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // 处理创建文件失败的情况
                    ex.printStackTrace()
                    null
                }
                // 继续只有在成功创建文件的情况下
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        getFileProviderAuthority(this), // 替换为你的FileProvider authority
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
                }
            }
        }
    }
    // 调用相机（已添加动态权限申请）
    private fun dispatchTakePictureIntent1() {
        // 检查是否有相机权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 已有权限，直接调用相机
            startCameraIntent()
        } else {
            // 没有权限，请求相机权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                0
            )
        }
    }

    // 实际启动相机的方法
    private fun startCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // 确保有相机应用可以处理该Intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // 创建临时文件保存照片
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // 处理创建文件失败的情况
                    ex.printStackTrace()
                    null
                }
                // 继续只有在成功创建文件的情况下
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        getFileProviderAuthority(this), // 替换为你的FileProvider authority
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
                }
            } ?: run {
                // 如果没有找到可以处理相机意图的应用
                Toast.makeText(this, "未找到相机应用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 创建临时图片文件
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 创建唯一文件名
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* 前缀 */
            ".jpg", /* 后缀 */
            storageDir /* 目录 */
        ).apply {
            // 保存文件路径用于后续使用
            currentPhotoPath = absolutePath
        }

    }

    // 将图片添加到系统图库
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    // 根据当前环境动态生成 authority
    fun getFileProviderAuthority(context: Context): String {
        return "${context.packageName}.fileprovider"
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun showRenameDialog(oldName:String) {
        val dialog = RenameDialog(this,oldName)
        //dialog.setDefaultName("原来的名字") // 设置输入框默认文本
        dialog.setOnSaveClickListener { newName ->
            // 点击“保存”后的逻辑，newName 是输入框内容
            //Toast.makeText(this, "新名称：$newName", Toast.LENGTH_SHORT).show()
            // 这里可执行真正的重命名操作，比如更新数据、刷新 UI 等
            val job1 = lifecycleScope.launch(Dispatchers.IO) {
                chatViewModel.changeUserName(WearData.getInstance().token,apiService,newName)
                newUserName = newName
            }


        }
        dialog.setOnCancelClickListener {
            // 点击“取消”后的逻辑
            Toast.makeText(this, ContextCompat.getString(this@PersonalCenterActivity, R.string.cancel_rename_toast_message), Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun showDeleteUserDialog(oldName:String) {
        val dialog = DeleteUserDialog(this,oldName)
        //dialog.setDefaultName("原来的名字") // 设置输入框默认文本
        dialog.setOnSaveClickListener { newName ->
            // 点击“保存”后的逻辑，newName 是输入框内容
            //Toast.makeText(this, "新名称：$newName", Toast.LENGTH_SHORT).show()
            // 这里可执行真正的重命名操作，比如更新数据、刷新 UI 等
            val job1 = lifecycleScope.launch(Dispatchers.IO) {
                chatViewModel.deleteUser(WearData.getInstance().token,apiService)
            }


        }
        dialog.setOnCancelClickListener {
            // 点击“取消”后的逻辑
            Toast.makeText(this, ContextCompat.getString(this@PersonalCenterActivity, R.string.delete_user_cancel_message), Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

}