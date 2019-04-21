package com.example.chatbot;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    public static ArrayList<DialogueLine> dialogue; /* Chat history data up to 100 lines of dialogue. */
    public TextView dialogueView; /* The space showing chat history. */
    public ImageView character;   /* The space showing the bot character. */
    public int algo=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initializing chat history data. */
        dialogue = new ArrayList<>(100);
        addLine("Output", "Halo, ada yang bisa dibantu?");

        /* Defining modifiable objects by ID. */
        character = findViewById(R.id.imageView);
        dialogueView = findViewById(R.id.dialogueView);
        dialogueView.setMovementMethod(new ScrollingMovementMethod());

        /* Modify objects. */
        changeExpression("greet");
        printDialogue();
    }

    /* Method to hide keyboard. (Used after user have input text.)*/
    public static void hideKeyboard( Activity activity ) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService( Context.INPUT_METHOD_SERVICE );
        View f = activity.getCurrentFocus();
        if( null != f && null != f.getWindowToken() && EditText.class.isAssignableFrom( f.getClass() ) )
            imm.hideSoftInputFromWindow( f.getWindowToken(), 0 );
        else
            activity.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
    }

    /* Clears the chat history space and rewrite chat history data. */
    public void printDialogue() {
        dialogueView.setText("");
        String buffer = "";
        for (int i = 0; i < dialogue.size(); i++){
            if (dialogue.get(i).getAuthor() == "Bot") {
                buffer = buffer + dialogueView.getText() + "<b>Miki</b>: " + dialogue.get(i).getMessage() + "<br/>";
            } else if (dialogue.get(i).getAuthor() == "User") {
                buffer = buffer + dialogueView.getText() + "<b>Anda</b>: " + dialogue.get(i).getMessage() + "<br/>";
            }
        }
        dialogueView.setText(Html.fromHtml(buffer));
    }

    /* Adds a line of dialogue to the chat history.
    *  If full, erases the earliest line first.
    *  DIALOGUE LINES CAN BE FORMATTED AS HTML TEXT. Use <br/> instead of '\n' for line break. */
    public void addLine(String type, String s) {
        if (dialogue.size() == 100) {
            dialogue.remove(0);
        }

        if (type == "Output") {
            dialogue.add(new DialogueLine("Bot", s));
        } else if (type == "Input") {
            dialogue.add(new DialogueLine("User", s));
        }
    }

    /* Changes the expression of the bot.
    *   1. Greet = initial expression
    *   2. Process = sort of a loading expression while question is processed
    *   3. Answer = expression when presenting answer to question
    *   4. Clarify = expression when asking for user clarification ('apakah maksud Anda...?') */
    public void changeExpression(String type) {
        if (type == "greet") {
            character.setImageResource(R.drawable.neutral);
        } else if (type == "process") {
            character.setImageResource(R.drawable.process);
        } else if (type == "answer") {
            character.setImageResource(R.drawable.answer);
        } else if (type == "clarify") {
            character.setImageResource(R.drawable.neutral);
        } else if (type == "surrender") {
            character.setImageResource(R.drawable.sad);
        }
    }

    /* Reads text input and processes it. */
    public void processInput(View view) {
        /* Reading input from field. */
        EditText editText = findViewById(R.id.input);
        String newInput = editText.getText().toString();


        /* Adding input to the chat history. */

        if(newInput.length()==0){
            return;
        }
        else if(newInput.equals("algo1")){
            algo = 1;
            addLine("Output", "Algoritma yang digunakan : KMP");
            printDialogue();
            changeExpression("answer");
            hideKeyboard(this);
            return;
        }
        else if(newInput.equals("algo2")){
            algo = 2;
            addLine("Output", "Algoritma yang digunakan : BM");
            printDialogue();
            changeExpression("answer");
            hideKeyboard(this);
            return;
        }
        else if(newInput.equals("algo3")){
            algo = 3;
            addLine("Output", "Algoritma yang digunakan : Regex");
            printDialogue();
            changeExpression("answer");
            hideKeyboard(this);
            return;
        }
        printDialogue();

        /* Change expression, reset field to empty. */
        changeExpression("process");
        hideKeyboard(this);
        editText.setText("");

        // call method to process string

        /* Adding output to the chat history. */
        addLine("Input",newInput);
        StringMatcher sm = new StringMatcher(this);
        ArrayList<String> answer = sm.answerQuery(newInput,algo);
        if(answer.size() == 1) {
            addLine("Output", answer.get(0));
            if (answer.get(0) == sm.no_ans) {
                changeExpression("surrender");
            } else {
                changeExpression("answer");
            }
        }
        else{
            String result = "<html>Apakah maksud anda :<br>";
            for(String ans : answer){
                result += "&nbsp;&nbsp;&nbsp;&nbsp;"+sm.getQuestion(ans)+"?<br>";
            }
            result+="</html>";
            addLine("Output", result);
            changeExpression("answer");
        }
        printDialogue();

        /* Change expression to:
         *  - "answer" if answer found
         *  - "clarify" if there are several possible answers
         *  - "surrender" if no answer is found */

    }

}
