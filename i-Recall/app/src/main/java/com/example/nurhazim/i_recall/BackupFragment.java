package com.example.nurhazim.i_recall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.nurhazim.i_recall.data.CardsDbHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class BackupFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final int PICK_BACKUP_FILE = 1;
    public static final int PICK_RESTORE_FILE = 2;
    public static final int UPLOAD_TO_GOOGLE_DRIVE = 3;
    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 4;

    private boolean googleDriveConnected = false;
    String backupFileName = "";

    GoogleApiClient mGoogleApiClient;

    public BackupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause(){
        // disconnect Google Api Client when this fragment pause
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    private void doUploadToGoogleDrive()
    {
        if (googleDriveConnected)
        {
            //application/x-sqlite3
            //Utility.BackupDatabaseToGoogleDrive(getActivity());
            Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(driveContentsCallback);
        }
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()){
                          Toast.makeText(getActivity(), "Error while trying to create new file content", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    //Perform I/O off the UI threads.
                    new Thread(){
                        @Override
                        public void run(){
                            try{
                                //write content to DriveContents
                                OutputStream outputStream = driveContents.getOutputStream();
                                File sd = Environment.getExternalStorageDirectory();
                                File data = Environment.getDataDirectory();

                                if(sd.canWrite()){
                                    //our current database file patch
                                    String currentDBPath = "//data//com.example.nurhazim.i_recall//databases//" + CardsDbHelper.DATABASE_NAME;
                                    backupFileName = "cards_" + Utility.getNowDateTimeStringNoSymbols() + ".db";
                                    File currentDB = new File(data, currentDBPath);

                                    //copy existing database file path to backup path
                                    if(currentDB.exists()){
                                        try{
                                            FileInputStream fis = new FileInputStream(currentDB);
                                            byte[] byteArray = new byte[(int) currentDB.length()];
                                            int readResult = fis.read(byteArray,0,byteArray.length);
                                            fis.close();
                                            if (readResult == currentDB.length()){
                                                Writer writer = new OutputStreamWriter(outputStream);
                                                outputStream.write(byteArray);
                                                writer.close();

                                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                                        .setTitle(backupFileName)
                                                        .setMimeType("application/x-sqlite3").build();

                                                // create a file on root folder
                                                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                                        .createFile(mGoogleApiClient, changeSet, driveContents)
                                                        .setResultCallback(fileCallback);
                                            }
                                            else{
                                                Toast.makeText(getActivity(), "Current Database read failed ", Toast.LENGTH_SHORT).show();
                                            }
                                        }catch (IOException e){
                                            Toast.makeText(getActivity(), "IOException " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }catch (Exception ex){
                                Toast.makeText(getActivity(), "Exception " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.start();
                }
            };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                    if (!driveFileResult.getStatus().isSuccess()){
                        Toast.makeText(getActivity(), "Error when trying to create the file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getActivity(), "Backup created with file name " + backupFileName + " and content ID: " + driveFileResult.getDriveFile().getDriveId(), Toast.LENGTH_LONG).show();
                }
            };

    private void connectToGoogleDrive()
    {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_backup, container, false);

        Button BackupDatabaseButton = (Button) rootView.findViewById(R.id.BackupDatabaseButton);
        Button RestoreDatabaseButton = (Button) rootView.findViewById(R.id.RestoreDatabaseButton);

        // Handles the BackupDatabaseButton button click
        BackupDatabaseButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Adrian: newly added for google Drive or SDCARD mode export
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_backup_mode)
                        .setSingleChoiceItems(R.array.backup_mode_choice_array, -1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                //Toast.makeText(getActivity(), "Backup mode choice is " + which, Toast.LENGTH_SHORT).show();
                                if (which == 0) // user choose the SDCARD backup mode
                                {
                                    Toast.makeText(getActivity(), "Please select a DIRECTORY to save your database backup", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(BackupFragment.this.getActivity(), FileDialog.class);
                                    intent.putExtra(FileDialog.START_PATH, "/sdcard");

                                    //can user select directories or not
                                    intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
                                    intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

                                    startActivityForResult(intent, PICK_BACKUP_FILE);
                                } else if (which == 1) // user choose the Google Drive backup mode
                                {
                                    connectToGoogleDrive();
                                    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
                                    if (resultCode == ConnectionResult.SUCCESS) {
                                        doUploadToGoogleDrive();
                                    } else // google service not available or not up to date
                                    {
                                        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                                            // display the error dialog to user
                                            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), UPLOAD_TO_GOOGLE_DRIVE);
                                            if (errorDialog != null) {
                                                errorDialog.show();
                                            }
                                        } else {
                                            Toast.makeText(getActivity(), "This device does not supported Google Drive ",Toast.LENGTH_LONG).show();
                                        }
                                    }

                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog chooseBackupModeDialog = builder.create();
                chooseBackupModeDialog.show();
            }
        });

        // Handles the RestoreDatabaseButton button click
        RestoreDatabaseButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // show instruction to user
                Toast.makeText(getActivity(), "Please select a backup file to restore", Toast.LENGTH_LONG).show();

                // launch a FILE picker for user to select restore file
                Intent intent = new Intent(BackupFragment.this.getActivity(), FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, "/sdcard");
                //can user select directories or not
                intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
                // launch file picker activity and wait for result to return
                startActivityForResult(intent, PICK_RESTORE_FILE);
            }
        });


        return rootView;
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(getActivity(), "Google Drive Connected ", Toast.LENGTH_SHORT).show();
        googleDriveConnected = true;
        doUploadToGoogleDrive();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        googleDriveConnected = false;
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appopriately
                Toast.makeText(getActivity(), "Connection to Google Drive is not resolvable",Toast.LENGTH_LONG).show();
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
        }

    }

    /**
     * Handles the activity result returned
     * @param requestCode The request code that was originally called
     * @param resultCode The result of the activity called
     * @param intent The caller Intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case (PICK_BACKUP_FILE): // user picked a directory to do the backup
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        String filePath = intent.getStringExtra(FileDialog.RESULT_PATH);
                        String backupFullPath = Utility.BackupDatabase(this.getActivity(),filePath);
                        Toast.makeText(this.getActivity(), "Database successfully backup to " + backupFullPath, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this.getActivity(), e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case (PICK_RESTORE_FILE): // user picked a single restore file that was previously backed up
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        String filePath = intent.getStringExtra(FileDialog.RESULT_PATH);
                        Utility.RestoreDatabase(getActivity(), filePath);
                        Toast.makeText(this.getActivity(), "Restore database successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this.getActivity(), e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }

}
