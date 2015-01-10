package com.example.nurhazim.i_recall;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * File picker obtained from http://code.google.com/p/android-file-dialog/
 *
 * @author android
 *
 */
public class FileDialog extends ListActivity {

    /**
     * Key of an item of paths list.
     */
    private static final String ITEM_KEY = "key";

    /**
     * Image of an item from the list of paths (directory or file).
     */
    private static final String ITEM_IMAGE = "image";

    /**
     * Root directory.
     */
    private static final String ROOT = "/";

    /**
     * Activity input parameter: initial path. Standard: ROOT.
     */
    public static final String START_PATH = "START_PATH";

    /**
     * Activity input parameter: file formats filter.
     * Standard: null.
     */
    public static final String FORMAT_FILTER = "FORMAT_FILTER";

    /**
     * Parameter output of Activity: path chosen.
     * Standard: null.
     */
    public static final String RESULT_PATH = "RESULT_PATH";

    /**
     * Input parameter of Activity: Type Selection: You can create new paths or not
     * Standard: not allowed.
     *
     * @see {@link SelectionMode}
     */
    public static final String SELECTION_MODE = "SELECTION_MODE";

    /**
     * Activity input parameter: If allowed to choose and directories.
     * Standard: false.
     */
    public static final String CAN_SELECT_DIR = "CAN_SELECT_DIR";

    private List<String> path = null;
    private TextView myPath;
    private EditText mFileName;
    private ArrayList<HashMap<String, Object>> mList;

    private Button selectButton;

    private LinearLayout layoutSelect;
    private LinearLayout layoutCreate;
    private InputMethodManager inputManager;
    private String parentPath;
    private String currentPath = ROOT;

    private int selectionMode = SelectionMode.MODE_CREATE;

    private String[] formatFilter = null;

    private boolean canSelectDir = false;

    private File selectedFile;
    private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED, getIntent());

        setContentView(R.layout.file_dialog_main);
        myPath = (TextView) findViewById(R.id.path);
        mFileName = (EditText) findViewById(R.id.fdEditTextFile);

        inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        selectButton = (Button) findViewById(R.id.fdButtonSelect);
        selectButton.setEnabled(false);
        selectButton.setOnClickListener(new OnClickListener() {

            //@Override
            public void onClick(View v) {
                if (selectedFile != null) {
                    //Toast.makeText(FileDialog.this, selectedFile.getPath() , Toast.LENGTH_SHORT).show();
                    getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        });

        final Button newButton = (Button) findViewById(R.id.fdButtonNew);
        newButton.setOnClickListener(new OnClickListener() {

            //@Override
            public void onClick(View v) {
                setCreateVisible(v);

                mFileName.setText("");
                mFileName.requestFocus();
            }
        });

        selectionMode = getIntent().getIntExtra(SELECTION_MODE, SelectionMode.MODE_CREATE);

        formatFilter = getIntent().getStringArrayExtra(FORMAT_FILTER);

        canSelectDir = getIntent().getBooleanExtra(CAN_SELECT_DIR, false);

        if (selectionMode == SelectionMode.MODE_OPEN) {
            newButton.setEnabled(false);
            newButton.setVisibility(View.GONE);
        }

        layoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);
        layoutCreate = (LinearLayout) findViewById(R.id.fdLinearLayoutCreate);
        layoutCreate.setVisibility(View.GONE);

        final Button cancelButton = (Button) findViewById(R.id.fdButtonCancel);
        cancelButton.setOnClickListener(new OnClickListener() {

            //@Override
            public void onClick(View v) {
                setSelectVisible(v);
            }

        });
        final Button createButton = (Button) findViewById(R.id.fdButtonCreate);
        createButton.setOnClickListener(new OnClickListener() {

            //@Override
            public void onClick(View v) {
                if (mFileName.getText().length() > 0) {
                    getIntent().putExtra(RESULT_PATH, currentPath + "/" + mFileName.getText());
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        });

        String startPath = getIntent().getStringExtra(START_PATH);
        startPath = startPath != null ? startPath : ROOT;
        if (canSelectDir) {
            File file = new File(startPath);
            selectedFile = file;
            selectButton.setEnabled(true);
        }
        getDir(startPath);
    }

    private void getDir(String dirPath) {

        boolean useAutoSelection = dirPath.length() < currentPath.length();

        Integer position = lastPositions.get(parentPath);

        getDirImpl(dirPath);

        if (position != null && useAutoSelection) {
            getListView().setSelection(position);
        }

    }

    /**
     * Mount the structure of files and directories children of the given directory.
     *
     * @param dirPath
     *            Directory path.
     */
    private void getDirImpl(final String dirPath) {

        currentPath = dirPath;

        final List<String> item = new ArrayList<String>();
        path = new ArrayList<String>();
        mList = new ArrayList<HashMap<String, Object>>();

        File f = new File(currentPath);
        File[] files = f.listFiles();
        if (files == null) {
            currentPath = ROOT;
            f = new File(currentPath);
            files = f.listFiles();
        }
        myPath.setText(getText(R.string.location) + ": " + currentPath);

        if (!currentPath.equals(ROOT)) {

            item.add(ROOT);
            addItem(ROOT, R.drawable.folder);
            path.add(ROOT);

            item.add("../");
            addItem("../", R.drawable.folder);
            path.add(f.getParent());
            parentPath = f.getParent();

        }

        TreeMap<String, String> dirsMap = new TreeMap<String, String>();
        TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
        TreeMap<String, String> filesMap = new TreeMap<String, String>();
        TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                dirsMap.put(dirName, dirName);
                dirsPathMap.put(dirName, file.getPath());
            } else {
                final String fileName = file.getName();
                final String fileNameLwr = fileName.toLowerCase();
                // has filter format, or uses
                if (formatFilter != null) {
                    boolean contains = false;
                    for (int i = 0; i < formatFilter.length; i++) {
                        final String formatLwr = formatFilter[i].toLowerCase();
                        if (fileNameLwr.endsWith(formatLwr)) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        filesMap.put(fileName, fileName);
                        filesPathMap.put(fileName, file.getPath());
                    }
                    // if not, add all files
                } else {
                    filesMap.put(fileName, fileName);
                    filesPathMap.put(fileName, file.getPath());
                }
            }
        }
        item.addAll(dirsMap.tailMap("").values());
        item.addAll(filesMap.tailMap("").values());
        path.addAll(dirsPathMap.tailMap("").values());
        path.addAll(filesPathMap.tailMap("").values());

        SimpleAdapter fileList = new SimpleAdapter(this, mList, R.layout.file_dialog_row, new String[] {
                ITEM_KEY, ITEM_IMAGE }, new int[] { R.id.fdrowtext, R.id.fdrowimage });

        for (String dir : dirsMap.tailMap("").values()) {
            addItem(dir, R.drawable.folder);
        }

        for (String file : filesMap.tailMap("").values()) {
            addItem(file, R.drawable.file);
        }

        fileList.notifyDataSetChanged();

        setListAdapter(fileList);

    }

    private void addItem(String fileName, int imageId) {
        HashMap<String, Object> item = new HashMap<String, Object>();
        item.put(ITEM_KEY, fileName);
        item.put(ITEM_IMAGE, imageId);
        mList.add(item);
    }

    /**
     * When you click the list item, you must:
     * 1) If directory, open its children files;
     * 2) If you can choose directory, define it as the path chosen.
     * 3) If file, define it as path chosen.
     * 4) Active button selection.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File file = new File(path.get(position));

        setSelectVisible(v);

        if (file.isDirectory()) {
            selectButton.setEnabled(false);
            if (file.canRead()) {
                lastPositions.put(currentPath, position);
                getDir(path.get(position));
                if (canSelectDir) {
                    selectedFile = file;
                    v.setSelected(true);
                    selectButton.setEnabled(true);
                }
            } else {
                new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
                        .setTitle("[" + file.getName() + "] " + getText(R.string.cant_read_folder))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            //@Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        } else {
            selectedFile = file;
            v.setSelected(true);
            selectButton.setEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            selectButton.setEnabled(false);

            if (layoutCreate.getVisibility() == View.VISIBLE) {
                layoutCreate.setVisibility(View.GONE);
                layoutSelect.setVisibility(View.VISIBLE);
            } else {
                if (!currentPath.equals(ROOT)) {
                    getDir(parentPath);
                } else {
                    return super.onKeyDown(keyCode, event);
                }
            }

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Sets the button CREATE visible.
     *
     * @param v
     */
    private void setCreateVisible(View v) {
        layoutCreate.setVisibility(View.VISIBLE);
        layoutSelect.setVisibility(View.GONE);

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        selectButton.setEnabled(false);
    }

    /**
     * Sets the button SELECT visible.
     *
     * @param v
     */
    private void setSelectVisible(View v) {
        layoutCreate.setVisibility(View.GONE);
        layoutSelect.setVisibility(View.VISIBLE);

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        selectButton.setEnabled(false);
    }
}
