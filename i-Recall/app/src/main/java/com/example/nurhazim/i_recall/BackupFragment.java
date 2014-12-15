package com.example.nurhazim.i_recall;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class BackupFragment extends Fragment {
    public static final int PICK_BACKUP_FILE = 1;
    public static final int PICK_RESTORE_FILE = 2;

    public BackupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                Toast.makeText(getActivity(), "Please select a DIRECTORY to save your database backup", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(BackupFragment.this.getActivity(), FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, "/sdcard");

                //can user select directories or not
                intent.putExtra(FileDialog.CAN_SELECT_DIR, true);

                startActivityForResult(intent, PICK_BACKUP_FILE);
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
