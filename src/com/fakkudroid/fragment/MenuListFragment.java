package com.fakkudroid.fragment;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.fakkudroid.MainActivity;
import com.fakkudroid.PreferencesActivity;
import com.fakkudroid.R;
import com.fakkudroid.adapter.MenuListAdapter;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.bean.VersionBean;
import com.fakkudroid.component.ActionImageButton;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;

public class MenuListFragment extends SherlockListFragment {

	private FakkuDroidApplication app;
	private MainActivity mainActivity;
	private int level = 1;
	private LinkedList<URLBean> lstURL;
	private View mFormView;
	private View mStatusView;
	private View ll;
	private View view;
	int nroPage = 1;
	int currentList = 0;
	int typeView;
	private String url;
	public final static int BROWSER_MANGA = 0;
	public final static int BROWSER_DOUJIN = 1;
	public final static int TYPE_LIST_TAGS = 0;
	public final static int TYPE_LIST_SERIES = 1;

	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	private View findViewById(int resource) {
		return view.findViewById(resource);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (FakkuDroidApplication) getActivity().getApplication();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), com.actionbarsherlock.R.style.Sherlock___Theme);

        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

		view = localInflater.inflate(R.layout.fragment_menu, container, false);
		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);
		ll = findViewById(R.id.ll);
		ActionImageButton btnPreviousPage = (ActionImageButton) findViewById(R.id.btnPreviousPage);
		ActionImageButton btnNextPage = (ActionImageButton) findViewById(R.id.btnNextPage);

		btnPreviousPage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				previousPage();
			}
		});

		btnNextPage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				nextPage();
			}
		});

		createMainMenu();
		return view;
	}

	public void nextPage() {
		nroPage++;
		loadPage();
		CharSequence text = "Page " + nroPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(getActivity(), text, duration);
		toast.show();
	}

	@SuppressLint("NewApi")
	private void loadPage() {
		TextView tvPage = (TextView) findViewById(R.id.tvPage);
		tvPage.setText("Page " + nroPage);
		Helper.executeAsyncTask(new DownloadList(), typeView);
	}

	public void previousPage() {
		if (nroPage - 1 == 0) {

			CharSequence text = "There aren't more pages.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(getActivity(), text, duration);
			toast.show();
		} else {
			nroPage--;
			loadPage();
			CharSequence text = "Page " + nroPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(getActivity(), text, duration);
			toast.show();
		}
	}

	public void createMainMenu() {
		level = 1;

		boolean connected = app.getSettingBean().isChecked();
		if (!connected)
			connected = FakkuConnection.isConnected();

		String[] lstMainMenu = getActivity().getResources().getStringArray(
				R.array.main_menu);
		int[] lstIcons = new int[] { R.drawable.home,
				R.drawable.navigation_forward_inverse, R.drawable.rating_important_inverse,
				R.drawable.navigation_back_inverse, -1, -2, -2,
				R.drawable.device_access_sd_storage_inverse, R.drawable.av_upload_inverse,
				R.drawable.action_settings_inverse };
		lstURL = new LinkedList<URLBean>();

		for (int i = 0; i < lstMainMenu.length; i++) {
			URLBean bean = new URLBean(lstMainMenu[i]);
			bean.setIcon(lstIcons[i]);
			lstURL.add(bean);
		}
		if (connected) {
			lstURL.remove(1);
		} else {
			lstURL.remove(3);
			lstURL.remove(2);
		}
		this.setListAdapter(new MenuListAdapter(this.getActivity(),
				R.layout.row_menu, 0, lstURL, true));
	}

	private void createBrowseManga() {
		ll.setVisibility(View.GONE);
		currentList = BROWSER_MANGA;
		level = 2;
		String[] lstBrowseManga = getActivity().getResources().getStringArray(
				R.array.browse_manga);
		String[] lstURLBrowseManga = getActivity().getResources()
				.getStringArray(R.array.url_browse_manga);
		lstURL = new LinkedList<URLBean>();

		for (int i = 0; i < lstBrowseManga.length; i++) {
			URLBean bean = new URLBean(lstURLBrowseManga[i], lstBrowseManga[i]);
			lstURL.add(bean);
		}
		lstURL.get(0).setIcon(R.drawable.content_undo_inverse);
		this.setListAdapter(new MenuListAdapter(this.getActivity(),
				R.layout.row_menu, 0, lstURL, false));
	}

	private void createBrowseDoujin() {
		ll.setVisibility(View.GONE);
		level = 2;
		currentList = BROWSER_DOUJIN;
		String[] lstBrowseDoujin = getActivity().getResources().getStringArray(
				R.array.browse_doujinshi);
		String[] lstURLBrowseDoujin = getActivity().getResources()
				.getStringArray(R.array.url_browse_doujinshis);
		lstURL = new LinkedList<URLBean>();

		for (int i = 0; i < lstBrowseDoujin.length; i++) {
			URLBean bean = new URLBean(lstURLBrowseDoujin[i],
					lstBrowseDoujin[i]);
			lstURL.add(bean);
		}
		lstURL.get(0).setIcon(R.drawable.content_undo_inverse);
		this.setListAdapter(new MenuListAdapter(this.getActivity(),
				R.layout.row_menu, 0, lstURL, false));
	}

	private void createTags() {
		ll.setVisibility(View.GONE);
		level = 3;
		String[] lstURLTags = null;
		if(currentList == BROWSER_DOUJIN){
			lstURLTags = getActivity().getResources().getStringArray(
					R.array.url_tags_doujinshis);
		}else{
			lstURLTags = getActivity().getResources()
					.getStringArray(R.array.url_tags_manga);
		}
		lstURL = new LinkedList<URLBean>();

		for (int i = 0; i < lstURLTags.length; i++) {
			URLBean bean = new URLBean(lstURLTags[i], lstURLTags[i].substring(
					lstURLTags[i].lastIndexOf("/")+1, lstURLTags[i].length()));
			lstURL.add(bean);
		}
		setData();
	}

	@SuppressLint("NewApi")
	public void onListItemClick(ListView l, View v, int position, long id) {
		URLBean bean = lstURL.get(position);
		if (level == 1) {
			if (bean.getDescription().equals("Manga")) {
				createBrowseManga();
			} else if (bean.getDescription().equals("Doujinshi")) {
				createBrowseDoujin();
			} else if (position == 0) {
                Intent itMain = new Intent(getActivity(), MainActivity.class);
                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN_LIST);
                getActivity().startActivityForResult(itMain, 1);
			} else if (bean.getDescription().equals("Downloads")) {
                Intent itMain = new Intent(getActivity(), MainActivity.class);
                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOWNLOADS);
                getActivity().startActivityForResult(itMain, 1);
			} else if (bean.getDescription().equals("My favorites")) {
                Intent itMain = new Intent(getActivity(), MainActivity.class);
                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.FAVORITES);
                itMain.putExtra(MainActivity.INTENT_VAR_USER, app.getSettingBean().getUser());
                itMain.putExtra(MainActivity.INTENT_VAR_URL, app.getSettingBean().getUrlUser());
                getActivity().startActivityForResult(itMain, 1);
			} else if (bean.getDescription().startsWith("Sign")) {
                Intent itMain = new Intent(getActivity(), MainActivity.class);
                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.LOGIN);
                getActivity().startActivityForResult(itMain, 1);
			} else if (bean.getDescription().equals("Preferences")) {
				Intent itPreference = new Intent(this.getActivity(),
						PreferencesActivity.class);
				getActivity().startActivity(itPreference);
			} else if (bean.getDescription().equals("Logout")) {
				UserBean sb = app.getSettingBean();
				sb.setChecked(false);
				new DataBaseHandler(getActivity()).updateSetting(sb);
				app.setSettingBean(null);
				FakkuConnection.disconnect();
				Toast.makeText(getActivity(),
						getResources().getString(R.string.loggedout),
						Toast.LENGTH_SHORT).show();
				createMainMenu();
			} else if (bean.getDescription().startsWith("Check")) {
				PackageInfo pInfo = null;
				try {
					pInfo = getActivity().getPackageManager().getPackageInfo(
							getActivity().getPackageName(), 0);
				} catch (NameNotFoundException e) {
					Helper.logError(this, "onOptionsItemSelected", e);
				}
				String version = pInfo.versionName;
				CharSequence text = "Your current version is " + version;
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(getActivity(), text, duration);
				toast.show();
                Helper.executeAsyncTask(new CheckerVersion());
			}
		} else if (level == 2) {
			if (bean.getUrl() == null || bean.getUrl().equals("")) {
				createMainMenu();
			} else if (bean.getDescription().endsWith("Tags")) {
				createTags();
			} else if (bean.getDescription().endsWith("Artist")
					|| bean.getDescription().endsWith("Series")) {
				url = bean.getUrl();
				nroPage = 1;
				typeView = TYPE_LIST_SERIES;
				Helper.executeAsyncTask(new DownloadList(), typeView);
			} else {
				if (position == lstURL.size() - 1) {
                    Intent itMain = new Intent(getActivity(), MainActivity.class);
                    itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN);
                    itMain.putExtra(MainActivity.INTENT_VAR_URL, bean.getUrl());
                    getActivity().startActivityForResult(itMain, 1);
				} else {
                    Intent itMain = new Intent(getActivity(), MainActivity.class);
                    itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN_LIST);
                    itMain.putExtra(MainActivity.INTENT_VAR_TITLE, bean.getDescription());
                    itMain.putExtra(MainActivity.INTENT_VAR_URL, bean.getUrl());
                    getActivity().startActivityForResult(itMain, 1);
				}
			}
		} else if (level == 3) {
			if (bean.getUrl() == null) {
				if (currentList == BROWSER_DOUJIN) {
					createBrowseDoujin();
				} else {
					createBrowseManga();
				}
			} else {
                Intent itMain = new Intent(getActivity(), MainActivity.class);
                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN_LIST);
                itMain.putExtra(MainActivity.INTENT_VAR_TITLE, bean.getDescription());
                itMain.putExtra(MainActivity.INTENT_VAR_URL, bean.getUrl());
                getActivity().startActivityForResult(itMain, 1);
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mStatusView.setVisibility(View.VISIBLE);
			mStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mFormView.setVisibility(View.VISIBLE);
			mFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private void setData() {
		level = 3;
		URLBean bean = new URLBean();
		bean.setDescription(getResources().getString(R.string.back));
		bean.setUrl(null);
		bean.setIcon(R.drawable.content_undo_inverse);
		lstURL.add(0, bean);
		this.setListAdapter(new MenuListAdapter(getActivity(),
				R.layout.row_menu, 0, lstURL, false));
	}

	class DownloadList extends AsyncTask<Integer, Float, Integer> {

		protected void onPreExecute() {
			showProgress(true);
		}

		protected Integer doInBackground(Integer... type) {

			try {
				Log.i(DownloadList.class.toString(),
						"URL List: " + app.getUrl(nroPage, url));
				lstURL = FakkuConnection.parseHTMLSeriesList(app.getUrl(
						nroPage, url));
			} catch (ClientProtocolException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (IOException e) {
				Helper.logError(this, e.getMessage(), e);
			}
			if (lstURL == null)
				lstURL = new LinkedList<URLBean>();
			return lstURL.size();
		}

		protected void onPostExecute(Integer bytes) {
			ll.setVisibility(View.VISIBLE);
			showProgress(false);
			setData();
		}
	}

    class CheckerVersion extends AsyncTask<String, Float, VersionBean> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(R.string.app_name);
            dialog.setMessage(getResources().getString(R.string.loading));
            dialog.setIcon(R.drawable.ic_launcher);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
        }

        @Override
        protected VersionBean doInBackground(String... arg0) {
            VersionBean versionBean = null;
            try {
                versionBean = FakkuConnection.getLastversion();
            } catch (Exception e) {
                Helper.logError(this, "error in verifing if exists new version.", e);
            }
            return versionBean;
        }

        protected void onPostExecute(final VersionBean result) {
            dialog.dismiss();
            if (result != null) {
                PackageInfo pInfo = null;
                try {
                    pInfo = getActivity().getPackageManager()
                            .getPackageInfo(getActivity().getPackageName(),
                                    0);
                    String currentVersion = pInfo.versionName;
                    if (currentVersion.compareToIgnoreCase(result
                            .getVersion_code()) < 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getActivity());
                        builder.setMessage(R.string.msg_new_version)
                                .setPositiveButton(R.string.visit_page,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface arg0,
                                                    int arg1) {
                                                Intent itVersion = new Intent(
                                                        Intent.ACTION_VIEW);
                                                itVersion.setData(Uri.parse(result
                                                        .getVersion_url()));
                                                getActivity()
                                                        .startActivity(itVersion);
                                            }
                                        })
                                .setNeutralButton(android.R.string.cancel, null)
                                .show();
                    }else{
                        Toast.makeText(getActivity(),R.string.you_have_the_last_version,Toast.LENGTH_SHORT).show();
                    }
                } catch (NameNotFoundException e) {
                    Helper.logError(this, "error getting current version", e);
                }
            }else{
                Toast.makeText(getActivity(),R.string.you_have_the_last_version,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
