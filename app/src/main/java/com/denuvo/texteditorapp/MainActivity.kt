package com.denuvo.texteditorapp

import android.app.*
import android.content.*
import android.content.Context.NOTIFICATION_SERVICE
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denuvo.texteditorapp.Fragment.BottomDialog
import com.denuvo.texteditorapp.databinding.ActivityMainBinding
import com.denuvo.texteditorapp.recycler.ItemAdapter
import com.denuvo.texteditorapp.recycler.TaskData
import com.denuvo.texteditorapp.recycler.ViewHolder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.resources.MaterialResources.getDrawable
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var mAuth: FirebaseAuth
    val bottomsheetDialog = BottomDialog()
    private lateinit var viewModelData: ViewModelData
    var myList = ArrayList<TaskData>()
    var backUpList = ArrayList<TaskData>()
    val callItemAdapter = ItemAdapter(this,myList)
    var persentase = callItemAdapter.persentase
    lateinit var pendingIntent : PendingIntent
    lateinit var alarmManager: AlarmManager
    private var appUpdate: AppUpdateManager? = null
    private val REQUEST_CODE = 100




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sp : SharedPreferences = this.getSharedPreferences("preferences",Context.MODE_PRIVATE)
        binding = ActivityMainBinding.inflate(layoutInflater)
        appUpdate = AppUpdateManagerFactory.create(this)
        checkUpdate()
        loadData()
        //Call Notif
        createNotification()

        val myAdapter = ItemAdapter(this,myList)
        if (myList.size == 0){
            binding.gifMan.visibility = View.VISIBLE
        }

        //Custom Dialog
        val customDialog = Dialog(this)
        customDialog.setContentView(R.layout.custom_dialog)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            customDialog.window!!.setBackgroundDrawable(getDrawable(R.drawable.bg_customdialog))
        }
        customDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        val btnYes : Button = customDialog.findViewById(R.id.btn_yes)
        val btnNo : Button = customDialog.findViewById(R.id.btn_no)
        val message : TextView = customDialog.findViewById(R.id.txt_message)
        val layoutBtn : LinearLayout = customDialog.findViewById(R.id.btn_layout)
        binding.deleteTask.setOnClickListener {
            if (myList.size == 0){
                message.text = "You Dont Have Any Task!"
                message.setTextSize(20f)
                layoutBtn.visibility = View.INVISIBLE
            } else {
                message.text = "Are you sure want to delete all task?"
                message.setTextSize(15f)
                layoutBtn.visibility = View.VISIBLE
            }
            customDialog.show()
        }
        btnYes.setOnClickListener {
            sp.edit().clear().apply()
            myList.clear()
            myAdapter.notifyDataSetChanged()
            persentase = 100f
            binding.txtPersentase.text = "${persentase.toInt()}%"
            binding.circularBar.apply {
                setProgressWithAnimation(persentase,2000)
            }
            binding.gifMan.visibility = View.VISIBLE
            cancelNotif()
            customDialog.dismiss()
        }
        btnNo.setOnClickListener {
            customDialog.dismiss()
        }


        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        viewModelData = ViewModelProvider(this).get(ViewModelData::class.java)
        setContentView(binding.root)
        binding.txtPersentase.text = "${persentase.toInt()}%"
        binding.floatButton.setOnClickListener {
            bottomsheetDialog.show(supportFragmentManager,"sheetDialog")
        }

        binding.recyclerView.adapter = myAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        //show personal info

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val header : View = navigationView.getHeaderView(0)
        val username : TextView = header.findViewById(R.id.txt_usernama)
        val image : ImageView = header.findViewById(R.id.user_profile)
        Glide.with(this).load(currentUser?.photoUrl).into(image)
        username.text = currentUser?.displayName


        //Log out and Email
        val menuNav : Menu = navigationView.menu
        val email = menuNav.findItem(R.id.email)
        email.setTitle(currentUser?.email)
        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.logout -> logout()
                R.id.rate_us -> rateUs()
            }
            true
        }



        //Receive Data From Fragment
        viewModelData.task.observe(this){task ->

            if (myList.size == 20) {
                Toast.makeText(this,"Cant add more task!",Toast.LENGTH_SHORT).show()
            }else {
                val checkDate : CheckBox = bottomsheetDialog.view!!.findViewById(R.id.check_remindme)
                val desc : TextView = bottomsheetDialog.view!!.findViewById(R.id.edt_title)
                val valueDesc = desc.text.toString()
                if (checkDate.isChecked){
                    scheduleNotification()
                }
                binding.gifMan.visibility = View.GONE
                myAdapter.notifyDataSetChanged()
                myList.add(TaskData(valueDesc,task))
                persentase -= 5f
                saveTask(myList,persentase)
                binding.circularBar.apply {
                    setProgressWithAnimation(persentase,1000)
                    binding.txtPersentase.text = "${persentase.toInt()}%"
                }
            }
            Log.d("Task","Task Read it")
        }
        //Swipe To Delete
       val swipeToDeleteCallBack = object : SwipeToDeleteCallBack(){
           override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
               val position = viewHolder.adapterPosition
               when(direction){
                   ItemTouchHelper.LEFT -> {
                       deleteData(myList,position)
                       binding.recyclerView.adapter?.notifyItemRemoved(position)
                       val checked = viewHolder.itemView.findViewById<CheckBox>(R.id.btn_done)
                       if (checked.isChecked == false) {
                           persentase += 5f
                           val editor = sp.edit()
                           editor.putFloat("valuee",persentase)
                           editor.apply()
                           cancelNotif()
                           binding.circularBar.apply {
                               setProgressWithAnimation(persentase,1000)
                               binding.txtPersentase.text = "${persentase.toInt()}%"
                           }
                       } else{
                           saveTask(myList,persentase)
                       }
                       checked.isChecked = false
                   }
                   ItemTouchHelper.RIGHT -> {
                       var editText = EditText(this@MainActivity)
                       var desc = EditText(this@MainActivity)
                       editText.setText("Update Title")
                       desc.setText("Update Task")
                       val builder = AlertDialog.Builder(this@MainActivity)
                       builder.setTitle("Update Task")
                       var layout = LinearLayout(this@MainActivity)
                       layout.setOrientation(LinearLayout.VERTICAL)
                       layout.addView(editText)
                       layout.addView(desc)
                       builder.setCancelable(true)
                       builder.setView(layout)
                       builder.setNegativeButton("Cancel",DialogInterface.OnClickListener{dialog, which ->
                           myList.clear()
                           myList.addAll(backUpList)
                           myAdapter.notifyDataSetChanged()
                       })
                       builder.setPositiveButton("Update",DialogInterface.OnClickListener { dialog, which ->
                           myList.set(position, TaskData(editText.text.toString(),desc.text.toString()))
                           myAdapter.notifyDataSetChanged()
                           saveTask(myList,persentase)
                       })
                       builder.show()
                   }
               }

           }
       }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        //Open Account Information
        binding.accountCircle.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.START)
        }
    }

    //Check Update Fun
    private fun checkUpdate() {
        appUpdate?.appUpdateInfo?.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                appUpdate?.startUpdateFlowForResult(it,AppUpdateType.IMMEDIATE,this,REQUEST_CODE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inProgressUpdate()
    }
    //Progress Update
    fun inProgressUpdate(){
        appUpdate?.appUpdateInfo?.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                appUpdate?.startUpdateFlowForResult(it,AppUpdateType.IMMEDIATE,this,REQUEST_CODE)
            }
        }
    }

    //Rate Us Fun
    private fun rateUs() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+"com.denuvo.texteditorapp")))
        }catch (e : ActivityNotFoundException){
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+"com.denuvo.texteditorapp")))
        }

    }

    private fun scheduleNotification() {
        val intent = Intent(applicationContext, Notification::class.java)
        val title = "You have a task to do!"
        val message = "come look your task"
        intent.putExtra(titleExtra,title)
        intent.putExtra(messageExtra,message)
        pendingIntent = PendingIntent.getBroadcast(applicationContext, ++notificationId,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
        showAllert(time,title,message)
    }

    private fun cancelNotif(){
        val intent = Intent(applicationContext, Notification::class.java)
        pendingIntent = PendingIntent.getBroadcast(applicationContext, notificationId,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun showAllert(time: Long, title: CharSequence?, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(applicationContext)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)
        AlertDialog.Builder(this)
            .setTitle("Notification Schedule")
            .setMessage("Title" + title +
                    "\nMessage: " + message +
                    "\nAt: "+ dateFormat.format(date) +
                    " " + timeFormat.format(date))
            .setPositiveButton("Oke"){_,_->}
            .show()
    }

    private fun getTime(): Long {
        val minute = bottomsheetDialog.view!!.findViewById<TimePicker>(R.id.time_picker).minute
        val hour= bottomsheetDialog.view!!.findViewById<TimePicker>(R.id.time_picker).hour
        val day = bottomsheetDialog.view!!.findViewById<DatePicker>(R.id.date_picker).dayOfMonth
        val month = bottomsheetDialog.view!!.findViewById<DatePicker>(R.id.date_picker).month
        val year = bottomsheetDialog.view!!.findViewById<DatePicker>(R.id.date_picker).year

        val calender = Calendar.getInstance()
        calender.set(year,month,day,hour,minute)
        return calender.timeInMillis
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notif Channel"
            val desc = "A desc of Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId,name,importance)
            channel.description = desc
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveTask(myList: ArrayList<TaskData>, persentase: Float) {
        val sp : SharedPreferences = this.getSharedPreferences("preferences",Context.MODE_PRIVATE)
        val editor = sp.edit()
        val gson = Gson()
        val json = gson.toJson(myList)
        editor.putString("mylistTask",json)
        editor.putFloat("valuee",persentase)
        editor.apply()
    }

    private fun loadData() {
        val sp : SharedPreferences = this.getSharedPreferences("preferences",Context.MODE_PRIVATE)
        val gson : Gson = Gson()
        val json = sp.getString("mylistTask",null)
        var persentasee = sp.getFloat("valuee",100f)
        val type = object : TypeToken<ArrayList<TaskData>>() {}.type
        if (json == null) {
            myList = ArrayList()
            persentasee = 100f
            binding.circularBar.apply {
                setProgressWithAnimation(persentase,2000)
            }
        } else{
            myList = gson.fromJson(json,type)
            backUpList.addAll(myList)
            binding.gifMan.visibility = View.INVISIBLE
            persentase = persentasee
            binding.circularBar.apply {
                setProgressWithAnimation(persentase,2000)
                Log.d("value","$persentase")
            }

        }
    }

    private fun deleteData(myList: ArrayList<TaskData>, position : Int){
        val sp : SharedPreferences = this.getSharedPreferences("preferences",Context.MODE_PRIVATE)
        val editor = sp.edit()
        myList.removeAt(position)
        val gson = Gson()
        val json = gson.toJson(myList)
        editor.putString("mylistTask",json)
        editor.apply()
        if (myList.size == 0) {
            binding.gifMan.visibility = View.VISIBLE
        }
    }
    //Logout Fun
    private fun logout() {
        GoogleSignIn.getClient(this,GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
        mAuth.signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}


