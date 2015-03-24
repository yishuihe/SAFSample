package org.zhixin.safsample.documentoperation;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.zhixin.safsample.MainActivity;
import org.zhixin.safsample.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class DocumentOperationFragment extends Fragment {
    private static final String TAG = DocumentOperationFragment.class.getSimpleName();

    // views
    private Button mCreateBtn;
    private Button mDeleteBtn;
    private Button mNewBtn;
    private ImageView mImageView;
    private TextView mImgTitle;

    private Uri mImgUri = null;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static int mId = -1;

    private static final int READ_REQUEST_CODE = 40;
    private static final int CREATE_REQUEST_CODE = 41;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DocumentOperationFragment newInstance(int sectionNumber) {
        DocumentOperationFragment fragment = new DocumentOperationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        Log.d(TAG, "newInstance: id = " + sectionNumber);
        mId = sectionNumber;
        fragment.setArguments(args);
        return fragment;
    }

    public DocumentOperationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: onCreateView, id = " + mId);
        View rootView = inflater.inflate(R.layout.document_operation, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach: onAttach");
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCreateBtn = (Button)view.findViewById(R.id.open);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: create button clicked");
                performOpenDocument();
            }
        });

        mDeleteBtn = (Button)view.findViewById(R.id.delete);
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: create button clicked");
                performDeleteDocument();
                mDeleteBtn.setEnabled(false);
            }
        });
        mDeleteBtn.setEnabled(false);

        mNewBtn = (Button)view.findViewById(R.id.new_document);
        mNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performNewDocument();
            }
        });
        mImageView = (ImageView)view.findViewById(R.id.image);
        mImgTitle = (TextView)view.findViewById(R.id.title);
    }

    private void performOpenDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // only for images
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);

    }

    private void performDeleteDocument() {
        if(mImgUri != null) {
            DocumentsContract.deleteDocument(getActivity().getContentResolver(), mImgUri);
            hideImage();
        }
    }

    private void hideImage() {
        mImageView.setImageBitmap(null);
        mImgTitle.setText("");
    }

    private void performNewDocument() {
        String name = "new-file.txt";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, name);

        startActivityForResult(intent, CREATE_REQUEST_CODE);

        hideImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == READ_REQUEST_CODE ) {
                Uri uri = null;
                if(data != null) {
                    uri = data.getData();
                    Log.d(TAG, "onActivityResult: data uri is:" + uri.toString());
                    showImage(uri);
                    mImgUri = uri;
                }
            } else if (requestCode == CREATE_REQUEST_CODE) {
                Log.d(TAG, "onActivityResult: document created");
                Toast.makeText(getActivity(), "document created", Toast.LENGTH_LONG).show();
            }

        }

    }

    //display images
    private void showImage(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null, null);

        try {
            if(cursor != null && cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.d(TAG, "showImage: name: " + displayName);

                ParcelFileDescriptor parcelFileDescriptor =
                        getActivity().getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                mImageView.setImageBitmap(image);
                mImgTitle.setText(displayName);
                mDeleteBtn.setEnabled(true);
                parcelFileDescriptor.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "showImage: file now found exception");
        } catch(IOException e) {
            Log.e(TAG, "showImage: IOException");
        } finally {
            cursor.close();
        }

    }
}
