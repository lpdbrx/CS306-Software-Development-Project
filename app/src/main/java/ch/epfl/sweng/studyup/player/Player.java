package ch.epfl.sweng.studyup.player;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import ch.epfl.sweng.studyup.MainActivity;
import ch.epfl.sweng.studyup.firebase.Firestore;

import static ch.epfl.sweng.studyup.firebase.Firestore.userData;
import static ch.epfl.sweng.studyup.utils.Utils.*;

/**
 * Player
 *
 * Used to store the Player's state and informations.
 */
public class Player {
    private static final String TAG = Player.class.getSimpleName();
    private static Player instance = null;
    private int experience;
    private int level;
    private int currency;
    private String firstName;
    private String lastName;
    private String username;
    private boolean isTeacher;
    private int sciper;
    private int[] questionsCurr;
    private int[] questsCurr;
    private int[] questionsAcheived; //todo equivalent to answeredQuestions ??
    private int[] questsAcheived;
    private Map<String, Boolean> answeredQuestions;


    public static String room = "INN_3_26";

    /**
     * Constructor called before someone is login.
     */
    private Player() {
        experience = INITIAL_XP;
        currency = INITIAL_CURRENCY;
        level = INITIAL_LEVEL;
        sciper = INITIAL_SCIPER;
        firstName = INITIAL_FIRSTNAME;
        lastName = INITIAL_LASTNAME;
        username = INITIAL_USERNAME;
        answeredQuestions = INITIAL_ANSWERED_QUESTIONS;
    }

    public static Player get() {
        if (instance == null) {
            instance = new Player();
        }
        return instance;
    }

    public int getExperience() {
        return experience;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrency() {
        return currency;
    }

    /**
     * Method suppose that we can only gain experience.
     */
    private void updateLevel(Context context) {
        int newLevel = experience / XP_TO_LEVEL_UP + 1;

        if (newLevel - level > 0) {
            addCurrency((newLevel - level) * CURRENCY_PER_LEVEL, context);
            putUserData(FB_CURRENCY, currency);
            putUserData(FB_LEVEL, newLevel);
            Firestore.get().setUserData(FB_CURRENCY, currency);
            Firestore.get().setUserData(FB_LEVEL, newLevel);
            level = newLevel;
        }
    }

    public void addCurrency(int curr, Context context) {
        currency += curr;

        if(context instanceof MainActivity) {
            ((MainActivity) context).updateCurrDisplay();
        }

        putUserData(FB_CURRENCY, currency);
        Firestore.get().setUserData(FB_CURRENCY, currency);
    }

    public void addExperience(int xp, Context context) {
        experience += xp;
        updateLevel(context);

        if(context instanceof MainActivity) {
            ((MainActivity) context).updateXpAndLvlDisplay();
            Log.i("Check", "Context is "+context.toString()+" "+((MainActivity) context).getLocalClassName());
        }

        putUserData(FB_XP, experience);
        Firestore.get().setUserData(FB_XP, experience);
    }

    public double getLevelProgress() {
        return (experience % XP_TO_LEVEL_UP) * 1.0 / XP_TO_LEVEL_UP;
    }

    /**
     * Changes the Player to the basic state, right after constructor.
     */
    public void reset() {
        instance = new Player();
        instance.setSciper(INITIAL_SCIPER);
        instance.setFirstName(FB_FIRSTNAME);
        instance.setLastName(FB_LASTNAME);
        instance.setUserName(INITIAL_USERNAME);
        putUserData(FB_SCIPER, sciper);
        putUserData(FB_FIRSTNAME, firstName);
        putUserData(FB_LASTNAME, lastName);
        if(isTeacher)
            putUserData(FB_ROLE, FB_ROLES_T);
        else
            putUserData(FB_ROLE, FB_ROLES_S);
    }

    /**
     * Method used to save the state contained in the userData attribute of the class Firestore in
     * the class Player
     */
    public void updatePlayerData(Context context) throws NullPointerException{
        // int newExperience = Ints.checkedCast((Long) userData.get(FB_XP))
        // Keeping this in case we want to have number attribute and not strings
        try {
            experience = Integer.parseInt(userData.get(FB_XP).toString());
            currency = Integer.parseInt(userData.get(FB_CURRENCY).toString());
            firstName = userData.get(FB_FIRSTNAME).toString();
            lastName = userData.get(FB_LASTNAME).toString();
            sciper = Integer.parseInt(userData.get(FB_SCIPER).toString());
            username = userData.get(FB_USERNAME).toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        updateLevel(context);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;

        putUserData(FB_FIRSTNAME, firstName);
    }

    public void setUserName(String new_username) {
        username = new_username;
        putUserData(FB_USERNAME, username);
        Firestore.get().setUserData(FB_USERNAME, username);
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;

        putUserData(FB_LASTNAME, lastName);
    }

    public String getUserName() {
        return username;
    }

    public int getSciper() {
        //to remove later
        if (sciper < MIN_SCIPER || sciper > MAX_SCIPER) {
            return INITIAL_SCIPER;
        }
        return sciper;
    }

    public void setSciper(int sciper) {
        this.sciper = sciper;
        putUserData(FB_SCIPER, sciper);
    }

    public void setRole(boolean isTeacher) {
        this.isTeacher = isTeacher;
        if(isTeacher) {
            putUserData(FB_ROLE, FB_ROLES_T);
        } else {
            putUserData(FB_ROLE, FB_ROLES_S);
        }
    }

    /**
     * Add the questionID to answered questions field in Firebase, mapped with the value of the answer.
     */
    public void addAnsweredQuestion(String questionID, boolean isAnswerGood) {
        this.answeredQuestions.put(questionID, isAnswerGood);
        putUserData(FB_ANSWERED_QUESTIONS, answeredQuestions);
        Firestore.get().setUserData(FB_ANSWERED_QUESTIONS, answeredQuestions);
    }

    public Map<String, Boolean> getAnsweredQuestion() {
        return this.answeredQuestions;
    }


    public boolean getRole() {
        return isTeacher;
    }

    public String getCurrentRoom(){
        return room;
    }
}
