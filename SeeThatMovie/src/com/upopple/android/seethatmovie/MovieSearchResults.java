package com.upopple.android.seethatmovie;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.upopple.android.seethatmovie.data.MoviesDbAdapter;
import com.upopple.android.seethatmovie.web.RTMovieResult;
import com.upopple.android.seethatmovie.web.RTMovieResults;
import com.upopple.android.seethatmovie.web.RottenTomatoesAPIAsync;

public class MovieSearchResults extends ListActivity {
	private static final int MOVIE_ALREADY_EXISTS = 100;
	
	MoviesDbAdapter mdb;
	
	SearchAdapter searchAdapter;
	TextView listDescription;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.movie_search_results);
		super.onCreate(savedInstanceState);

		mdb = new MoviesDbAdapter(this);
		mdb.open();
		
		listDescription = (TextView)findViewById(R.id.categoryListText);	
		
		Intent i = getIntent();
		String search = i.getStringExtra("search");
		searchAdapter = new SearchAdapter(this, search);
		this.setListAdapter(searchAdapter);
		
	}
	
	protected Dialog onCreateDialog(int id){
		Dialog inputError;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id){
		case MOVIE_ALREADY_EXISTS:
			builder.setTitle("Can't add that")
			.setMessage("You've already added that movie!")
			.setCancelable(true)
			.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
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
	
	
	private class SearchAdapter extends BaseAdapter{
		private LayoutInflater li;
		private RTMovieResults movieResults;
		
		public SearchAdapter(Context context, String search){
			li = LayoutInflater.from(context);
			
			try {
				movieResults = (RTMovieResults)new RottenTomatoesAPIAsync().execute(new String[]{""+RottenTomatoesAPIAsync.GET_MOVIE_TITLES, search}).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			if(movieResults == null || movieResults.getMovies().size() == 0){
				movieResults = new RTMovieResults();
				movieResults.setMovies(new ArrayList<RTMovieResult>());
				listDescription.setText("Oh no! No movies were found.\nSelect what you typed in to add it anyway, or go back to try again.");
				movieResults.getMovies().add(new RTMovieResult(search));
			} else {
				listDescription.setText("Sweet, these movies match your search.\nSelect the right one!");
			}
		}
		

		public int getCount() {return movieResults.getMovies().size();}
		public RTMovieResult getItem(int i){return movieResults.getMovies().get(i);}
		public long getItemId(int i){return i;}
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			View v = convertView;
			if((v==null) || v.getTag() == null){
				v = li.inflate(R.layout.movie_search_result, null);
				holder = new ViewHolder();
				holder.mTitle = (TextView)v.findViewById(R.id.movieSearchResult);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}
			
			holder.movie = getItem(position);
			holder.mTitle.setText(holder.movie.getTitleYear());
			
			v.setTag(holder);
			
			return v;
		}
		
		public class ViewHolder {
			RTMovieResult movie;
			TextView mTitle;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String title = ((RTMovieResult)l.getItemAtPosition(position)).getTitleYear();
		if(mdb.getMovieByTitle(title, false) != null)
			showDialog(MOVIE_ALREADY_EXISTS);
		else{
			Intent i = new Intent(MovieSearchResults.this, AddMovie.class);
			i.putExtra("movieTitle", title);
			startActivity(i);
		}
	}

}
