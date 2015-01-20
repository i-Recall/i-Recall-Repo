package com.example.nurhazim.i_recall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by NurHazim on 21-Dec-14.
 */
//TO-DO: cancel button for game
public class SignInActivity extends ActionBarActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RoomUpdateListener,
        RoomStatusUpdateListener,
        RealTimeMessageReceivedListener{
    private static int RC_SIGN_IN = 9001;
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static int RC_WAITING_ROOM = 10002;
    private final static int REQUEST_LEADERBOARD = 9003;
    private final static int RC_INVITATION_INBOX = 10001;

    private final static String LEADERBOARD_ID = "CgkI2p-P95YNEAIQAQ";

    private final static String LOG_TAG = SignInActivity.class.getSimpleName();

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;

    private boolean mExplicitSignOut = false;
    private boolean mInSignInFlow = false;

    GoogleApiClient mGoogleApiClient;

    boolean mPlaying = false;
    final static int MIN_PLAYERS = 2;

    private static String mRoomId;

    private OnInvitationReceivedListener mInvitationListener;

    private Spinner mSpinnerDeck;
    private int mCurrentDeck;
    private List<String> mDeckList;
    private List<Card> mCardList;

    private NoSwipeViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private final String STRING_SHOOT = "shoot";
    private final String STRING_FINISHED = "finished";
    Boolean mShot = false;

    private FrameLayout mGameLayout;
    private LinearLayout mSignInLayout;
    private ImageView mAttackingBar;

    private ProgressDialog mProgressDialog;
    private int mCardsLeft = 0;
    private boolean mOpponentWaiting = false;
    private boolean mFinished = false;

    private int mPlayerScore = 0;
    private int mOpponentScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_sign_in);
        setSupportActionBar(toolbar);
        toolbar.setTitle("New Game");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_in_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_refresh_deck_list:
                if(mGoogleApiClient.isConnected()) {
                    FetchDeckListTask fetchDeckListTask = new FetchDeckListTask(this);
                    fetchDeckListTask.execute();
                    return true;
                }
                else{
                    Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInvitationDialog(final Invitation invitation){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(invitation.getInviter().getDisplayName() + " " + getResources().getString(R.string.dialog_new_invitation))
                .setTitle(R.string.dialog_title_invitation)
                .setPositiveButton(R.string.dialog_button_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                        roomConfigBuilder.setInvitationIdToAccept(invitation.getInvitationId());

                        mCurrentDeck = invitation.getVariant()-1;
                        Log.v(LOG_TAG, "The deck that will be played is " + mDeckList.get(mCurrentDeck));

                        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());

                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                })
                        .setNegativeButton(R.string.dialog_button_decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Games.RealTimeMultiplayer.declineInvitation(mGoogleApiClient, invitation.getInvitationId());
                    }
                })
                .create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mInSignInFlow && !mExplicitSignOut){
            Log.v(LOG_TAG, "Connecting to Play Games");
            mSignInClicked = true;
//            mInSignInFlow = true;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(LOG_TAG, "Connected");
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        findViewById(R.id.spinner_deck).setVisibility(View.VISIBLE);
        Button buttonStart = (Button) findViewById(R.id.button_start_game);
        buttonStart.setVisibility(View.VISIBLE);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 1);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
            }
        });

        FetchDeckListTask fetchDeckListTask = new FetchDeckListTask(this);
        fetchDeckListTask.execute();

        mInSignInFlow = false;
        mSignInClicked = false;

        mInvitationListener = new OnInvitationReceivedListener() {
            @Override
            public void onInvitationReceived(Invitation invitation) {
                finishActivity(RC_SELECT_PLAYERS);
                showInvitationDialog(invitation);
            }

            @Override
            public void onInvitationRemoved(String s) {

            }
        };
        Games.Invitations.registerInvitationListener(mGoogleApiClient, mInvitationListener);

        if(bundle != null){
            Invitation inv = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if(inv != null){
                RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());

                mCurrentDeck = inv.getVariant()-1;
                Log.v(LOG_TAG, "The deck that will be played is " + mDeckList.get(mCurrentDeck));

                Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_SELECT_PLAYERS){
            if(resultCode != Activity.RESULT_OK){
                return;
            }

            Bundle extras = data.getExtras();
            final ArrayList<String> invitees =
                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if(minAutoMatchPlayers > 0){
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            }
            else {
                autoMatchCriteria = null;
            }

            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();

            roomConfigBuilder.addPlayersToInvite(invitees);
            if(autoMatchCriteria != null){
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            mCurrentDeck = mSpinnerDeck.getSelectedItemPosition();
            roomConfigBuilder.setVariant(mSpinnerDeck.getSelectedItemPosition()+1);
            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Preparing waiting lobby");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        else if(requestCode == RC_WAITING_ROOM){
            if(resultCode == Activity.RESULT_OK){
                //TO-DO: start game
                Log.v(LOG_TAG, "Game begins");
                mSignInLayout = (LinearLayout) findViewById(R.id.sign_in_layout);
                mSignInLayout.setVisibility(View.GONE);
                mGameLayout = (FrameLayout) findViewById(R.id.game_layout);
                mGameLayout.setVisibility(View.VISIBLE);

                InitializeGame();
            }
            else if(resultCode == Activity. RESULT_CANCELED){
                Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else if(resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM){
                Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
        else if(requestCode == GamesActivityResultCodes.RESULT_LEFT_ROOM){
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else if(requestCode == RC_SIGN_IN){
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode,
                        R.string.signin_error);
            }
        }
    }

    private void AttackOpponent(){
        String shootMessage = STRING_SHOOT;
        byte[] message = shootMessage.getBytes();
        Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(
                mGoogleApiClient,
                message,
                mRoomId
        );
        mShot = true;
        Log.v(LOG_TAG, "Attack the other player");
        Animation exit = AnimationUtils.loadAnimation(this, R.anim.bar_exit);
        exit.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAttackingBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mAttackingBar.startAnimation(exit);
        mPlayerScore++;
    }

    private void ResetGame(){
        mPlaying = false;
        mPager.setCurrentItem(0);
        mShot = false;
        mAttackingBar.clearAnimation();
        mAttackingBar.setVisibility(View.INVISIBLE);
        mCardsLeft = 0;
        mOpponentWaiting = false;
        mFinished = false;
        mPlayerScore = 0;
        mOpponentScore = 0;
    }

    private void WrapUpGame(){
        byte[] message = (STRING_FINISHED + ":" + mPlayerScore).getBytes();
        Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(
                mGoogleApiClient,
                message,
                mRoomId
        );
        mFinished = true;
        if(mOpponentWaiting){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            String dialogMessage;
            if(mPlayerScore > mOpponentScore){
                dialogMessage = getResources().getString(R.string.dialog_message_winner);
            }
            else{
                dialogMessage = getResources().getString(R.string.dialog_message_loser);
            }
            Log.v("OnRealTimeMessageReceived", "My score: " + mPlayerScore + ", Opponent score: " + mOpponentScore);

            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            builder.setMessage(dialogMessage)
                    .setTitle(R.string.dialog_title_game_end)
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mGameLayout.setVisibility(View.GONE);
                            mSignInLayout.setVisibility(View.VISIBLE);
                            Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_ID, mPlayerScore);
                            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, LEADERBOARD_ID), REQUEST_LEADERBOARD);
                            ResetGame();
                        }
                    })
                    .create().show();
        }
        else{
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Opponent has " + mCardsLeft + " cards left.");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    public void QuitGame(){
        Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mGameLayout.setVisibility(View.GONE);
        mSignInLayout.setVisibility(View.VISIBLE);
        Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_ID, mPlayerScore);
        ResetGame();
    }

    private void InitializeGame(){
        findViewById(R.id.button_quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuitGame();
            }
        });
        mPlaying = true;

        mAttackingBar = (ImageView) findViewById(R.id.long_bar);

        mPager = (NoSwipeViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), mCardList);
        mPager.setAdapter(mPagerAdapter);

        Button btnTrue = (Button) findViewById(R.id.button_true);
        Button btnFalse = (Button) findViewById(R.id.button_false);

        btnTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mShot && ((ScreenSlidePagerAdapter)mPagerAdapter).getAnswer(mPager.getCurrentItem()) == true){
                    AttackOpponent();
                }
                if(isAtLastItem(mPager)){
                    WrapUpGame();
                }
                else {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
                if(mOpponentWaiting){
                    int cardsLeft = mCardList.size() - mPager.getCurrentItem();
                    byte[] message = String.valueOf(cardsLeft).getBytes();
                    Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(
                            mGoogleApiClient,
                            message,
                            mRoomId
                    );
                }
            }
        });

        btnFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mShot && ((ScreenSlidePagerAdapter)mPagerAdapter).getAnswer(mPager.getCurrentItem()) == false){
                    AttackOpponent();
                }
                if(isAtLastItem(mPager)){
                    WrapUpGame();
                }
                else {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    mShot = false;
                }
                if(mOpponentWaiting){
                    int cardsLeft = mCardList.size() - mPager.getCurrentItem();
                    byte[] message = String.valueOf(cardsLeft).getBytes();
                    Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(
                            mGoogleApiClient,
                            message,
                            mRoomId
                    );
                }
            }
        });
    }

    private boolean isAtLastItem(ViewPager viewPager){
        return viewPager.getCurrentItem() == mCardList.size() - 1;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<Card> mCardList;
        private List<Integer> shuffledDescriptions;
        private List<Boolean> answers = new ArrayList<Boolean>();
        public Map<Integer, SingleSideCardFragment> mPageReferenceMap;

        public ScreenSlidePagerAdapter(FragmentManager fm, List<Card> cardList){
            super(fm);
            shuffledDescriptions = shuffleList(cardList.size());
            mCardList = cardList;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putString(StudyActivity.TERM_KEY, mCardList.get(i).getTerm());
            bundle.putString(StudyActivity.DESCRIPTION_KEY, mCardList.get(shuffledDescriptions.get(i)).getDescription());

            if(i == shuffledDescriptions.get(i)){
                answers.add(true);
            }
            else{
                answers.add(false);
            }

            SingleSideCardFragment singleSideCardFragment = new SingleSideCardFragment();
            singleSideCardFragment.setArguments(bundle);

            return singleSideCardFragment;
        }

        @Override
        public int getCount() {
            return mCardList.size();
        }

        private List<Integer> shuffleList(int totalCards){
            List<Integer> dataList = new ArrayList<Integer>();
            for (int i = 0; i < totalCards; i++) {
                dataList.add(i);
            }
            Collections.shuffle(dataList);
            return dataList;
        }

        public boolean getAnswer(int currentCard){
            return answers.get(currentCard);
        }
    }

    private RoomConfig.Builder makeBasicRoomConfigBuilder(){
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        try{
            String message = new String(realTimeMessage.getMessageData(), "UTF-8");
            String messageParams[] = message.split(":");

            switch(messageParams[0]){
                case STRING_SHOOT:
                    Log.v(LOG_TAG, "I'm under attack!");
                    mAttackingBar.setVisibility(View.VISIBLE);
                    mAttackingBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bar_attack));
                    break;
                case STRING_FINISHED:
                    Log.v(LOG_TAG, "Opponent is finished");
                    if(mFinished){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        String dialogMessage;
                        if(mPlayerScore >= Integer.parseInt(messageParams[1])){
                            dialogMessage = getResources().getString(R.string.dialog_message_winner);
                        }
                        else{
                            dialogMessage = getResources().getString(R.string.dialog_message_loser);
                        }
                        Log.v("OnRealTimeMessageReceived", "My score: " + mPlayerScore + ", Opponent score: " + messageParams[1]);

                        Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        builder.setMessage(dialogMessage)
                                .setTitle(R.string.dialog_title_game_end)
                                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mGameLayout.setVisibility(View.GONE);
                                        mSignInLayout.setVisibility(View.VISIBLE);
                                        Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_ID, mPlayerScore);
                                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, LEADERBOARD_ID), REQUEST_LEADERBOARD);
                                        ResetGame();
                                    }
                                })
                                .create().show();
                        if(mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                    }
                    else{
                        mOpponentScore = Integer.parseInt(messageParams[1]);
                        int cardsLeft = mCardList.size() - mPager.getCurrentItem();
                        byte[] messageQuantity = String.valueOf(cardsLeft).getBytes();
                        Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(
                                mGoogleApiClient,
                                messageQuantity,
                                mRoomId
                        );
                        mOpponentWaiting = true;
                    }
                    break;
                default:
                    mCardsLeft = Integer.parseInt(message);
                    mProgressDialog.setMessage("Opponent has " + mCardsLeft + " cards left.");
            }
        } catch(UnsupportedEncodingException e){
            Log.e(LOG_TAG, "Error: " + e);
        }
    }

    @Override
    public void onRoomConnecting(Room room) {

    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> strings) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> strings) {
        if(!mPlaying){
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> strings) {
        FetchCardsTask fetchCardsTask = new FetchCardsTask(this);
        fetchCardsTask.execute(mCurrentDeck);
        mRoomId = room.getRoomId();
    }

    @Override
    public void onPeerLeft(Room room, List<String> strings) {
        if(!mPlaying){
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else{
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//            builder.setMessage(getResources().getString(R.string.dialog_message_disconnected))
//                    .setTitle(R.string.dialog_error)
//                    .setNeutralButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    })
//                    .create().show();
        }
    }

    @Override
    public void onConnectedToRoom(Room room) {
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPeersConnected(Room room, List<String> strings) {
        if(shouldStartGame(room)){
            //TO-DO: start game
            Log.v(LOG_TAG, "Peers connected!");
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> strings) {
        if(mPlaying && !mFinished){
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(getResources().getString(R.string.dialog_message_disconnected))
                    .setTitle(R.string.dialog_error)
                    .setNeutralButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create().show();
            QuitGame();
        }
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if(statusCode != GamesStatusCodes.STATUS_OK){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //TO-DO: show eror message, return to main screen
            Log.v(LOG_TAG, "Room wasn't created");
        }
        else{
            mRoomId = room.getRoomId();
            Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
            startActivityForResult(intent, RC_WAITING_ROOM);
            if(mProgressDialog.isShowing())mProgressDialog.dismiss();
        }
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if(statusCode != GamesStatusCodes.STATUS_OK){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //TO-DO: show eror message, return to main screen
            Log.v(LOG_TAG, "Join room problem");
        }
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(intent, RC_WAITING_ROOM);
        if(mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    @Override
    public void onLeftRoom(int i, String s) {

    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if(statusCode != GamesStatusCodes.STATUS_OK){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //TO-DO: show eror message, return to main screen
            Log.v(LOG_TAG, "Connect room problem");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.sign_in_button:
                mSignInClicked = true;
                mInSignInFlow = true;
                mGoogleApiClient.connect();
                break;
            case R.id.sign_out_button:
                mSignInClicked = false;
                mExplicitSignOut = true;
                if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
                    Games.signOut(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }

                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
                findViewById(R.id.spinner_deck).setVisibility(View.GONE);
                findViewById(R.id.button_start_game).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(mResolvingConnectionFailure){
            return;
        }

        if(mSignInClicked || mAutoStartSignInflow){
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            if(!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getResources().getString(R.string.sign_in_failed))){
                mResolvingConnectionFailure = false;
            }
            Log.e(LOG_TAG, "There was a problem with signing in.");
        }
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        findViewById(R.id.spinner_deck).setVisibility(View.GONE);
        findViewById(R.id.button_start_game).setVisibility(View.GONE);
    }

    boolean shouldStartGame(Room room){
        int connectedPlayers = 0;
        for(Participant p : room.getParticipants()){
            if(p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    boolean shouldCancelGame(Room room){
        for(Participant p : room.getParticipants()){
            if(p.getStatus() == Participant.STATUS_DECLINED){
                return true;
            }
        }
        return false;
    }

    private class FetchDeckListTask extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = FetchDeckListTask.class.getSimpleName();

        private Context mContext;
        private ProgressDialog dialog;

        public FetchDeckListTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(context);
        }

        @SuppressWarnings("unchecked")
        private void getDeckDataFromJson(String deckJsonStr) throws JSONException {

            final String OWM_DECKS = "decks";
            final String OWM_DECK_NAME = "deck_name";

            List deckList = new ArrayList<String>();

            JSONObject deckJson = new JSONObject(deckJsonStr);
            JSONArray deckArray = deckJson.getJSONArray(OWM_DECKS);
            for (int i = 0; i < deckArray.length(); i++) {
                String deckName;
                JSONObject singleDeck = deckArray.getJSONObject(i);
                deckName = singleDeck.getString(OWM_DECK_NAME);
                deckList.add(deckName);
            }
            mDeckList = deckList;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Loading available decks.");
            dialog.show();
        }

        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String decksJsonStr = null;

            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String BASE_URL = sharedPreferences.getString(SettingsActivity.KEY_PREF_BASE_URL, "");
                final String DECK_PARAM = "deck";

                Uri.Builder b = Uri.parse(BASE_URL).buildUpon();
                b.path("/api/index.php");
                b.appendQueryParameter(DECK_PARAM, "*");
                String urli = b.build().toString();

                Log.v(LOG_TAG, "The Uri is " + urli);

                URL url = new URL(urli);

                Log.v(LOG_TAG, "The URL is " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(30000);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                decksJsonStr = buffer.toString();
            } catch(SocketTimeoutException e){
                dialog.dismiss();
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(mContext, "Connection failed. Please retry", Toast.LENGTH_SHORT).show();
                    }
                });
                this.cancel(true);
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally{
                if (urlConnection != null) {
                    try {
                        if(reader != null){
                            reader.close();
                        }
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                getDeckDataFromJson(decksJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mSpinnerDeck = (Spinner) findViewById(R.id.spinner_deck);
            final ArrayAdapter<String> spinnerDecksAdapter = new ArrayAdapter<String>(
                    mContext,
                    android.R.layout.simple_spinner_item,
                    mDeckList
            );
            spinnerDecksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinnerDeck.setAdapter(spinnerDecksAdapter);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private class FetchCardsTask extends AsyncTask<Integer, Void, Void> {
        private final String LOG_TAG = FetchDeckListTask.class.getSimpleName();

        private ProgressDialog dialog;
        private Context mContext;

        public FetchCardsTask(Context context) {
            dialog = new ProgressDialog(context);
            mContext = context;
        }

        @SuppressWarnings("unchecked")
        private void getCardDataFromJson(String cardJsonStr) throws JSONException {
            final String OWM_CARDS = "cards";
            final String OWM_CARD_ID = "card_id";
            final String OWM_CARD_TERM = "card_term";
            final String OWM_CARD_DESCRIPTION = "card_description";

            List cardList = new ArrayList<Card>();

            JSONObject cardsJson = new JSONObject(cardJsonStr);
            JSONArray cardsArray = cardsJson.getJSONArray(OWM_CARDS);
            for (int i = 0; i < cardsArray.length(); i++) {
                int cardId = 0;
                String cardTerm = "";
                String cardDescription = "";

                JSONObject card = cardsArray.getJSONObject(i);
                cardId = card.getInt(OWM_CARD_ID);
                cardTerm = card.getString(OWM_CARD_TERM);
                cardDescription = card.getString(OWM_CARD_DESCRIPTION);

                cardList.add(new Card(cardId, cardTerm, cardDescription));
            }
            mCardList = cardList;
            for(int i = 0; i < mCardList.size(); i++){
                Log.v(LOG_TAG, "Term is " + mCardList.get(i).getTerm() + ": " + mCardList.get(i).getDescription());
            }
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Getting cards.");
            dialog.show();

            Log.v(LOG_TAG, "Task about to run");
        }

        protected Void doInBackground(Integer... params) {
            Log.v(LOG_TAG, "Task running");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String CardsJsonStr = null;

            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String BASE_URL = sharedPreferences.getString(SettingsActivity.KEY_PREF_BASE_URL, "");
                final String DECK_PARAM = "deck";

                Uri.Builder b = Uri.parse(BASE_URL).buildUpon();
                b.path("/api/index.php");
                String queryParameter = mDeckList.get(params[0]);
                b.appendQueryParameter(DECK_PARAM, queryParameter);
                String builtUri = b.build().toString();

                URL url = new URL(builtUri);

                Log.v(LOG_TAG, "The URL is " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                CardsJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                getCardDataFromJson(CardsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.v(LOG_TAG, "Task finished");
        }
    }

}


