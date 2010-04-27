/**
 * 
 */
package com.rackspacecloud.android;

import java.util.ArrayList;
import java.util.Iterator;

import com.rackspace.cloud.servers.api.client.Flavor;
import com.rackspace.cloud.servers.api.client.Image;
import com.rackspace.cloud.servers.api.client.Server;
import com.rackspace.cloud.servers.api.client.ServerManager;
// import com.rackspacecloud.android.RackspaceCloudActivity.LoadFlavorsTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author mike
 *
 */
public class AddServerActivity extends Activity implements OnItemSelectedListener, OnClickListener {

	private Image[] images;
	private Flavor[] flavors;
	private String selectedImageId;
	private String selectedFlavorId;
	private EditText serverName;
	private Spinner imageSpinner;
	private Spinner flavorSpinner;
	private Server server;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createserver);
        serverName = (EditText) findViewById(R.id.server_name);
        ((Button) findViewById(R.id.save_button)).setOnClickListener(this);
        loadImageSpinner();
        loadFlavorSpinner();
    }

    private void loadImageSpinner() {
		imageSpinner = (Spinner) findViewById(R.id.image_spinner);
		imageSpinner.setOnItemSelectedListener(this);
		String imageNames[] = new String[Image.getImages().size()]; 
		images = new Image[Image.getImages().size()];

		Iterator<Image> iter = Image.getImages().values().iterator();
		int i = 0;
		while (iter.hasNext()) {
			Image image = iter.next();
			images[i] = image;
			imageNames[i] = image.getName();
			i++;
		}
		selectedImageId = images[0].getId();
		ArrayAdapter<String> imageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, imageNames);
		imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		imageSpinner.setAdapter(imageAdapter);
    }
    
    private void loadFlavorSpinner() {
		flavorSpinner = (Spinner) findViewById(R.id.flavor_spinner);
		flavorSpinner.setOnItemSelectedListener(this);
		String flavorNames[] = new String[Flavor.getFlavors().size()]; 
		flavors = new Flavor[Flavor.getFlavors().size()];

		Iterator<Flavor> iter = Flavor.getFlavors().values().iterator();
		int i = 0;
		while (iter.hasNext()) {
			Flavor flavor = iter.next();
			flavors[i] = flavor;
			flavorNames[i] = flavor.getName() + ", " + flavor.getDisk() + " GB disk";
			i++;
		}
		selectedFlavorId = flavors[0].getId();
		ArrayAdapter<String> flavorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, flavorNames);
		flavorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		flavorSpinner.setAdapter(flavorAdapter);
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if (parent == imageSpinner) {
			selectedImageId = images[position].getId();
		} else if (parent == flavorSpinner) {
			selectedFlavorId = flavors[position].getId();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if ("".equals(serverName.getText().toString())) {
			showAlert("Required Fields Missing", "Server name is required.");
		} else {
			showActivityIndicators();
			server = new Server();
			server.setName(serverName.getText().toString());
			server.setImageId(selectedImageId);
			server.setFlavorId(selectedFlavorId);
			new SaveServerTask().execute((Void[]) null);
		}
	}
	
	// TODO: extract to a util class?
    private void showAlert(String title, String message) {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	        return;
	    } }); 
		alert.show();
    }
	
    private void setActivityIndicatorsVisibility(int visibility) {
        ProgressBar pb = (ProgressBar) findViewById(R.id.save_server_progress_bar);
    	TextView tv = (TextView) findViewById(R.id.saving_server_label);
        pb.setVisibility(visibility);
        tv.setVisibility(visibility);
    }

    private void showActivityIndicators() {
    	setActivityIndicatorsVisibility(View.VISIBLE);
    }
    
    private void hideActivityIndicators() {
    	setActivityIndicatorsVisibility(View.INVISIBLE);
    }
        
    private class SaveServerTask extends AsyncTask<Void, Void, Server> {
    	
		@Override
		protected Server doInBackground(Void... arg0) {
			(new ServerManager()).create(server);
			return server;
		}
    	
		@Override
		protected void onPostExecute(Server result) {
			//setServerList(result);
			//this.
			hideActivityIndicators();
			System.out.println("done");
		}
    }
	
}