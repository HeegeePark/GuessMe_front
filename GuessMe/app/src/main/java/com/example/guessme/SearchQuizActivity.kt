package com.example.guessme

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guessme.api.Json
import com.example.guessme.api.Okhttp
import com.example.guessme.data.Quiz
import kotlinx.android.synthetic.main.activity_search_quiz.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class SearchQuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_quiz)
        SearchQuiz_Control().edit_init()
    }


    inner class SearchQuiz_Control() {

        fun edit_init(){
            sq_et_nickname.addTextChangedListener(EditListener())
        }

        fun edit_check() : Boolean{

            if (sq_et_nickname.text.isNullOrEmpty()) {
                Toast.makeText(applicationContext, "닉네임을 입력해주세요.", Toast.LENGTH_LONG).show()
                return false
            }

            return true

        }

        fun GET_QUIZ(nickname : String) {
            val url = getString(R.string.server_url) + "/quizzes/" + nickname
            asynctask().execute(url)
        }
    }

    inner class asynctask : AsyncTask<String, Void, String>(){

        var state : Int = 0

        override fun doInBackground(vararg params: String): String {
            val url = params[0]
            return Okhttp(applicationContext).GET(url)
        }


        override fun onPostExecute(response: String?) {
            //넘어온 값이 없을 때 로그 찍고 리턴
            if(response.isNullOrEmpty()) {
                Toast.makeText(applicationContext,"Search_activity", Toast.LENGTH_SHORT).show()
                Log.d("Search_Activity", "null in")
                return
            }
            Log.d("Search_Activity",response)
            if(!Json().isJson(response)){
                Log.d("퀴즈 입력 통신 에러", response)
                Toast.makeText(applicationContext,"네트워크 통신 오류",Toast.LENGTH_SHORT).show()
                return
            }

            val jsonObj = JSONObject(response) // 에러여도 여기까진 가능

            try {
                val jsonObj_embedded = jsonObj.getJSONObject("_embedded")
                val jsonQuizAry = jsonObj_embedded.getJSONArray("quizList")
                val intent = Intent(this@SearchQuizActivity, SolveQuizActivity::class.java)
                intent.putExtra("nickname", sq_et_nickname.text.toString()) //list를 넘겨주기 위해
                startActivity(intent)
            }catch (e:Exception){
                Toast.makeText(applicationContext, "존재하지 않는 닉네임 입니다.", Toast.LENGTH_SHORT).show()
            }



            Toast.makeText(applicationContext,"퀴즈를 찾아왔어요!",Toast.LENGTH_SHORT).show()

        }

        fun getStateInt():Int{
            return this.state
        }
    }

    fun SearchQuiz_Click_Listener(view : View){
        when(view.id){
            R.id.btn_search ->{
                if(SearchQuiz_Control().edit_check()) {
                    SearchQuiz_Control().GET_QUIZ(sq_et_nickname.text.toString())
                }
            }

            R.id.btn_mypage ->{
//                생성 이력이 있는 유저인지 제약해야 함
                val intent = Intent(this, CreateQuizActivity::class.java)
                startActivity(intent)
            }
        }
    }

    inner class EditListener : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if(!s.isNullOrEmpty())
                btn_search.isEnabled = !sq_et_nickname.text.isNullOrEmpty()
            else
                btn_search.isEnabled = false
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
    override fun onPause() {
        asynctask().cancel(true)
        super.onPause()
    }

}