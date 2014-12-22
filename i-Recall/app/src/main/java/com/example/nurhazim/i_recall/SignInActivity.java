package com.example.nurhazim.i_recall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

import java.util.ArrayList;
import java.util.List;

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
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_WAITING_ROOM = 10002;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_sign_in);
        setSupportActionBar(toolbar);
    }

    private void showInvitationDialog(final Invitation invitation){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(invitation.getInviter().getDisplayName() + R.string.dialog_new_invitation + mSpinnerDeck.getSelectedItem().toString())
                .setTitle(R.string.dialog_title_invitation)
                .setPositiveButton(R.string.dialog_button_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                        roomConfigBuilder.setInvitationIdToAccept(invitation.getInvitationId());
                        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());

                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                })
                        .setNegativeButton(R.string.dialog_button_decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .build();
        if(!mInSignInFlow && !mExplicitSignOut){
            Log.v(LOG_TAG, "Connecting to Play Games");
            mSignInClicked = true;
            mInSignInFlow = true;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
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

        mSpinnerDeck = (Spinner) findViewById(R.id.spinner_deck);
        List<String> decks = Utility.GetArrayListOfDecks(this);
        final ArrayAdapter<String> spinnerDecksAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                decks
        );
        spinnerDecksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDeck.setAdapter(spinnerDecksAdapter);

        mInSignInFlow = false;
        mSignInClicked = false;

        mInvitationListener = new OnInvitationReceivedListener() {
            @Override
            public void onInvitationReceived(Invitation invitation) {
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
            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else if(requestCode == RC_WAITING_ROOM){
            if(resultCode == Activity.RESULT_OK){
                //TO-DO: start game
                Log.v(LOG_TAG, "Game begins");
                findViewById(R.id.sign_in_layout).setVisibility(View.GONE);
                findViewById(R.id.game_layout).setVisibility(View.VISIBLE);
                InitializeGame();
            }
            else if(resultCode == Activity. RESULT_CANCELED){
                Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else if(resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM){
                Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    private void InitializeGame(){

    }

    private RoomConfig.Builder makeBasicRoomConfigBuilder(){
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

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
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> strings) {

    }

    @Override
    public void onPeerLeft(Room room, List<String> strings) {
        if(!mPlaying){
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

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
        if(mPlaying){
            //TO-DO: stop game
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
        }
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
}
