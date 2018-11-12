package ganesh.kotlin.tictactoe

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(),View.OnClickListener {


    //database instance
    private var database= FirebaseDatabase.getInstance()
    var myEmail:String?=null
    //var lockForPlay:Int=-1
    private lateinit var tabLayout:TableLayout
    private lateinit var parentLay: LinearLayout
    private var disabledButtonList = mutableSetOf<Button>()
    private var enabledButtonList = mutableSetOf<Button>()
    //private val btn_IDs= listOf(R.id.bu1,R.id.bu2,R.id.bu3,R.id.bu4,R.id.bu5,R.id.bu6,R.id.bu7,R.id.bu8,R.id.bu9)
    private var youAre:String=""
    private lateinit var btnRematch:Button
    private lateinit var buRequest: Button
    private lateinit var buAccept: Button
    private lateinit var bu1:Button
    private lateinit var bu2:Button
    private lateinit var bu3:Button
    private lateinit var bu4:Button
    private lateinit var bu5:Button
    private lateinit var bu6:Button
    private lateinit var bu7:Button
    private lateinit var bu8:Button
    private lateinit var bu9:Button

    private var mFirebaseAnalytics: com.google.firebase.analytics.FirebaseAnalytics?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        mFirebaseAnalytics= FirebaseAnalytics.getInstance(this)

        //bind views
        tabLayout=findViewById(R.id.tabLayout)
        parentLay=findViewById(R.id.parentLay)
        btnRematch=findViewById(R.id.btnRematch)
        buRequest=findViewById(R.id.buRequest)
        buAccept=findViewById(R.id.buAccept)

        bu1=findViewById(R.id.bu1)
        bu2=findViewById(R.id.bu2)
        bu3=findViewById(R.id.bu3)
        bu4=findViewById(R.id.bu4)
        bu5=findViewById(R.id.bu5)
        bu6=findViewById(R.id.bu6)
        bu7=findViewById(R.id.bu7)
        bu8=findViewById(R.id.bu8)
        bu9=findViewById(R.id.bu9)

        //set listeners
        bu1.setOnClickListener(this)
        bu2.setOnClickListener(this)
        bu3.setOnClickListener(this)
        bu4.setOnClickListener(this)
        bu5.setOnClickListener(this)
        bu6.setOnClickListener(this)
        bu7.setOnClickListener(this)
        bu8.setOnClickListener(this)
        bu9.setOnClickListener(this)

        var b:Bundle=intent.extras
        myEmail=b.getString("email")
        txtPlayerName.text="Your are : $myEmail"
        IncommingCalls()//initial user connection


        //initial enabled buttons
        enabledButtonList.add(bu1)
        enabledButtonList.add(bu2)
        enabledButtonList.add(bu3)
        enabledButtonList.add(bu4)
        enabledButtonList.add(bu5)
        enabledButtonList.add(bu6)
        enabledButtonList.add(bu7)
        enabledButtonList.add(bu8)
        enabledButtonList.add(bu9)


        btnRematch.setOnClickListener(View.OnClickListener {
            //for rematch
            clear()
        })

        buRequest.setOnClickListener(View.OnClickListener {
            var userDemail=etEmail.text.toString()

            myRef.child("Users").child(SplitString(userDemail)).child("Request").push().setValue(myEmail)


            PlayerOnline(SplitString(myEmail!!)+ SplitString(userDemail)) // player1name+player2name
            PlayerSymbol="X"
            if (youAre.isBlank())
                youAre="O"//define your symbol
        })

        buAccept.setOnClickListener(View.OnClickListener {
            var userDemail=etEmail.text.toString()
            myRef.child("Users").child( SplitString(userDemail)).child("Request").push().setValue(myEmail)


            PlayerOnline(SplitString(userDemail)+SplitString(myEmail!!)) //player1name+player2name
            PlayerSymbol="O"
            if (youAre.isBlank())
                youAre="X"
        })
    }


    private var myRef=database.reference


    override fun onClick(v: View?) {
        val buSelected= v as android.widget.Button
        var cellID=0

        when(buSelected.id){
            ganesh.kotlin.tictactoe.R.id.bu1-> cellID=1
            ganesh.kotlin.tictactoe.R.id.bu2-> cellID=2
            ganesh.kotlin.tictactoe.R.id.bu3-> cellID=3
            ganesh.kotlin.tictactoe.R.id.bu4-> cellID=4
            ganesh.kotlin.tictactoe.R.id.bu5-> cellID=5
            ganesh.kotlin.tictactoe.R.id.bu6-> cellID=6
            ganesh.kotlin.tictactoe.R.id.bu7-> cellID=7
            ganesh.kotlin.tictactoe.R.id.bu8-> cellID=8
            ganesh.kotlin.tictactoe.R.id.bu9-> cellID=9

        }

        try {
            if (sessionID!=null) {
                myRef.child("PlayerOnline").child(sessionID!!).child(cellID.toString()).setValue(myEmail)
                myRef.child("PlayerOnline").child(sessionID!!).child("lastClicked").setValue(myEmail)
            }
            else
                Toast.makeText(this,"Please connect to a player before start this game",Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
        }
    }

    var player1= java.util.ArrayList<Int>()
    var player2= java.util.ArrayList<Int>()
    var ActivePlayer=1

    fun PlayGame(cellID:Int,buSelected: Button){

        if(ActivePlayer==1){
            buSelected.text="X"
            buSelected.setBackgroundResource(R.color.blue)
            player1.add(cellID)
            ActivePlayer=2

        }else if (ActivePlayer==2){
            buSelected.text="O"
            buSelected.setBackgroundResource(R.color.darkgreen)
            player2.add(cellID)
            ActivePlayer=1
        }


        buSelected.isEnabled=false
        buSelected.isClickable=false

        enabledButtonList.remove(buSelected)
        disabledButtonList.add(buSelected)

        if (winer!=3)
            CheckWiner()
    }

    var winer=-1

    fun  CheckWiner(){


        if (winer==3)
            return

        // row 1
        if(player1.contains(1) && player1.contains(2) && player1.contains(3)){
            winer=1
        }
        if(player2.contains(1) && player2.contains(2) && player2.contains(3)){
            winer=2
        }


        // row 2
        if(player1.contains(4) && player1.contains(5) && player1.contains(6)){
            winer=1
        }
        if(player2.contains(4) && player2.contains(5) && player2.contains(6)){
            winer=2
        }




        // row 3
        if(player1.contains(7) && player1.contains(8) && player1.contains(9)){
            winer=1
        }
        if(player2.contains(7) && player2.contains(8) && player2.contains(9)){
            winer=2
        }



        // col 1
        if(player1.contains(1) && player1.contains(4) && player1.contains(7)){
            winer=1
        }
        if(player2.contains(1) && player2.contains(4) && player2.contains(7)){
            winer=2
        }



        // col 2
        if(player1.contains(2) && player1.contains(5) && player1.contains(8)){
            winer=1
        }
        if(player2.contains(2) && player2.contains(5) && player2.contains(8)){
            winer=2
        }


        // col 3
        if(player1.contains(3) && player1.contains(6) && player1.contains(9)){
            winer=1
        }
        if(player2.contains(3) && player2.contains(6) && player2.contains(9)){
            winer=2
        }

        // cross 1
        if(player1.contains(1) && player1.contains(5) && player1.contains(9)){
            winer=1
        }
        if(player2.contains(1) && player2.contains(5) && player2.contains(9)){
            winer=2
        }

        // cross 1
        if(player1.contains(3) && player1.contains(5) && player1.contains(7)){
            winer=1
        }
        if(player2.contains(3) && player2.contains(5) && player2.contains(7)){
            winer=2
        }


        if( winer != -1){

            if (winer==1){
                if (youAre==="X")
                    android.widget.Toast.makeText(this," You won the game", android.widget.Toast.LENGTH_LONG).show()
                else
                    android.widget.Toast.makeText(this," You lost the game", android.widget.Toast.LENGTH_LONG).show()

                winer=3
                for (btn in enabledButtonList){
                    btn.isEnabled=false
                    btn.isClickable=false
                }

               btnRematch.visibility=View.VISIBLE

            }else if (winer==2){

                if (youAre==="O")
                    android.widget.Toast.makeText(this," You won the game", android.widget.Toast.LENGTH_LONG).show()
                else
                    android.widget.Toast.makeText(this," You lost the game", android.widget.Toast.LENGTH_LONG).show()
                winer=3
                for (btn in enabledButtonList){
                    btn.isEnabled=false
                    btn.isClickable=false
                }
                btnRematch.visibility=View.VISIBLE

            }

        }else if ((player1.size+player2.size)==9 || disabledButtonList.size==9){
            winer=3
            android.widget.Toast.makeText(this," Match Draw", android.widget.Toast.LENGTH_LONG).show()
            for (btn in enabledButtonList){
                btn.isEnabled=false
                btn.isClickable=false
            }
            btnRematch.visibility=View.VISIBLE

        }

    }


    fun AutoPlay(cellID:Int){



        var buSelect:Button?
        when(cellID){
            1-> buSelect=bu1
            2-> buSelect=bu2
            3-> buSelect=bu3
            4-> buSelect=bu4
            5-> buSelect=bu5
            6-> buSelect=bu6
            7-> buSelect=bu7
            8-> buSelect=bu8
            9-> buSelect=bu9
            else->{
                buSelect=bu1
            }
        }

        PlayGame(cellID,buSelect)

    }

    var sessionID:String?=null
    var PlayerSymbol:String?=null
    fun PlayerOnline(sessionID:String){
        this.sessionID=sessionID
        myRef.child("PlayerOnline").removeValue()
        myRef.child("PlayerOnline").child(sessionID)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try{
                            player1.clear()
                            player2.clear()

                            if (dataSnapshot.value==null){
                                clear()
                            }

                            var td=HashMap<Int,String>()
                            for (postSnapshot in dataSnapshot.children) {
                                val key=postSnapshot.key
                                val value=postSnapshot.value.toString()
                                if (key != "lastClicked")
                                    td.put(key!!.toInt(),value)
                                else
                                    if (key == "lastClicked"){
                                        if (value == myEmail) {
                                            for (btn in enabledButtonList){
                                                btn.isClickable=false
                                                btn.isEnabled=false
                                            }
                                        }
                                        else{
                                            for (btn in enabledButtonList){
                                                btn.isClickable=true
                                                btn.isEnabled=true
                                            }
                                        }

                                    }
                            }


                            //val td=dataSnapshot.value as HashMap<String,Any>
                            if(true){

                                var value:String
                                for (key in td.keys){
                                    value= td[key] as String

                                    if(value!= myEmail){
                                        ActivePlayer= if(PlayerSymbol==="X") 1 else 2
                                    }else{
                                        ActivePlayer= if(PlayerSymbol==="X") 2 else 1
                                    }

                                    AutoPlay(key.toInt())


                                }

                            }

                        }catch (ex:Exception){
                            Log.d("Exception",ex.message)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })

    }



    var number=0
    fun IncommingCalls(){
        myRef.child("Users").child(SplitString(myEmail!!)).child("Request")
                .addValueEventListener(object:ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {



                        try{
                            val td=dataSnapshot!!.value as HashMap<String,Any>
                            if(td!=null){

                                var value:String
                                for (key in td.keys){
                                    value= td[key] as String
                                    etEmail.setText(value)
                                    txtPlayerName.text="Your ID : $myEmail   \nOpponent ID : $value"
                                    val notifyme=Notifications()
                                    notifyme.Notify(applicationContext,value + " want to play tic tac toe",number)
                                    number++
                                    myRef.child("Users").child(SplitString(myEmail!!)).child("Request").setValue(true)

                                    break

                                }

                            }

                        }catch (ex:Exception){}
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }

                })
    }




    fun  SplitString(str:String):String{
        var split=str.split("@")
        return split[0]
    }

    fun clear(){
        //reset values, clear buttons and enable all buttons
        winer=-1
        ActivePlayer=1
        player1.clear()
        player2.clear()
        btnRematch.visibility=View.GONE

        for(btn in enabledButtonList){
            btn.isEnabled=true
            btn.isClickable=true
            btn.text=""
            btn.setBackgroundResource(R.color.white)
        }

        for (btn in disabledButtonList){
            btn.isEnabled=true
            btn.isClickable=true
            btn.text=""
            btn.setBackgroundResource(R.color.white)
            enabledButtonList.add(btn)
        }
        disabledButtonList.clear()
        myRef.child("PlayerOnline").removeValue()

    }

}


