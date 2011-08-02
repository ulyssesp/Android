package com.upopple.android.seethatmovie;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.upopple.android.seethatmovie.data.CategoriesDbAdapter;
import com.upopple.android.seethatmovie.data.MoviesDbAdapter;

public class AddMovie extends Activity {
	AutoCompleteTextView categoryAuto;
	TextView titleBox;
	AddMovieTextWatcher textWatch;
	
	CheckBox seenIt;

	private static final int CATEGORY_SELECT = 0;
	private static final int CATEGORY_ERROR_UNDERSCORE = 100;
	Dialog inputError;
	
	ArrayList<String> movieList;
	
	MoviesDbAdapter mdb;
	CategoriesDbAdapter cdb;
	
	Button addbutton;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_movie);
		
		mdb = new MoviesDbAdapter(this);
		mdb.open();

		cdb = mdb.getCdbAdapter();
		cdb.open();
		
		textWatch = new AddMovieTextWatcher();
		
		Intent i = getIntent();
		titleBox = (TextView)findViewById(R.id.movieTitle);
		titleBox.setText(i.getStringExtra("movieTitle"));
		
		categoryAuto = (AutoCompleteTextView)findViewById(R.id.movieCategoryEdit);
		ArrayList<String> allCategories = new ArrayList<String>();
		allCategories.addAll(cdb.getAllCategories());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, allCategories.toArray(new String[]{}));
		categoryAuto.setAdapter(adapter);
		categoryAuto.addTextChangedListener(textWatch);
		
		seenIt = (CheckBox) findViewById(R.id.seenIt);
		
		addbutton = (Button)findViewById(R.id.addMovieButton);
		addbutton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				try{
					saveItToDb();
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	
	public void saveItToDb(){
		String movieId = getIntent().getStringExtra("movieId");
		String movieTitle = titleBox.getText().toString();
		
		//Adding categories
		ArrayList<String> movieCategories = new ArrayList<String>();
		for(String category: categoryAuto.getText().toString().split(",")){
			if(category.trim().startsWith("_")){
				
			}
			movieCategories.add(category.trim());
		}
		
		if(seenIt.isChecked())
			movieCategories.add("_seen");

		mdb.insertmovie(movieId, movieTitle, movieCategories);
			
		cdb.close();
		mdb.close();
		titleBox.setText("");
		categoryAuto.setText("");
		Intent i = new Intent(AddMovie.this, Home.class);
		startActivity(i);
	}	
	
	@Override
	protected Dialog onCreateDialog(int id){
		Dialog inputError;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id){
		case CATEGORY_SELECT: 
			builder.setTitle("Add Categories")
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						categoryAuto.getText().delete(0, 1);
					}
				});
			inputError = builder.create();
			break;
		case CATEGORY_ERROR_UNDERSCORE: 
			builder.setMessage("Sorry! The category name cannot start with a _ (underscore).")
				.setCancelable(true)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						categoryAuto.getText().delete(0, 1);
					}
				});
			inputError = builder.create();
			break;
		default:
			builder.setMessage("Oh no! Something broke.")
				.setCancelable(true)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
			inputError = builder.create();
			break;
		}
		return inputError;
	}
	
	private class AddMovieTextWatcher implements TextWatcher{

		
		public void afterTextChanged(Editable e) {
			if(e.toString().startsWith("_")){
				showDialog(CATEGORY_ERROR_UNDERSCORE);
			}
		}

		
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
